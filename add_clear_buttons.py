import re

with open("web/index.html", "r") as f:
    content = f.read()

# 1. Add the clearTabData function
clear_fn = """
        window.clearTabData = function(type) {
            if (confirm("Tem certeza que deseja limpar todos os dados desta seção? Esta ação não pode ser desfeita.")) {
                if (type === 'tanks') {
                    state.tanks = [];
                } else if (type === 'employees') {
                    state.employees = [];
                    state.shifts = [];
                } else if (type === 'reports') {
                    state.reports = [];
                    state.nozzleSales = [];
                } else if (type === 'lmc') {
                    state.lmcRecords = [];
                } else if (type === 'nozzles') {
                    state.nozzles = [];
                } else if (type === 'compliance') {
                    state.conformities = [];
                    state.deliveries = [];
                    state.calibrations = [];
                } else if (type === 'systems') {
                    state.systemCredentials = [];
                }
                saveLocalDb();
                logActivity("DELETE", "Limpeza", `Os dados da aba ${type} foram apagados.`, "Alerta");
                renderWorkspace();
            }
        };

        // UI View router (already exists, we insert before it)
"""

content = content.replace("        // UI View router", clear_fn)

def insert_button(content, marker, insert_text):
    if marker in content:
        return content.replace(marker, insert_text + "\n" + marker)
    return content

# Tanks
tanks_marker = """<button onclick="openAddTankModal()" class="bg-hdPrimary text-white font-bold px-4 py-2.5 rounded-xl hover:bg-blue-700 transition flex items-center space-x-1.5 self-start text-xs">"""
tanks_btn = """                        <div class="flex space-x-2 self-start">
                            <button onclick="clearTabData('tanks')" class="bg-red-50 text-red-600 border border-red-200 font-bold px-4 py-2.5 rounded-xl hover:bg-red-100 transition flex items-center space-x-1.5 text-xs">
                                <span class="material-symbols-outlined text-sm font-bold">delete</span>
                                <span>Limpar Dados</span>
                            </button>"""
if tanks_marker in content:
    content = content.replace(tanks_marker, tanks_btn + "\n                            " + tanks_marker.replace("self-start", "") + "\n                        </div>")

# Employees
emp_marker = """<button onclick="openAddEmployeeModal()" class="bg-hdPrimary text-white font-bold px-4 py-2.5 rounded-xl hover:bg-blue-700 transition flex items-center space-x-1.5 text-xs shrink-0">"""
emp_btn = """                        <div class="flex space-x-2">
                            <button onclick="clearTabData('employees')" class="bg-red-50 text-red-600 border border-red-200 font-bold px-4 py-2.5 rounded-xl hover:bg-red-100 transition flex items-center space-x-1.5 text-xs">
                                <span class="material-symbols-outlined text-sm font-bold">delete</span>
                                <span>Limpar Dados</span>
                            </button>"""
if emp_marker in content:
    content = content.replace(emp_marker, emp_btn + "\n                            " + emp_marker.replace("shrink-0", "") + "\n                        </div>")

# Reports
rep_marker = """<button onclick="openAddReportModal()" class="bg-hdPrimary text-white font-bold px-4 py-2.5 rounded-xl hover:bg-blue-700 transition flex items-center space-x-1.5 text-xs">"""
rep_btn = """                            <button onclick="clearTabData('reports')" class="bg-red-50 text-red-600 border border-red-200 font-bold px-4 py-2.5 rounded-xl hover:bg-red-100 transition flex items-center space-x-1.5 text-xs">
                                <span class="material-symbols-outlined text-sm font-bold">delete</span>
                                <span>Limpar Dados</span>
                            </button>"""
if rep_marker in content:
    content = content.replace(rep_marker, rep_btn + "\n                            " + rep_marker)


# LMC
lmc_marker = """<button onclick="openAddLmcModal()" class="bg-hdPrimary text-white font-bold px-4 py-2.5 rounded-xl hover:bg-blue-700 transition flex items-center space-x-1.5 text-xs">"""
lmc_btn = """                            <button onclick="clearTabData('lmc')" class="bg-red-50 text-red-600 border border-red-200 font-bold px-4 py-2.5 rounded-xl hover:bg-red-100 transition flex items-center space-x-1.5 text-xs">
                                <span class="material-symbols-outlined text-sm font-bold">delete</span>
                                <span>Limpar Dados</span>
                            </button>"""
if lmc_marker in content:
    content = content.replace(lmc_marker, lmc_btn + "\n                            " + lmc_marker)
else:
    # LMC has an export button, we should place the clear button next to it.
    lmc_export = """<button onclick="downloadLmcPDF()" class="bg-white border border-hdBorder text-hdTextPrimary font-bold px-4 py-2.5 rounded-xl hover:bg-hdSurfaceContainer transition flex items-center space-x-1.5 text-xs shadow-sm">"""
    lmc_btn2 = """                            <button onclick="clearTabData('lmc')" class="bg-red-50 text-red-600 border border-red-200 font-bold px-4 py-2.5 rounded-xl hover:bg-red-100 transition flex items-center space-x-1.5 text-xs">
                                <span class="material-symbols-outlined text-sm font-bold">delete</span>
                                <span>Limpar Dados</span>
                            </button>"""
    if lmc_export in content:
        content = content.replace(lmc_export, lmc_btn2 + "\n                            " + lmc_export)


# Nozzles
noz_marker = """<button onclick="openAddNozzleModal()" class="bg-hdPrimary text-white font-bold px-4 py-2.5 rounded-xl hover:bg-blue-700 transition flex items-center space-x-1.5 self-start text-xs">"""
noz_btn = """                        <div class="flex space-x-2 self-start">
                            <button onclick="clearTabData('nozzles')" class="bg-red-50 text-red-600 border border-red-200 font-bold px-4 py-2.5 rounded-xl hover:bg-red-100 transition flex items-center space-x-1.5 text-xs">
                                <span class="material-symbols-outlined text-sm font-bold">delete</span>
                                <span>Limpar Dados</span>
                            </button>"""
if noz_marker in content:
    content = content.replace(noz_marker, noz_btn + "\n                            " + noz_marker.replace("self-start", "") + "\n                        </div>")

# Systems
sys_marker = """<button onclick="openAddCredentialModal()" class="bg-hdPrimary text-white font-bold px-4 py-2.5 rounded-xl hover:bg-blue-700 transition flex items-center space-x-1.5 text-xs">"""
sys_btn = """                            <button onclick="clearTabData('systems')" class="bg-red-50 text-red-600 border border-red-200 font-bold px-4 py-2.5 rounded-xl hover:bg-red-100 transition flex items-center space-x-1.5 text-xs">
                                <span class="material-symbols-outlined text-sm font-bold">delete</span>
                                <span>Limpar Dados</span>
                            </button>"""
if sys_marker in content:
    content = content.replace(sys_marker, sys_btn + "\n                            " + sys_marker)

# Compliance (Qualidade)
# This tab has a tab-based layout. Let's add the button to the main header.
comp_marker = """<h2 class="text-xl font-bold">Controle de Qualidade & Aferições</h2>"""
comp_btn = """                        <div class="flex justify-between w-full sm:w-auto items-start sm:items-center">
                            <div>
                                <h2 class="text-xl font-bold">Controle de Qualidade & Aferições</h2>
                                <p class="text-xs text-hdTextSecondary">Gestão de aferições das bombas, testes de conformidade e entregas (LMC).</p>
                            </div>
                            <button onclick="clearTabData('compliance')" class="bg-red-50 text-red-600 border border-red-200 font-bold px-4 py-2.5 rounded-xl hover:bg-red-100 transition flex items-center space-x-1.5 text-xs self-start sm:self-auto">
                                <span class="material-symbols-outlined text-sm font-bold">delete</span>
                                <span>Limpar Dados</span>
                            </button>
                        </div>"""

if comp_marker in content:
    # Need to replace the whole div that contains the title.
    # Let's search for the block
    old_comp = """                        <div>
                            <h2 class="text-xl font-bold">Controle de Qualidade & Aferições</h2>
                            <p class="text-xs text-hdTextSecondary">Gestão de aferições das bombas, testes de conformidade e entregas (LMC).</p>
                        </div>"""
    content = content.replace(old_comp, comp_btn)


with open("web/index.html", "w") as f:
    f.write(content)

print("Added clear buttons")
