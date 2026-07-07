/**
 * Validações e tratamento de conflitos no planner de escalas
 * Detecta problemas e oferece soluções automáticas
 */

/**
 * Validar se funcionário já está escalado no dia
 * @param {number} day - Dia do mês
 * @param {string} employeeName - Nome do funcionário
 * @returns {boolean} true se está conflitando
 */
function checkEmployeeConflictOnDay(day, employeeName) {
  const dayShifts = getShiftsForDay(day);
  return dayShifts && dayShifts.some(s => s.employeeName === employeeName);
}

/**
 * Validar múltiplos turnos no mesmo dia
 * @param {number} day - Dia do mês
 * @param {string} employeeName - Nome do funcionário
 * @returns {array} Array de turnos conflitantes
 */
function findMultipleShiftsConflicts(day, employeeName) {
  const dayShifts = getShiftsForDay(day);
  return dayShifts ? dayShifts.filter(s => s.employeeName === employeeName) : [];
}

/**
 * Validar se funcionário trabalhou muitos dias seguidos
 * @param {string} employeeName - Nome do funcionário
 * @param {number} maxConsecutiveDays - Máximo de dias consecutivos permitidos
 * @returns {object} Relatório de dias consecutivos
 */
function checkConsecutiveDaysWarning(employeeName, maxConsecutiveDays = 6) {
  const monthShifts = getAllShiftsForMonth();
  const workDays = [];
  
  Object.entries(monthShifts).forEach(([dateKey, shifts]) => {
    if (shifts.some(s => s.employeeName === employeeName)) {
      workDays.push(parseInt(dateKey.split('-')[2]));
    }
  });
  
  workDays.sort((a, b) => a - b);
  
  let maxConsecutive = 1;
  let currentConsecutive = 1;
  let consecutiveSequences = [];
  let currentSequence = [workDays[0]];
  
  for (let i = 1; i < workDays.length; i++) {
    if (workDays[i] === workDays[i - 1] + 1) {
      currentConsecutive++;
      currentSequence.push(workDays[i]);
    } else {
      if (currentConsecutive >= maxConsecutiveDays) {
        consecutiveSequences.push({
          days: currentSequence,
          count: currentConsecutive
        });
      }
      currentConsecutive = 1;
      currentSequence = [workDays[i]];
    }
  }
  
  if (currentConsecutive >= maxConsecutiveDays) {
    consecutiveSequences.push({
      days: currentSequence,
      count: currentConsecutive
    });
  }
  
  return {
    workDays: workDays.length,
    warningTriggered: consecutiveSequences.length > 0,
    consecutiveSequences: consecutiveSequences
  };
}

/**
 * Validar se dia tem cobertura mínima de funcionários
 * @param {number} day - Dia do mês
 * @param {number} minimumCoverage - Mínimo de funcionários requerido
 * @returns {boolean} true se atende o mínimo
 */
function validateMinimumCoverage(day, minimumCoverage = 2) {
  const dayShifts = getShiftsForDay(day);
  const uniqueEmployees = new Set(dayShifts ? dayShifts.map(s => s.employeeName) : []);
  return uniqueEmployees.size >= minimumCoverage;
}

/**
 * Validar se todas as turmas estão cobertas
 * @param {number} day - Dia do mês
 * @returns {object} Relatório de cobertura por turno
 */
function validateShiftCoverage(day) {
  const dayShifts = getShiftsForDay(day);
  const shiftTypes = getAllShiftOptions();
  const coverage = {};
  
  shiftTypes.forEach(type => {
    coverage[type] = dayShifts ? dayShifts.filter(s => s.shiftType === type).length : 0;
  });
  
  return {
    day: day,
    coverageByShift: coverage,
    totalCoverage: dayShifts ? dayShifts.length : 0,
    allShiftsCovered: Object.values(coverage).every(count => count > 0)
  };
}

/**
 * Obter sugestões automáticas de escalas
 * @param {number} day - Dia do mês
 * @returns {array} Array de sugestões
 */
function getAutoSchedulingSuggestions(day) {
  const suggestions = [];
  const dayShifts = getShiftsForDay(day);
  const employees = getAllEmployees();
  const coverage = validateShiftCoverage(day);
  
  // Sugestão 1: Cobertura mínima
  if (!validateMinimumCoverage(day, 2)) {
    const uncovered = employees.filter(emp => 
      !dayShifts || !dayShifts.some(s => s.employeeName === emp.name)
    );
    
    if (uncovered.length > 0) {
      suggestions.push({
        type: 'coverage',
        severity: 'warning',
        message: `⚠️ Dia ${day} com poucos funcionários. Recomenda-se adicionar mais ${2 - (dayShifts ? dayShifts.length : 0)} funcionário(s).`,
        suggestedEmployees: uncovered.slice(0, 2)
      });
    }
  }
  
  // Sugestão 2: Turnos não cobertos
  getAllShiftOptions().forEach(shiftType => {
    if (coverage.coverageByShift[shiftType] === 0) {
      const availableEmployees = employees.filter(emp => {
        const conflicts = findMultipleShiftsConflicts(day, emp.name);
        return conflicts.length === 0;
      });
      
      if (availableEmployees.length > 0) {
        suggestions.push({
          type: 'shift_coverage',
          severity: 'info',
          message: `📌 Turno "${getShiftConfig(shiftType).label}" sem cobertura no dia ${day}`,
          suggestedEmployees: availableEmployees.slice(0, 1),
          shiftType: shiftType
        });
      }
    }
  });
  
  return suggestions;
}

/**
 * Validar toda a escala do mês
 * @returns {object} Relatório completo de validação
 */
function validateEntireMonth() {
  const report = {
    totalDays: new Date(currentPlannerYear, currentPlannerMonth + 1, 0).getDate(),
    issues: [],
    warnings: [],
    suggestions: [],
    statistics: {
      daysWithCoverage: 0,
      daysWithoutCoverage: 0,
      totalShifts: 0,
      uniqueEmployees: new Set()
    }
  };
  
  for (let day = 1; day <= report.totalDays; day++) {
    const dayShifts = getShiftsForDay(day);
    const coverage = validateShiftCoverage(day);
    const suggestions = getAutoSchedulingSuggestions(day);
    
    if (dayShifts && dayShifts.length > 0) {
      report.statistics.daysWithCoverage++;
      report.statistics.totalShifts += dayShifts.length;
      dayShifts.forEach(shift => {
        report.statistics.uniqueEmployees.add(shift.employeeName);
      });
    } else {
      report.statistics.daysWithoutCoverage++;
      report.issues.push(`Dia ${day} sem nenhuma escala`);
    }
    
    // Adicionar sugestões
    suggestions.forEach(s => {
      if (s.severity === 'warning') {
        report.warnings.push(s);
      } else {
        report.suggestions.push(s);
      }
    });
  }
  
  report.statistics.uniqueEmployees = report.statistics.uniqueEmployees.size;
  
  return report;
}

/**
 * Aplicar sugestão automática
 * @param {object} suggestion - Sugestão a aplicar
 */
function applySuggestion(suggestion) {
  if (suggestion.type === 'coverage' && suggestion.suggestedEmployees.length > 0) {
    const employee = suggestion.suggestedEmployees[0];
    const shiftType = document.getElementById('dragShiftType')?.value || 'Manhã (06h - 14h)';
    addShiftToDay(suggestion.day, employee.name, shiftType);
    showNotification(`✅ ${employee.name} escalado para dia ${suggestion.day}`, 'success');
  }
  
  if (suggestion.type === 'shift_coverage' && suggestion.suggestedEmployees.length > 0) {
    const employee = suggestion.suggestedEmployees[0];
    addShiftToDay(suggestion.day, employee.name, suggestion.shiftType);
    showNotification(`✅ ${employee.name} escalado para ${getShiftConfig(suggestion.shiftType).label}`, 'success');
  }
}

/**
 * Exibir relatório de validação do mês
 */
function showMonthlyValidationReport() {
  const report = validateEntireMonth();
  
  const modal = document.createElement('div');
  modal.className = 'fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4';
  modal.id = 'validationReportModal';
  
  let issuesHTML = '';
  let warningsHTML = '';
  let suggestionsHTML = '';
  
  if (report.issues.length > 0) {
    issuesHTML = `
      <div class="bg-hdRedLight border border-hdRed rounded-xl p-4 mb-4">
        <h4 class="font-bold text-hdRed mb-2 flex items-center gap-2">
          <span class="material-symbols-outlined">error</span>
          Problemas Críticos (${report.issues.length})
        </h4>
        <ul class="text-sm text-hdRed space-y-1">
          ${report.issues.map(issue => `<li>• ${issue}</li>`).join('')}
        </ul>
      </div>
    `;
  }
  
  if (report.warnings.length > 0) {
    warningsHTML = `
      <div class="bg-yellow-100 border border-yellow-500 rounded-xl p-4 mb-4">
        <h4 class="font-bold text-yellow-800 mb-2 flex items-center gap-2">
          <span class="material-symbols-outlined">warning</span>
          Avisos (${report.warnings.length})
        </h4>
        <div class="space-y-2">
          ${report.warnings.slice(0, 3).map(w => `
            <div class="text-sm text-yellow-800">
              ${w.message}
              ${w.suggestedEmployees ? `<br><small>${w.suggestedEmployees.map(e => e.name).join(', ')}</small>` : ''}
            </div>
          `).join('')}
        </div>
      </div>
    `;
  }
  
  if (report.suggestions.length > 0) {
    suggestionsHTML = `
      <div class="bg-hdGreenLight border border-hdGreen rounded-xl p-4 mb-4">
        <h4 class="font-bold text-hdGreen mb-2 flex items-center gap-2">
          <span class="material-symbols-outlined">lightbulb</span>
          Sugestões (${report.suggestions.length})
        </h4>
        <div class="space-y-2 text-sm">
          ${report.suggestions.slice(0, 3).map(s => `<div>💡 ${s.message}</div>`).join('')}
        </div>
      </div>
    `;
  }
  
  modal.innerHTML = `
    <div class="bg-white rounded-3xl p-6 shadow-xl max-w-2xl w-full max-h-[80vh] overflow-y-auto">
      <div class="flex justify-between items-center mb-4 border-b border-hdBorder pb-3">
        <h3 class="text-lg font-bold text-hdTextPrimary">📊 Relatório de Validação - Mês ${currentPlannerMonth + 1}/${currentPlannerYear}</h3>
        <button onclick="document.getElementById('validationReportModal').remove()" class="text-hdTextSecondary hover:text-hdTextPrimary">
          <span class="material-symbols-outlined">close</span>
        </button>
      </div>
      
      <!-- Estatísticas -->
      <div class="grid grid-cols-4 gap-3 mb-6">
        <div class="bg-hdSurfaceContainer p-3 rounded-xl text-center">
          <p class="text-xs text-hdTextSecondary">Total Dias</p>
          <p class="text-lg font-bold text-hdPrimary">${report.statistics.daysWithCoverage}/${report.statistics.totalDays}</p>
        </div>
        <div class="bg-hdSurfaceContainer p-3 rounded-xl text-center">
          <p class="text-xs text-hdTextSecondary">Escalas</p>
          <p class="text-lg font-bold text-hdPrimary">${report.statistics.totalShifts}</p>
        </div>
        <div class="bg-hdSurfaceContainer p-3 rounded-xl text-center">
          <p class="text-xs text-hdTextSecondary">Funcionários</p>
          <p class="text-lg font-bold text-hdPrimary">${report.statistics.uniqueEmployees}</p>
        </div>
        <div class="bg-hdSurfaceContainer p-3 rounded-xl text-center">
          <p class="text-xs text-hdTextSecondary">Sem Cobertura</p>
          <p class="text-lg font-bold text-hdRed">${report.statistics.daysWithoutCoverage}</p>
        </div>
      </div>
      
      ${issuesHTML}
      ${warningsHTML}
      ${suggestionsHTML}
      
      <div class="flex gap-3 pt-4 border-t border-hdBorder">
        <button onclick="document.getElementById('validationReportModal').remove()" class="flex-1 border border-hdBorder text-hdTextPrimary font-bold py-2.5 rounded-xl hover:bg-hdSurface transition">
          Fechar
        </button>
        <button onclick="clearMonthShifts(); document.getElementById('validationReportModal').remove();" class="flex-1 bg-hdRed text-white font-bold py-2.5 rounded-xl hover:bg-red-700 transition flex items-center justify-center gap-2">
          <span class="material-symbols-outlined text-sm">delete_sweep</span>
          Limpar Tudo
        </button>
      </div>
    </div>
  `;
  
  document.body.appendChild(modal);
  modal.onclick = (e) => {
    if (e.target === modal) modal.remove();
  };
}

/**
 * Exportar função globalmente
 */
window.showMonthlyValidationReport = showMonthlyValidationReport;
