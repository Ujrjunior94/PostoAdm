with open("web/index.html", "r") as f:
    content = f.read()

import re

new_generate_functions = """
        function generateCalibrationsReport() {
            const checkboxes = document.querySelectorAll('.cal-checkbox:checked');
            if (checkboxes.length === 0) {
                alert('Selecione pelo menos um registro de aferição para gerar o relatório.');
                return;
            }
            
            const selectedIds = Array.from(checkboxes).map(cb => parseInt(cb.value));
            // Use state from the window or just state if it's in scope, but actually state is passed to renderWorkspace.
            // Wait, state is defined in the script globally. Let's use global state.
            const selectedData = state.calibrations.filter(cal => selectedIds.includes(cal.id));
            
            const { jsPDF } = window.jspdf;
            const doc = new jsPDF('p', 'pt', 'a4');
            
            doc.setFontSize(16);
            doc.text("Relatório de Calibração (Aferição de Bombas)", 40, 40);
            
            doc.setFontSize(10);
            doc.text("Posto Administrativo - CNPJ: 12.345.678/0001-99", 40, 60);
            doc.text("Data de Emissão: " + new Date().toLocaleDateString('pt-BR'), 40, 75);
            
            const tableData = selectedData.map(cal => [
                cal.date,
                cal.referenceName,
                `${cal.nominalVolume}L / ${cal.measuredVolume}L`,
                `${cal.errorPercent}%`,
                cal.laudo,
                cal.isConforme ? 'Aprovado' : 'Ajustar'
            ]);
            
            doc.autoTable({
                startY: 90,
                head: [['Data', 'Referência', 'Vol Nom/Med', 'Erro %', 'Laudo', 'Status']],
                body: tableData,
                styles: { fontSize: 8 },
                headStyles: { fillColor: [41, 128, 185] },
            });
            
            doc.save('relatorio_calibracao.pdf');
        }

        function generateQualityReport() {
            const checkboxes = document.querySelectorAll('.conf-checkbox:checked');
            if (checkboxes.length === 0) {
                alert('Selecione pelo menos uma análise de qualidade para gerar o relatório.');
                return;
            }
            
            const selectedIds = Array.from(checkboxes).map(cb => parseInt(cb.value));
            const selectedData = state.conformities.filter(rec => selectedIds.includes(rec.id));
            
            const { jsPDF } = window.jspdf;
            const doc = new jsPDF('p', 'pt', 'a4');
            
            doc.setFontSize(16);
            doc.text("Relatório de Análise de Conformidade (Qualidade)", 40, 40);
            
            doc.setFontSize(10);
            doc.text("Posto Administrativo - CNPJ: 12.345.678/0001-99", 40, 60);
            doc.text("Data de Emissão: " + new Date().toLocaleDateString('pt-BR'), 40, 75);
            
            const tableData = selectedData.map(rec => [
                rec.date,
                rec.fuelType,
                `${rec.densityMeasured} / ${rec.temperature}ºC`,
                `${rec.ethanolPercent}%`,
                rec.technicianName,
                rec.isConforme ? 'Regular' : 'Irregular'
            ]);
            
            doc.autoTable({
                startY: 90,
                head: [['Data', 'Combustível', 'Densidade / Temp', 'Etanol %', 'Técnico', 'Status']],
                body: tableData,
                styles: { fontSize: 8 },
                headStyles: { fillColor: [39, 174, 96] },
            });
            
            doc.save('relatorio_qualidade.pdf');
        }
"""

old_start = "function generateCalibrationsReport() {"
old_end = "window.toggleSelectAllCals = toggleSelectAllCals;"

idx1 = content.find(old_start)
idx2 = content.find(old_end)

if idx1 != -1 and idx2 != -1:
    new_content = content[:idx1] + new_generate_functions.strip() + "\n\n        " + content[idx2:]
    with open("web/index.html", "w") as f:
        f.write(new_content)
    print("Replaced!")
else:
    print("Could not find boundaries")
