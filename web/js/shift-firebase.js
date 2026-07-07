/**
 * Integração com Firebase para sincronização de escalas
 * Salva e carrega escalas do banco de dados em tempo real
 */

/**
 * Carregar escalas do Firebase
 */
function loadShiftsFromFirebase() {
  if (!window.firebaseDatabase) {
    console.log('⚠️ Firebase não configurado, usando local storage');
    return;
  }
  
  try {
    const shiftsRef = window.firebaseDatabase.ref(`stations/${window.currentStationId}/shifts`);
    
    shiftsRef.once('value', (snapshot) => {
      const firebaseShifts = snapshot.val();
      
      if (firebaseShifts) {
        // Sincronizar com local storage
        const localShifts = JSON.parse(localStorage.getItem('postoadm_shifts') || '{}');
        const merged = { ...localShifts, ...firebaseShifts };
        localStorage.setItem('postoadm_shifts', JSON.stringify(merged));
        
        // Re-renderizar calendário
        renderShiftCalendar();
        
        console.log('✅ Shifts loaded from Firebase');
      }
    });
  } catch (error) {
    console.error('❌ Error loading shifts from Firebase:', error);
  }
}

/**
 * Salvar escala no Firebase
 * @param {object} shift - Objeto da escala
 */
function saveShiftToFirebase(shift) {
  if (!window.firebaseDatabase || !window.currentStationId) {
    console.log('⚠️ Firebase não configurado');
    return;
  }
  
  try {
    const dateKey = shift.date || `${currentPlannerYear}-${String(currentPlannerMonth + 1).padStart(2, '0')}-${String(selectedPlannerDay).padStart(2, '0')}`;
    const shiftRef = window.firebaseDatabase.ref(`stations/${window.currentStationId}/shifts/${dateKey}`);
    
    // Obter escalas existentes para o dia
    shiftRef.once('value', (snapshot) => {
      const dayShifts = snapshot.val() || [];
      
      // Adicionar nova escala
      dayShifts.push({
        ...shift,
        id: Date.now() + Math.random(),
        savedAt: new Date().toISOString()
      });
      
      // Salvar de volta
      shiftRef.set(dayShifts, (error) => {
        if (error) {
          console.error('❌ Error saving shift to Firebase:', error);
        } else {
          console.log('✅ Shift saved to Firebase');
        }
      });
    });
  } catch (error) {
    console.error('❌ Error in saveShiftToFirebase:', error);
  }
}

/**
 * Remover escala do Firebase
 * @param {number} day - Dia da escala
 * @param {number} index - Índice da escala
 */
function removeShiftFromFirebase(day, index) {
  if (!window.firebaseDatabase || !window.currentStationId) {
    console.log('⚠️ Firebase não configurado');
    return;
  }
  
  try {
    const dateKey = `${currentPlannerYear}-${String(currentPlannerMonth + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
    const shiftRef = window.firebaseDatabase.ref(`stations/${window.currentStationId}/shifts/${dateKey}`);
    
    shiftRef.once('value', (snapshot) => {
      const dayShifts = snapshot.val() || [];
      
      // Remover escala no índice
      if (dayShifts[index]) {
        dayShifts.splice(index, 1);
        
        if (dayShifts.length === 0) {
          // Se não houver mais escalas, remover o dia
          shiftRef.remove((error) => {
            if (!error) {
              console.log('✅ Day shifts removed from Firebase');
            }
          });
        } else {
          // Salvar com escalas atualizadas
          shiftRef.set(dayShifts, (error) => {
            if (!error) {
              console.log('✅ Shift removed from Firebase');
            }
          });
        }
      }
    });
  } catch (error) {
    console.error('❌ Error in removeShiftFromFirebase:', error);
  }
}

/**
 * Sincronizar todas as escalas do mês com Firebase
 */
function syncAllShiftsWithFirebase() {
  if (!window.firebaseDatabase || !window.currentStationId) {
    console.log('⚠️ Firebase não configurado');
    return;
  }
  
  const monthShifts = getAllShiftsForMonth();
  const stationShiftsRef = window.firebaseDatabase.ref(`stations/${window.currentStationId}/shifts`);
  
  stationShiftsRef.set(monthShifts, (error) => {
    if (error) {
      console.error('❌ Error syncing shifts:', error);
      showNotification('Erro ao sincronizar escalas', 'error');
    } else {
      console.log('✅ All shifts synced with Firebase');
      showNotification('✅ Escalas sincronizadas com Firebase', 'success');
    }
  });
}

/**
 * Escutar mudanças em tempo real
 */
function listenForShiftChanges() {
  if (!window.firebaseDatabase || !window.currentStationId) {
    return;
  }
  
  const shiftsRef = window.firebaseDatabase.ref(`stations/${window.currentStationId}/shifts`);
  
  shiftsRef.on('child_changed', (snapshot) => {
    const dateKey = snapshot.key;
    const shifts = snapshot.val();
    
    // Atualizar local storage
    const allShifts = JSON.parse(localStorage.getItem('postoadm_shifts') || '{}');
    allShifts[dateKey] = shifts;
    localStorage.setItem('postoadm_shifts', JSON.stringify(allShifts));
    
    // Re-renderizar se for do mês atual
    const [year, month] = dateKey.split('-');
    if (parseInt(year) === currentPlannerYear && parseInt(month) === currentPlannerMonth + 1) {
      const day = parseInt(dateKey.split('-')[2]);
      const shiftsContainer = document.getElementById(`shifts-container-${day}`);
      if (shiftsContainer) {
        renderDayShifts(day, shiftsContainer);
      }
    }
    
    console.log('🔄 Shift updated from Firebase:', dateKey);
  });
  
  shiftsRef.on('child_added', (snapshot) => {
    const dateKey = snapshot.key;
    const shifts = snapshot.val();
    
    // Atualizar local storage
    const allShifts = JSON.parse(localStorage.getItem('postoadm_shifts') || '{}');
    if (!allShifts[dateKey]) {
      allShifts[dateKey] = shifts;
      localStorage.setItem('postoadm_shifts', JSON.stringify(allShifts));
      
      // Re-renderizar se for do mês atual
      const [year, month] = dateKey.split('-');
      if (parseInt(year) === currentPlannerYear && parseInt(month) === currentPlannerMonth + 1) {
        const day = parseInt(dateKey.split('-')[2]);
        const shiftsContainer = document.getElementById(`shifts-container-${day}`);
        if (shiftsContainer) {
          renderDayShifts(day, shiftsContainer);
        }
      }
    }
    
    console.log('➕ New shift added from Firebase:', dateKey);
  });
  
  shiftsRef.on('child_removed', (snapshot) => {
    const dateKey = snapshot.key;
    
    // Remover do local storage
    const allShifts = JSON.parse(localStorage.getItem('postoadm_shifts') || '{}');
    delete allShifts[dateKey];
    localStorage.setItem('postoadm_shifts', JSON.stringify(allShifts));
    
    // Re-renderizar se for do mês atual
    const [year, month] = dateKey.split('-');
    if (parseInt(year) === currentPlannerYear && parseInt(month) === currentPlannerMonth + 1) {
      const day = parseInt(dateKey.split('-')[2]);
      const shiftsContainer = document.getElementById(`shifts-container-${day}`);
      if (shiftsContainer) {
        renderDayShifts(day, shiftsContainer);
      }
    }
    
    console.log('➖ Shift removed from Firebase:', dateKey);
  });
}

/**
 * Parar de escutar mudanças em tempo real
 */
function stopListeningForShiftChanges() {
  if (!window.firebaseDatabase || !window.currentStationId) {
    return;
  }
  
  const shiftsRef = window.firebaseDatabase.ref(`stations/${window.currentStationId}/shifts`);
  shiftsRef.off();
  
  console.log('⏹️ Stopped listening for shift changes');
}
