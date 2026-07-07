/**
 * Renderer para o calendário mensal de escalas
 * Responsável por gerar o HTML da grid de dias
 */

let currentPlannerMonth = new Date().getMonth();
let currentPlannerYear = new Date().getFullYear();
let selectedPlannerDay = new Date().getDate();

/**
 * Inicializar o planner de escalas
 */
function initShiftPlanner() {
  renderShiftCalendar();
  renderPlannerEmployeesList();
  updatePlannerGeneratedDate();
  loadShiftsFromFirebase();
  
  console.log('✅ Shift Planner initialized');
}

/**
 * Renderizar a grid do calendário mensal
 */
function renderShiftCalendar() {
  const calendarGrid = document.getElementById('shiftCalendarGrid');
  const weekdayHeaders = document.getElementById('shiftWeekdayHeadersGrid');
  
  if (!calendarGrid) return;
  
  calendarGrid.innerHTML = '';
  
  // Obter primeiro dia do mês e número de dias
  const firstDay = new Date(currentPlannerYear, currentPlannerMonth, 1).getDay();
  const daysInMonth = new Date(currentPlannerYear, currentPlannerMonth + 1, 0).getDate();
  
  // Renderizar células vazias antes do 1º dia
  for (let i = 0; i < firstDay; i++) {
    const emptyCell = document.createElement('div');
    emptyCell.className = 'bg-hdSurface/30 rounded-2xl p-2 md:p-3 min-h-[80px] md:min-h-[100px] border border-hdBorder/20';
    calendarGrid.appendChild(emptyCell);
  }
  
  // Renderizar dias do mês
  for (let day = 1; day <= daysInMonth; day++) {
    const dayCell = createShiftDayCell(day);
    calendarGrid.appendChild(dayCell);
  }
  
  console.log(`📅 Rendered ${daysInMonth} days for ${currentPlannerMonth + 1}/${currentPlannerYear}`);
}

/**
 * Criar célula de dia do calendário
 * @param {number} day - Dia do mês
 * @returns {HTMLElement} Elemento da célula do dia
 */
function createShiftDayCell(day) {
  const cellDiv = document.createElement('div');
  cellDiv.id = `shift-day-${day}`;
  cellDiv.className = `bg-white border border-hdBorder rounded-2xl p-2 md:p-3 min-h-[80px] md:min-h-[100px] flex flex-col cursor-pointer transition hover:shadow-md hover:border-hdPrimary/50`;
  
  // Adicionar evento de clique para selecionar dia
  cellDiv.onclick = () => selectPlannerDay(day);
  
  // Permitir drop de funcionários
  cellDiv.ondragover = (e) => {
    e.preventDefault();
    cellDiv.classList.add('bg-hdPrimaryContainer', 'border-hdPrimary');
  };
  
  cellDiv.ondragleave = () => {
    cellDiv.classList.remove('bg-hdPrimaryContainer', 'border-hdPrimary');
  };
  
  cellDiv.ondrop = (e) => {
    e.preventDefault();
    cellDiv.classList.remove('bg-hdPrimaryContainer', 'border-hdPrimary');
    handleShiftDrop(e, day);
  };
  
  // Header: Número do dia
  const dayHeader = document.createElement('div');
  dayHeader.className = 'flex justify-between items-start mb-2';
  
  const dayNumber = document.createElement('span');
  dayNumber.className = 'text-sm md:text-base font-black text-hdTextPrimary';
  dayNumber.textContent = day;
  
  // Indicador de dia selecionado
  const selectedIndicator = document.createElement('span');
  selectedIndicator.id = `day-indicator-${day}`;
  selectedIndicator.className = 'hidden w-2 h-2 rounded-full bg-hdPrimary';
  
  dayHeader.appendChild(dayNumber);
  dayHeader.appendChild(selectedIndicator);
  cellDiv.appendChild(dayHeader);
  
  // Container para turnos
  const shiftsContainer = document.createElement('div');
  shiftsContainer.id = `shifts-container-${day}`;
  shiftsContainer.className = 'flex-1 space-y-1 overflow-y-auto custom-scrollbar';
  shiftsContainer.dataset.day = day;
  
  cellDiv.appendChild(shiftsContainer);
  
  // Renderizar turnos do dia
  renderDayShifts(day, shiftsContainer);
  
  return cellDiv;
}

/**
 * Renderizar turnos de um dia específico
 * @param {number} day - Dia do mês
 * @param {HTMLElement} container - Container para renderizar
 */
function renderDayShifts(day, container) {
  if (!container) return;
  
  container.innerHTML = '';
  
  // Obter escalas do dia do banco de dados local/Firebase
  const dayShifts = getShiftsForDay(day);
  
  if (!dayShifts || dayShifts.length === 0) {
    const emptyText = document.createElement('p');
    emptyText.className = 'text-[9px] text-hdTextSecondary/50 italic px-1';
    emptyText.textContent = 'Sem escala';
    container.appendChild(emptyText);
    return;
  }
  
  dayShifts.forEach((shift, index) => {
    const shiftBadge = createShiftBadge(shift, day, index);
    container.appendChild(shiftBadge);
  });
}

/**
 * Criar badge de turno
 * @param {object} shift - Objeto com dados do turno
 * @param {number} day - Dia do mês
 * @param {number} index - Índice do turno no dia
 * @returns {HTMLElement} Badge do turno
 */
function createShiftBadge(shift, day, index) {
  const config = getShiftConfig(shift.shiftType);
  
  const badge = document.createElement('div');
  badge.id = `shift-badge-${day}-${index}`;
  badge.className = `${config.light} ${config.text} border ${config.border} rounded-lg px-2 py-1 text-[9px] font-bold flex items-center justify-between group cursor-pointer transition hover:shadow-md`;
  badge.draggable = true;
  badge.dataset.day = day;
  badge.dataset.shiftIndex = index;
  
  // Nome do funcionário + turno
  const labelDiv = document.createElement('div');
  labelDiv.className = 'flex-1 truncate';
  labelDiv.innerHTML = `<span>${config.abbr}</span> • <span class="truncate">${shift.employeeName}</span>`;
  
  badge.appendChild(labelDiv);
  
  // Botão de remover (hidden até hover)
  const removeBtn = document.createElement('button');
  removeBtn.className = 'hidden group-hover:flex ml-1 items-center justify-center w-4 h-4 rounded hover:bg-red-200 transition';
  removeBtn.onclick = (e) => {
    e.stopPropagation();
    removeShiftFromDay(day, index);
  };
  removeBtn.innerHTML = '<span class="material-symbols-outlined text-xs">close</span>';
  
  badge.appendChild(removeBtn);
  
  // Drag event handlers
  badge.ondragstart = (e) => {
    e.dataTransfer.effectAllowed = 'move';
    e.dataTransfer.setData('shiftData', JSON.stringify({
      employeeName: shift.employeeName,
      shiftType: shift.shiftType,
      sourceDay: day,
      sourceIndex: index
    }));
    badge.classList.add('opacity-50');
  };
  
  badge.ondragend = () => {
    badge.classList.remove('opacity-50');
  };
  
  return badge;
}

/**
 * Renderizar lista de funcionários para drag
 */
function renderPlannerEmployeesList() {
  const employeesList = document.getElementById('draggableEmployeesList');
  
  if (!employeesList) return;
  
  employeesList.innerHTML = '';
  
  // Obter lista de funcionários do Firebase
  const employees = getAllEmployees();
  
  if (!employees || employees.length === 0) {
    const emptyMsg = document.createElement('p');
    emptyMsg.className = 'text-xs text-hdTextSecondary italic py-4 text-center';
    emptyMsg.textContent = 'Nenhum funcionário cadastrado';
    employeesList.appendChild(emptyMsg);
    return;
  }
  
  employees.forEach(employee => {
    const employeeBadge = createDraggableEmployeeBadge(employee);
    employeesList.appendChild(employeeBadge);
  });
}

/**
 * Criar badge de funcionário para drag
 * @param {object} employee - Dados do funcionário
 * @returns {HTMLElement} Badge draggable
 */
function createDraggableEmployeeBadge(employee) {
  const badge = document.createElement('div');
  badge.className = 'bg-hdPrimaryContainer border border-hdPrimary rounded-xl px-3 py-2 text-xs font-semibold text-hdPrimary cursor-move hover:bg-hdPrimary hover:text-white transition';
  badge.draggable = true;
  badge.dataset.employeeId = employee.id;
  badge.dataset.employeeName = employee.name;
  
  const nameSpan = document.createElement('span');
  nameSpan.className = 'truncate block';
  nameSpan.textContent = employee.name;
  
  const roleSpan = document.createElement('span');
  roleSpan.className = 'text-[10px] opacity-75 block truncate';
  roleSpan.textContent = employee.role || 'Frentista';
  
  badge.appendChild(nameSpan);
  badge.appendChild(roleSpan);
  
  // Drag event
  badge.ondragstart = (e) => {
    e.dataTransfer.effectAllowed = 'copy';
    e.dataTransfer.setData('employeeData', JSON.stringify({
      id: employee.id,
      name: employee.name,
      role: employee.role
    }));
  };
  
  // Mobile tap handler
  badge.onclick = (e) => {
    if (isMobileDevice()) {
      selectEmployeeForMobileShift(employee);
    }
  };
  
  return badge;
}

/**
 * Selecionar um dia do calendário
 * @param {number} day - Dia selecionado
 */
function selectPlannerDay(day) {
  selectedPlannerDay = day;
  
  // Atualizar visual dos indicadores
  document.querySelectorAll('[id^="day-indicator-"]').forEach(el => {
    el.classList.add('hidden');
  });
  const indicator = document.getElementById(`day-indicator-${day}`);
  if (indicator) indicator.classList.remove('hidden');
  
  // Atualizar painel lateral com detalhes do dia
  updateSelectedDayPanel(day);
  
  console.log(`📍 Selected day: ${day}`);
}

/**
 * Atualizar painel de detalhes do dia selecionado
 * @param {number} day - Dia selecionado
 */
function updateSelectedDayPanel(day) {
  const titleEl = document.getElementById('calendarSelectedDayTitle');
  const listEl = document.getElementById('calendarSelectedDayList');
  
  if (!titleEl || !listEl) return;
  
  // Atualizar título
  const monthNames = ['Janeiro', 'Fevereiro', 'Março', 'Abril', 'Maio', 'Junho', 
                      'Julho', 'Agosto', 'Setembro', 'Outubro', 'Novembro', 'Dezembro'];
  const monthName = monthNames[currentPlannerMonth];
  titleEl.textContent = `Plantonistas de ${day} de ${monthName}`;
  
  // Limpar e renderizar escalas do dia
  listEl.innerHTML = '';
  
  const dayShifts = getShiftsForDay(day);
  
  if (!dayShifts || dayShifts.length === 0) {
    const emptyMsg = document.createElement('div');
    emptyMsg.className = 'col-span-3 text-center text-hdTextSecondary text-xs py-6';
    emptyMsg.textContent = 'Nenhuma escala para este dia';
    listEl.appendChild(emptyMsg);
    return;
  }
  
  dayShifts.forEach((shift, index) => {
    const config = getShiftConfig(shift.shiftType);
    const shiftCard = document.createElement('div');
    shiftCard.className = `${config.light} ${config.text} border ${config.border} rounded-xl p-3 flex flex-col gap-2 group`;
    
    shiftCard.innerHTML = `
      <div class="flex justify-between items-start">
        <div>
          <p class="font-bold text-sm">${shift.employeeName}</p>
          <p class="text-[10px] opacity-75">${config.emoji} ${config.label}</p>
        </div>
        <button onclick="removeShiftFromDay(${day}, ${index})" class="opacity-0 group-hover:opacity-100 transition p-1 hover:bg-red-200 rounded">
          <span class="material-symbols-outlined text-sm">close</span>
        </button>
      </div>
    `;
    
    listEl.appendChild(shiftCard);
  });
}

/**
 * Atualizar data de geração do planner
 */
function updatePlannerGeneratedDate() {
  const dateEl = document.getElementById('plannerGeneratedDate');
  if (!dateEl) return;
  
  const now = new Date();
  const formatted = `${String(now.getDate()).padStart(2, '0')}/${String(now.getMonth() + 1).padStart(2, '0')}/${now.getFullYear()}`;
  dateEl.textContent = `Gerado: ${formatted}`;
}

/**
 * Navegar para o mês anterior
 */
function prevPlannerMonth() {
  currentPlannerMonth--;
  if (currentPlannerMonth < 0) {
    currentPlannerMonth = 11;
    currentPlannerYear--;
  }
  updatePlannerMonth();
}

/**
 * Navegar para o próximo mês
 */
function nextPlannerMonth() {
  currentPlannerMonth++;
  if (currentPlannerMonth > 11) {
    currentPlannerMonth = 0;
    currentPlannerYear++;
  }
  updatePlannerMonth();
}

/**
 * Atualizar o mês do planner
 */
function updatePlannerMonth() {
  // Atualizar select
  const monthSelect = document.getElementById('plannerMonthSelect');
  if (monthSelect) {
    monthSelect.value = '0'; // Reset para mês atual na lista
  }
  
  // Rerender calendário
  renderShiftCalendar();
  selectedPlannerDay = 1;
  updateSelectedDayPanel(1);
}

/**
 * Handler de mudança de mês via select
 * @param {string} monthOffset - Offset relativo do mês (0 = atual, 1 = próximo, etc)
 */
function onPlannerMonthChange(monthOffset) {
  const offset = parseInt(monthOffset);
  const today = new Date();
  
  currentPlannerMonth = today.getMonth() + offset;
  currentPlannerYear = today.getFullYear();
  
  if (currentPlannerMonth > 11) {
    currentPlannerMonth = currentPlannerMonth % 12;
    currentPlannerYear++;
  }
  
  updatePlannerMonth();
}

/**
 * Toggle da sidebar no planner
 */
function toggleShiftSidebar() {
  const sidebar = document.getElementById('shiftPlannerSidebar');
  const calendarCol = document.getElementById('shiftPlannerCalendarCol');
  const toggleBtn = document.getElementById('toggleShiftSidebarIcon');
  const toggleText = document.getElementById('toggleShiftSidebarText');
  
  if (!sidebar || !calendarCol) return;
  
  const isHidden = sidebar.classList.contains('hidden');
  
  if (isHidden) {
    // Mostrar sidebar
    sidebar.classList.remove('hidden');
    calendarCol.classList.remove('lg:col-span-4');
    calendarCol.classList.add('lg:col-span-3');
    toggleBtn.textContent = 'left_panel_close';
    toggleText.textContent = 'Ocultar Painel Lateral';
  } else {
    // Ocultar sidebar
    sidebar.classList.add('hidden');
    calendarCol.classList.add('lg:col-span-4');
    calendarCol.classList.remove('lg:col-span-3');
    toggleBtn.textContent = 'left_panel_open';
    toggleText.textContent = 'Mostrar Painel Lateral';
  }
}

/**
 * Verificar se é dispositivo mobile
 * @returns {boolean}
 */
function isMobileDevice() {
  return /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent);
}
