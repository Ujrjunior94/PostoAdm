/**
 * Drag & Drop avançado e Touch para mobile
 * Suporta desktop e dispositivos móveis com gestos otimizados
 */

let draggedElement = null;
let touchStartX = 0;
let touchStartY = 0;
let longPressTimer = null;
let selectedShiftForMove = null;

/**
 * Inicializar handlers de drag & drop
 */
function initAdvancedDragDrop() {
  // Drag events globais
  document.addEventListener('dragstart', handleGlobalDragStart, true);
  document.addEventListener('dragend', handleGlobalDragEnd, true);
  document.addEventListener('dragover', handleGlobalDragOver, true);
  document.addEventListener('drop', handleGlobalDrop, true);
  
  // Touch events para mobile
  document.addEventListener('touchstart', handleTouchStart, true);
  document.addEventListener('touchmove', handleTouchMove, true);
  document.addEventListener('touchend', handleTouchEnd, true);
  
  console.log('✅ Advanced drag & drop initialized');
}

/**
 * ===== DESKTOP DRAG & DROP =====
 */

function handleGlobalDragStart(e) {
  if (e.target.classList.contains('draggable-employee')) {
    draggedElement = e.target;
    e.dataTransfer.effectAllowed = 'copy';
    e.dataTransfer.setData('text/html', e.target.innerHTML);
    e.target.classList.add('opacity-50', 'scale-95');
  }
  
  if (e.target.classList.contains('shift-badge')) {
    draggedElement = e.target;
    e.dataTransfer.effectAllowed = 'move';
    e.target.classList.add('opacity-50');
  }
}

function handleGlobalDragEnd(e) {
  if (draggedElement) {
    draggedElement.classList.remove('opacity-50', 'scale-95');
    draggedElement = null;
  }
}

function handleGlobalDragOver(e) {
  e.preventDefault();
  e.dataTransfer.dropEffect = 'copy';
  
  if (e.target.classList.contains('shift-day-cell')) {
    e.target.classList.add('bg-hdPrimaryContainer', 'border-hdPrimary', 'ring-2', 'ring-hdPrimary');
  }
}

function handleGlobalDrop(e) {
  e.preventDefault();
  e.stopPropagation();
  
  // Limpar estilos de drag over
  document.querySelectorAll('.shift-day-cell').forEach(el => {
    el.classList.remove('bg-hdPrimaryContainer', 'border-hdPrimary', 'ring-2', 'ring-hdPrimary');
  });
  
  const targetDay = e.target.closest('.shift-day-cell');
  if (!targetDay) return;
  
  const dayIndex = targetDay.dataset.day;
  
  // Se é um funcionário sendo arrastado
  if (draggedElement && draggedElement.classList.contains('draggable-employee')) {
    const employeeName = draggedElement.dataset.employeeName;
    const shiftType = document.getElementById('dragShiftType')?.value || 'Manhã (06h - 14h)';
    addShiftToDay(parseInt(dayIndex), employeeName, shiftType);
  }
  
  draggedElement = null;
}

/**
 * ===== MOBILE TOUCH HANDLERS =====
 */

function handleTouchStart(e) {
  touchStartX = e.touches[0].clientX;
  touchStartY = e.touches[0].clientY;
  
  // Detectar long press em funcionário
  if (e.target.closest('.draggable-employee')) {
    longPressTimer = setTimeout(() => {
      const employee = e.target.closest('.draggable-employee');
      if (employee) {
        selectEmployeeForMobileShift({
          id: employee.dataset.employeeId,
          name: employee.dataset.employeeName
        });
        playHapticFeedback();
      }
    }, 500);
  }
  
  // Detectar long press em badge de turno (para editar)
  if (e.target.closest('.shift-badge')) {
    longPressTimer = setTimeout(() => {
      const badge = e.target.closest('.shift-badge');
      if (badge) {
        const day = badge.dataset.day;
        const index = badge.dataset.shiftIndex;
        editShiftForDay(parseInt(day), parseInt(index));
        playHapticFeedback();
      }
    }, 500);
  }
}

function handleTouchMove(e) {
  // Cancelar long press se o usuário mover muito
  if (longPressTimer) {
    const distX = Math.abs(e.touches[0].clientX - touchStartX);
    const distY = Math.abs(e.touches[0].clientY - touchStartY);
    
    if (distX > 10 || distY > 10) {
      clearTimeout(longPressTimer);
      longPressTimer = null;
    }
  }
}

function handleTouchEnd(e) {
  if (longPressTimer) {
    clearTimeout(longPressTimer);
    longPressTimer = null;
  }
}

/**
 * Feedback háptico para mobile
 */
function playHapticFeedback() {
  if (navigator.vibrate) {
    navigator.vibrate(50);
  }
}

/**
 * Modo de seleção para mobile (touch)
 * Usuário toca 1x em funcionário, depois toca 1x em dia
 */
function enableMobileModeSelection() {
  const employeesList = document.getElementById('draggableEmployeesList');
  if (!employeesList) return;
  
  employeesList.querySelectorAll('.draggable-employee').forEach(employee => {
    employee.addEventListener('click', (e) => {
      e.preventDefault();
      const data = {
        id: employee.dataset.employeeId,
        name: employee.dataset.employeeName
      };
      
      if (mobileSelectedEmployee && mobileSelectedEmployee.id === data.id) {
        // Desselecionar
        mobileSelectedEmployee = null;
        employee.classList.remove('ring-2', 'ring-hdPrimary');
        showNotification('Seleção cancelada', 'info');
      } else {
        // Selecionar
        employeesList.querySelectorAll('.draggable-employee').forEach(el => {
          el.classList.remove('ring-2', 'ring-hdPrimary');
        });
        
        mobileSelectedEmployee = data;
        employee.classList.add('ring-2', 'ring-hdPrimary');
        showNotification(`Toque no dia para escalar ${data.name}`, 'info');
        playHapticFeedback();
      }
    });
  });
  
  // Eventos para dias
  const calendarGrid = document.getElementById('shiftCalendarGrid');
  if (!calendarGrid) return;
  
  calendarGrid.querySelectorAll('.shift-day-cell').forEach(dayCell => {
    dayCell.addEventListener('click', (e) => {
      const day = parseInt(dayCell.dataset.day);
      
      if (mobileSelectedEmployee) {
        const shiftType = document.getElementById('dragShiftType')?.value || 'Manhã (06h - 14h)';
        addShiftToDay(day, mobileSelectedEmployee.name, shiftType);
        
        // Desselecionar
        document.querySelectorAll('.draggable-employee').forEach(el => {
          el.classList.remove('ring-2', 'ring-hdPrimary');
        });
        mobileSelectedEmployee = null;
      } else {
        selectPlannerDay(day);
      }
    });
  });
}

/**
 * Detectar se é dispositivo mobile
 */
function isMobile() {
  return /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent) || window.innerWidth < 768;
}

/**
 * Adaptação automática da interface para mobile
 */
function adaptUIForDevice() {
  const isMobileDevice = isMobile();
  
  if (isMobileDevice) {
    // Desabilitar drag & drop visual em mobile
    document.querySelectorAll('.draggable-employee').forEach(el => {
      el.draggable = false;
      el.classList.add('cursor-pointer');
    });
    
    // Mostrar instruções mobile
    const instructions = document.querySelector('[data-mobile-instructions]');
    if (instructions) {
      instructions.classList.remove('hidden');
    }
    
    console.log('📱 Mobile UI adapted');
  } else {
    console.log('🖥️ Desktop UI');
  }
}

// Inicializar ao carregar
document.addEventListener('DOMContentLoaded', () => {
  initAdvancedDragDrop();
  adaptUIForDevice();
  enableMobileModeSelection();
  
  // Readaptar ao redimensionar
  window.addEventListener('resize', adaptUIForDevice);
});
