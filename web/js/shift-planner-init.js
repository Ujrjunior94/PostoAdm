/**
 * Inicialização do Shift Planner
 * Chamado ao abrir a aba de Equipe e Escalas
 */

function initializeShiftPlannerOnPageLoad() {
  // Aguardar o DOM estar pronto
  document.addEventListener('DOMContentLoaded', function() {
    console.log('🚀 Shift Planner initialized on page load');
  });
}

function activateShiftPlannerTab() {
  // Quando o usuário clica em "Equipe e Escalas"
  console.log('📅 Activating Shift Planner tab...');
  
  // Aguardar um pequeno delay para garantir que o DOM está pronto
  setTimeout(() => {
    try {
      initShiftPlanner();
      listenForShiftChanges();
      console.log('✅ Shift Planner active and listening for changes');
    } catch (error) {
      console.error('❌ Error activating shift planner:', error);
    }
  }, 100);
}

function deactivateShiftPlannerTab() {
  // Quando o usuário sai da aba de Equipe e Escalas
  console.log('⏹️ Deactivating Shift Planner tab...');
  stopListeningForShiftChanges();
}

// Exportar funções globalmente
window.initializeShiftPlannerOnPageLoad = initializeShiftPlannerOnPageLoad;
window.activateShiftPlannerTab = activateShiftPlannerTab;
window.deactivateShiftPlannerTab = deactivateShiftPlannerTab;
