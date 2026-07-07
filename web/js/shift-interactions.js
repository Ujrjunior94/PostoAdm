/**
 * Lógica de interações do planner de escalas
 * Drag & drop, mobile tap, adição/remoção de turnos
 */

let mobileSelectedEmployee = null;

/**
 * Handler para drop de funcionário em um dia
 * @param {DragEvent} e - Evento de drag
 * @param {number} day - Dia do drop
 */
function handleShiftDrop(e, day) {
  const shiftType = document.getElementById('dragShiftType')?.value || 'Manhã (06h - 14h)';
  
  // Verificar se é um funcionário sendo arrastado
  if (e.dataTransfer.types.includes('employeeData')) {
    const employeeData = JSON.parse(e.dataTransfer.getData('employeeData'));
    addShiftToDay(day, employeeData.name, shiftType);
    return;
  }
  
  // Verificar se é um turno sendo movido
  if (e.dataTransfer.types.includes('shiftData')) {
    const shiftData = JSON.parse(e.dataTransfer.getData('shiftData'));
    
    // Se for o mesmo dia, ignorar
    if (shiftData.sourceDay === day) return;
    
    // Mover turno de um dia para outro
    moveShiftBetweenDays(shiftData.sourceDay, shiftData.sourceIndex, day, shiftData.shiftType);
  }
}

/**
 * Adicionar escala a um dia
 * @param {number} day - Dia do mês
 * @param {string} employeeName - Nome do funcionário
 * @param {string} shiftType - Tipo de turno
 */
function addShiftToDay(day, employeeName, shiftType) {
  const dayShifts = getShiftsForDay(day);
  
  // Verificar se o funcionário já está escalado neste dia
  if (dayShifts && dayShifts.some(s => s.employeeName === employeeName)) {
    showNotification(`${employeeName} já está escalado neste dia`, 'warning');
    return;
  }
  
  // Adicionar nova escala
  const newShift = {
    employeeName,
    shiftType,
    date: `${currentPlannerYear}-${String(currentPlannerMonth + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`,
    timestamp: new Date().toISOString()
  };
  
  // Salvar no estado local
  saveShiftToDay(day, newShift);
  
  // Atualizar UI
  const shiftsContainer = document.getElementById(`shifts-container-${day}`);
  if (shiftsContainer) {
    renderDayShifts(day, shiftsContainer);
  }
  
  // Atualizar painel lateral
  if (selectedPlannerDay === day) {
    updateSelectedDayPanel(day);
  }
  
  // Salvar no Firebase
  saveShiftToFirebase(newShift);
  
  showNotification(`✅ ${employeeName} escalado para ${getShiftConfig(shiftType).label}`, 'success');
  
  console.log(`➕ Shift added: ${employeeName} on day ${day}`);
}

/**
 * Remover escala de um dia
 * @param {number} day - Dia do mês
 * @param {number} index - Índice da escala
 */
function removeShiftFromDay(day, index) {
  const dayShifts = getShiftsForDay(day);
  
  if (!dayShifts || !dayShifts[index]) return;
  
  const shift = dayShifts[index];
  
  // Confirmar antes de remover
  if (!confirm(`Remover ${shift.employeeName} da escala de ${day}?`)) {
    return;
  }
  
  // Remover do estado local
  removeShiftFromLocalStorage(day, index);
  
  // Atualizar UI
  const shiftsContainer = document.getElementById(`shifts-container-${day}`);
  if (shiftsContainer) {
    renderDayShifts(day, shiftsContainer);
  }
  
  // Atualizar painel lateral
  if (selectedPlannerDay === day) {
    updateSelectedDayPanel(day);
  }
  
  // Remover do Firebase
  removeShiftFromFirebase(day, index);
  
  showNotification(`🗑️ ${shift.employeeName} removido da escala`, 'info');
  
  console.log(`➖ Shift removed: ${shift.employeeName} from day ${day}`);
}

/**
 * Mover turno entre dias
 * @param {number} sourceDay - Dia de origem
 * @param {number} sourceIndex - Índice na origem
 * @param {number} targetDay - Dia de destino
 * @param {string} shiftType - Tipo de turno
 */
function moveShiftBetweenDays(sourceDay, sourceIndex, targetDay, shiftType) {
  const sourceShifts = getShiftsForDay(sourceDay);
  
  if (!sourceShifts || !sourceShifts[sourceIndex]) return;
  
  const shift = sourceShifts[sourceIndex];
  
  // Remover da origem
  removeShiftFromLocalStorage(sourceDay, sourceIndex);
  
  // Adicionar no destino
  const newShift = {
    ...shift,
    date: `${currentPlannerYear}-${String(currentPlannerMonth + 1).padStart(2, '0')}-${String(targetDay).padStart(2, '0')}`
  };
  saveShiftToDay(targetDay, newShift);
  
  // Atualizar UI
  renderDayShifts(sourceDay, document.getElementById(`shifts-container-${sourceDay}`));
  renderDayShifts(targetDay, document.getElementById(`shifts-container-${targetDay}`));
  
  // Atualizar Firebase
  removeShiftFromFirebase(sourceDay, sourceIndex);
  saveShiftToFirebase(newShift);
  
  showNotification(`↪️ ${shift.employeeName} movido para dia ${targetDay}`, 'success');
  
  console.log(`↔️ Shift moved: ${shift.employeeName} from day ${sourceDay} to ${targetDay}`);
}

/**
 * Selecionar funcionário para mobile (sem drag & drop)
 * @param {object} employee - Dados do funcionário
 */
function selectEmployeeForMobileShift(employee) {
  mobileSelectedEmployee = employee;
  showNotification(`Toque no dia para escalar ${employee.name}`, 'info');
}

/**
 * Handler de toque em dia para mobile
 * @param {number} day - Dia tocado
 */
function handleMobileDayTap(day) {
  if (!mobileSelectedEmployee) {
    selectPlannerDay(day);
    return;
  }
  
  const shiftType = document.getElementById('dragShiftType')?.value || 'Manhã (06h - 14h)';
  addShiftToDay(day, mobileSelectedEmployee.name, shiftType);
  mobileSelectedEmployee = null;
}

/**
 * Abrir modal para adicionar escala a um dia selecionado
 */
function openAddShiftForSelectedDay() {
  const shiftType = document.getElementById('dragShiftType')?.value || 'Manhã (06h - 14h)';
  const employees = getAllEmployees();
  
  if (!employees || employees.length === 0) {
    showNotification('Nenhum funcionário cadastrado', 'warning');
    return;
  }
  
  // Criar modal dinamicamente
  const modal = document.createElement('div');
  modal.className = 'fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4';
  modal.id = 'addShiftModal';
  
  modal.innerHTML = `
    <div class="bg-white rounded-3xl p-6 shadow-xl max-w-md w-full">
      <div class="flex justify-between items-center mb-4 border-b border-hdBorder pb-3">
        <h3 class="text-lg font-bold text-hdTextPrimary">Escalar Funcionário - Dia ${selectedPlannerDay}</h3>
        <button onclick="document.getElementById('addShiftModal').remove()" class="text-hdTextSecondary hover:text-hdTextPrimary">
          <span class="material-symbols-outlined">close</span>
        </button>
      </div>
      
      <div class="space-y-4">
        <!-- Funcionário -->
        <div>
          <label class="block text-xs font-bold text-hdTextSecondary mb-2">Funcionário</label>
          <select id="modalEmployeeSelect" class="w-full bg-hdSurface border border-hdBorder rounded-xl px-3 py-2.5 text-sm font-medium focus:outline-none focus:border-hdPrimary">
            ${employees.map(emp => `<option value="${emp.id}">${emp.name}</option>`).join('')}
          </select>
        </div>
        
        <!-- Turno -->
        <div>
          <label class="block text-xs font-bold text-hdTextSecondary mb-2">Turno</label>
          <select id="modalShiftSelect" class="w-full bg-hdSurface border border-hdBorder rounded-xl px-3 py-2.5 text-sm font-medium focus:outline-none focus:border-hdPrimary">
            ${getAllShiftOptions().map(shift => `<option value="${shift}">${shift}</option>`).join('')}
          </select>
        </div>
        
        <!-- Botões -->
        <div class="flex gap-3 pt-4">
          <button onclick="document.getElementById('addShiftModal').remove()" class="flex-1 border border-hdBorder text-hdTextPrimary font-bold py-2.5 rounded-xl hover:bg-hdSurface transition">
            Cancelar
          </button>
          <button onclick="confirmAddShiftModal(${selectedPlannerDay})" class="flex-1 bg-hdPrimary text-white font-bold py-2.5 rounded-xl hover:bg-blue-700 transition">
            Escalar
          </button>
        </div>
      </div>
    </div>
  `;
  
  document.body.appendChild(modal);
  modal.onclick = (e) => {
    if (e.target === modal) modal.remove();
  };
}

/**
 * Confirmar adição de escala no modal
 * @param {number} day - Dia selecionado
 */
function confirmAddShiftModal(day) {
  const employeeSelect = document.getElementById('modalEmployeeSelect');
  const shiftSelect = document.getElementById('modalShiftSelect');
  const modal = document.getElementById('addShiftModal');
  
  if (!employeeSelect || !shiftSelect) return;
  
  const selectedEmployee = Array.from(getAllEmployees()).find(e => e.id === employeeSelect.value);
  const selectedShift = shiftSelect.value;
  
  if (!selectedEmployee) {
    showNotification('Selecione um funcionário', 'warning');
    return;
  }
  
  addShiftToDay(day, selectedEmployee.name, selectedShift);
  modal.remove();
}

/**
 * Mostrar notificação na tela
 * @param {string} message - Mensagem a exibir
 * @param {string} type - Tipo: 'success', 'warning', 'error', 'info'
 */
function showNotification(message, type = 'info') {
  const notification = document.createElement('div');
  notification.className = `fixed top-4 right-4 px-6 py-3 rounded-xl shadow-lg text-white font-semibold text-sm z-50 animate-pulse`;
  
  const bgColor = {
    success: 'bg-hdGreen',
    warning: 'bg-yellow-600',
    error: 'bg-hdRed',
    info: 'bg-hdPrimary'
  }[type] || 'bg-hdPrimary';
  
  notification.className += ` ${bgColor}`;
  notification.textContent = message;
  
  document.body.appendChild(notification);
  
  setTimeout(() => {
    notification.remove();
  }, 3000);
}

/**
 * Editar uma escala existente
 * @param {number} day - Dia da escala
 * @param {number} index - Índice da escala
 */
function editShiftForDay(day, index) {
  const dayShifts = getShiftsForDay(day);
  
  if (!dayShifts || !dayShifts[index]) return;
  
  const shift = dayShifts[index];
  
  const modal = document.createElement('div');
  modal.className = 'fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4';
  modal.id = 'editShiftModal';
  
  modal.innerHTML = `
    <div class="bg-white rounded-3xl p-6 shadow-xl max-w-md w-full">
      <div class="flex justify-between items-center mb-4 border-b border-hdBorder pb-3">
        <h3 class="text-lg font-bold text-hdTextPrimary">Editar Escala</h3>
        <button onclick="document.getElementById('editShiftModal').remove()" class="text-hdTextSecondary hover:text-hdTextPrimary">
          <span class="material-symbols-outlined">close</span>
        </button>
      </div>
      
      <div class="space-y-4">
        <div class="bg-hdSurfaceContainer p-3 rounded-xl border border-hdBorder">
          <p class="text-xs text-hdTextSecondary mb-1">Funcionário</p>
          <p class="font-bold text-hdTextPrimary">${shift.employeeName}</p>
        </div>
        
        <div>
          <label class="block text-xs font-bold text-hdTextSecondary mb-2">Turno</label>
          <select id="editShiftSelect" class="w-full bg-hdSurface border border-hdBorder rounded-xl px-3 py-2.5 text-sm font-medium focus:outline-none focus:border-hdPrimary">
            ${getAllShiftOptions().map(shift => `<option value="${shift}" ${shift === shift.shiftType ? 'selected' : ''}>${shift}</option>`).join('')}
          </select>
        </div>
        
        <div class="flex gap-3 pt-4">
          <button onclick="document.getElementById('editShiftModal').remove()" class="flex-1 border border-hdBorder text-hdTextPrimary font-bold py-2.5 rounded-xl hover:bg-hdSurface transition">
            Cancelar
          </button>
          <button onclick="confirmEditShiftModal(${day}, ${index})" class="flex-1 bg-hdPrimary text-white font-bold py-2.5 rounded-xl hover:bg-blue-700 transition">
            Atualizar
          </button>
        </div>
      </div>
    </div>
  `;
  
  document.body.appendChild(modal);
  modal.onclick = (e) => {
    if (e.target === modal) modal.remove();
  };
}

/**
 * Confirmar edição de escala
 * @param {number} day - Dia da escala
 * @param {number} index - Índice da escala
 */
function confirmEditShiftModal(day, index) {
  const shiftSelect = document.getElementById('editShiftSelect');
  const modal = document.getElementById('editShiftModal');
  
  if (!shiftSelect) return;
  
  const newShiftType = shiftSelect.value;
  
  updateShiftType(day, index, newShiftType);
  modal.remove();
}

/**
 * Atualizar tipo de turno de uma escala
 * @param {number} day - Dia da escala
 * @param {number} index - Índice da escala
 * @param {string} newShiftType - Novo tipo de turno
 */
function updateShiftType(day, index, newShiftType) {
  const dayShifts = getShiftsForDay(day);
  
  if (!dayShifts || !dayShifts[index]) return;
  
  dayShifts[index].shiftType = newShiftType;
  
  // Salvar no estado local
  saveShiftsForDay(day, dayShifts);
  
  // Atualizar UI
  renderDayShifts(day, document.getElementById(`shifts-container-${day}`));
  
  if (selectedPlannerDay === day) {
    updateSelectedDayPanel(day);
  }
  
  // Salvar no Firebase
  saveShiftToFirebase(dayShifts[index]);
  
  showNotification(`✏️ Turno atualizado para ${getShiftConfig(newShiftType).label}`, 'success');
}
