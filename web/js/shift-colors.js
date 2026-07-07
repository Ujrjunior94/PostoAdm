/**
 * Configuração de cores e estilos para os turnos
 * Padrão visual consistente com o design system do PostoAdmin
 */

const SHIFT_CONFIG = {
  shifts: {
    "Manhã (06h - 14h)": {
      label: "Manhã",
      abbr: "M",
      bg: "bg-blue-600",
      light: "bg-blue-100",
      text: "text-blue-700",
      border: "border-blue-300",
      emoji: "☀️",
      time: "06h - 14h"
    },
    "Tarde (14h - 22h)": {
      label: "Tarde",
      abbr: "T",
      bg: "bg-green-600",
      light: "bg-green-100",
      text: "text-green-700",
      border: "border-green-300",
      emoji: "⛅",
      time: "14h - 22h"
    },
    "Noite (22h - 06h)": {
      label: "Noite",
      abbr: "N",
      bg: "bg-red-600",
      light: "bg-red-100",
      text: "text-red-700",
      border: "border-red-300",
      emoji: "🌙",
      time: "22h - 06h"
    },
    "Horista (10h-18h)": {
      label: "Horista",
      abbr: "H",
      bg: "bg-fuchsia-600",
      light: "bg-fuchsia-100",
      text: "text-fuchsia-700",
      border: "border-fuchsia-300",
      emoji: "💜",
      time: "10h - 18h"
    },
    "Horista 2 (09h-18h)": {
      label: "Horista 2",
      abbr: "H2",
      bg: "bg-indigo-600",
      light: "bg-indigo-100",
      text: "text-indigo-700",
      border: "border-indigo-300",
      emoji: "💙",
      time: "09h - 18h"
    },
    "Folga Geral": {
      label: "Folga",
      abbr: "F",
      bg: "bg-gray-600",
      light: "bg-gray-100",
      text: "text-gray-700",
      border: "border-gray-300",
      emoji: "🟢",
      time: "Folga"
    },
    "Repouso": {
      label: "Repouso",
      abbr: "R",
      bg: "bg-amber-800",
      light: "bg-amber-100",
      text: "text-amber-700",
      border: "border-amber-300",
      emoji: "🟤",
      time: "Repouso"
    }
  }
};

/**
 * Obter configuração de um turno
 * @param {string} shiftName - Nome do turno
 * @returns {object} Configuração do turno
 */
function getShiftConfig(shiftName) {
  return SHIFT_CONFIG.shifts[shiftName] || SHIFT_CONFIG.shifts["Folga Geral"];
}

/**
 * Obter todas as opções de turno para select
 * @returns {array} Array de nomes de turnos
 */
function getAllShiftOptions() {
  return Object.keys(SHIFT_CONFIG.shifts);
}

/**
 * Gerar HTML para legenda de cores
 * @returns {string} HTML da legenda
 */
function generateShiftLegend() {
  const legendItems = Object.entries(SHIFT_CONFIG.shifts)
    .map(([shiftName, config]) => {
      return `
        <div class="flex items-center space-x-1.5">
          <span class="w-5 h-5 rounded-full ${config.bg} text-white text-[9px] font-black flex items-center justify-center shadow-sm">${config.abbr}</span>
          <span>(${config.label} ${config.time})</span>
        </div>
      `;
    })
    .join('');

  return legendItems;
}
