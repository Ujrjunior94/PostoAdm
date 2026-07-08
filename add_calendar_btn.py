import re

with open("web/index.html", "r") as f:
    content = f.read()

# Update clearTabData function
old_clear = """                } else if (type === 'systems') {
                    state.systemCredentials = [];
                }
                saveLocalDb();"""
new_clear = """                } else if (type === 'systems') {
                    state.systemCredentials = [];
                } else if (type === 'calendar') {
                    state.appointments = [];
                }
                saveLocalDb();"""
content = content.replace(old_clear, new_clear)

# Insert button in Calendar
cal_marker = """                        <div>
                            <h2 class="text-xl font-bold">Calendário Geral e Agendas de Compromissos 📅</h2>
                            <p class="text-xs text-hdTextSecondary">Visão unificada mensal de manutenções programadas, vistorias fiscais, entregas de combustíveis e escalas.</p>
                        </div>"""
cal_btn = """                        <div class="flex flex-col sm:flex-row justify-between w-full items-start sm:items-center gap-3">
                            <div>
                                <h2 class="text-xl font-bold">Calendário Geral e Agendas de Compromissos 📅</h2>
                                <p class="text-xs text-hdTextSecondary">Visão unificada mensal de manutenções programadas, vistorias fiscais, entregas de combustíveis e escalas.</p>
                            </div>
                            <button onclick="clearTabData('calendar')" class="bg-red-50 text-red-600 border border-red-200 font-bold px-4 py-2.5 rounded-xl hover:bg-red-100 transition flex items-center space-x-1.5 text-xs shrink-0 self-start sm:self-auto">
                                <span class="material-symbols-outlined text-sm font-bold">delete</span>
                                <span>Limpar Dados</span>
                            </button>
                        </div>"""

if cal_marker in content:
    content = content.replace(cal_marker, cal_btn)

with open("web/index.html", "w") as f:
    f.write(content)

print("Updated Calendar tab")
