/**
 * Gerenciamento de dados do planner de escalas
 * Local storage e integração com Firebase
 */

const SHIFTS_STORAGE_KEY = 'postoadm_shifts';

/**
 * Obter todas as escalas de um dia
 * @param {number} day - Dia do mês
 * @returns {array} Array de escalas do dia
 */
function getShiftsForDay(day) {
  const shifts = JSON.parse(localStorage.getItem(SHIFTS_STORAGE_KEY) || '{}');
  const dateKey = `${currentPlannerYear}-${String(currentPlannerMonth + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
  
  return shifts[dateKey] || [];
}

/**
 * Salvar escalas para um dia
 * @param {number} day - Dia do mês
 * @param {array} shifts - Array de escalas
 */
function saveShiftsForDay(day, shifts) {
  const allShifts = JSON.parse(localStorage.getItem(SHIFTS_STORAGE_KEY) || '{}');
  const dateKey = `${currentPlannerYear}-${String(currentPlannerMonth + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
  
  allShifts[dateKey] = shifts;
  localStorage.setItem(SHIFTS_STORAGE_KEY, JSON.stringify(allShifts));
}

/**
 * Salvar uma escala para um dia
 * @param {number} day - Dia do mês
 * @param {object} shift - Objeto da escala
 */
function saveShiftToDay(day, shift) {
  const dayShifts = getShiftsForDay(day);
  dayShifts.push(shift);
  saveShiftsForDay(day, dayShifts);
}

/**
 * Remover uma escala de um dia
 * @param {number} day - Dia do mês
 * @param {number} index - Índice da escala
 */
function removeShiftFromLocalStorage(day, index) {
  const dayShifts = getShiftsForDay(day);
  
  if (dayShifts && dayShifts[index]) {
    dayShifts.splice(index, 1);
    saveShiftsForDay(day, dayShifts);
  }
}

/**
 * Obter todos os funcionários
 * @returns {array} Array de funcionários
 */
function getAllEmployees() {
  const employees = JSON.parse(localStorage.getItem('employees') || '[]');
  return Array.isArray(employees) ? employees : [];
}

/**
 * Obter todas as escalas do mês
 * @returns {object} Objeto com todas as escalas do mês
 */
function getAllShiftsForMonth() {
  const allShifts = JSON.parse(localStorage.getItem(SHIFTS_STORAGE_KEY) || '{}');
  const monthKey = `${currentPlannerYear}-${String(currentPlannerMonth + 1).padStart(2, '0')}`;
  
  const monthShifts = {};
  Object.keys(allShifts).forEach(dateKey => {
    if (dateKey.startsWith(monthKey)) {
      monthShifts[dateKey] = allShifts[dateKey];
    }
  });
  
  return monthShifts;
}

/**
 * Exportar escalas como CSV
 * @returns {string} CSV formatado
 */
function exportShiftsAsCSV() {
  const monthShifts = getAllShiftsForMonth();
  const lines = ['Data,Funcionário,Turno,Hora'];
  
  Object.entries(monthShifts).forEach(([dateKey, shifts]) => {
    shifts.forEach(shift => {
      const config = getShiftConfig(shift.shiftType);
      lines.push(`${dateKey},${shift.employeeName},${config.label},${config.time}`);
    });
  });
  
  return lines.join('\n');
}

/**
 * Duplicar escalas de um dia para outro
 * @param {number} sourceDay - Dia de origem
 * @param {number} targetDay - Dia de destino
 */
function duplicateDayShifts(sourceDay, targetDay) {
  const sourceShifts = getShiftsForDay(sourceDay);
  
  if (!sourceShifts || sourceShifts.length === 0) {
    showNotification('Nenhuma escala para duplicar', 'warning');
    return;
  }
  
  const targetShifts = getShiftsForDay(targetDay);
  
  sourceShifts.forEach(shift => {
    // Verificar se funcionário não está duplicado
    if (!targetShifts.some(s => s.employeeName === shift.employeeName)) {
      const newShift = {
        ...shift,
        date: `${currentPlannerYear}-${String(currentPlannerMonth + 1).padStart(2, '0')}-${String(targetDay).padStart(2, '0')}`,
        timestamp: new Date().toISOString()
      };
      targetShifts.push(newShift);
    }
  });
  
  saveShiftsForDay(targetDay, targetShifts);
  renderDayShifts(targetDay, document.getElementById(`shifts-container-${targetDay}`));
  
  showNotification(`✅ Escalas duplicadas para dia ${targetDay}`, 'success');
}

/**
 * Limpar todas as escalas do mês
 */
function clearMonthShifts() {
  if (!confirm('Tem certeza que deseja remover TODAS as escalas do mês? Essa ação não pode ser desfeita.')) {
    return;
  }
  
  const allShifts = JSON.parse(localStorage.getItem(SHIFTS_STORAGE_KEY) || '{}');
  const monthKey = `${currentPlannerYear}-${String(currentPlannerMonth + 1).padStart(2, '0')}`;
  
  Object.keys(allShifts).forEach(dateKey => {
    if (dateKey.startsWith(monthKey)) {
      delete allShifts[dateKey];
    }
  });
  
  localStorage.setItem(SHIFTS_STORAGE_KEY, JSON.stringify(allShifts));
  renderShiftCalendar();
  
  showNotification('🗑️ Todas as escalas do mês foram removidas', 'info');
}

/**
 * Obter resumo de escalas do funcionário no mês
 * @param {string} employeeName - Nome do funcionário
 * @returns {object} Resumo das escalas
 */
function getEmployeeMonthSummary(employeeName) {
  const monthShifts = getAllShiftsForMonth();
  const summary = {
    totalShifts: 0,
    byType: {},
    days: []
  };
  
  Object.entries(monthShifts).forEach(([dateKey, shifts]) => {
    const employeeShifts = shifts.filter(s => s.employeeName === employeeName);
    
    employeeShifts.forEach(shift => {
      summary.totalShifts++;
      summary.byType[shift.shiftType] = (summary.byType[shift.shiftType] || 0) + 1;
      summary.days.push(parseInt(dateKey.split('-')[2]));
    });
  });
  
  return summary;
}

/**
 * Validar conflitos de escalas (mesmo funcionário em múltiplos turnos no mesmo dia)
 * @returns {array} Array de conflitos encontrados
 */
function validateShiftConflicts() {
  const monthShifts = getAllShiftsForMonth();
  const conflicts = [];
  
  Object.entries(monthShifts).forEach(([dateKey, shifts]) => {
    const employeesByName = {};
    
    shifts.forEach(shift => {
      if (employeesByName[shift.employeeName]) {
        conflicts.push({
          date: dateKey,
          employee: shift.employeeName,
          shifts: [employeesByName[shift.employeeName], shift.shiftType]
        });
      }
      employeesByName[shift.employeeName] = shift.shiftType;
    });
  });
  
  return conflicts;
}
