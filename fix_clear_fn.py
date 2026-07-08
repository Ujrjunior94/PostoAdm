import re

with open("web/index.html", "r") as f:
    content = f.read()

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
                    state.lmc = [];
                } else if (type === 'nozzles') {
                    state.nozzles = [];
                } else if (type === 'compliance') {
                    state.conformities = [];
                    state.deliveries = [];
                    state.calibrations = [];
                } else if (type === 'systems') {
                    state.systemCredentials = [];
                } else if (type === 'calendar') {
                    state.appointments = [];
                }
                saveLocalDb();
                logActivity("DELETE", "Limpeza", `Os dados da aba ${type} foram apagados.`, "Alerta");
                renderWorkspace();
            }
        };

        window.switchTab ="""

content = content.replace("        window.switchTab =", clear_fn)

with open("web/index.html", "w") as f:
    f.write(content)

print("Injected clear function")
