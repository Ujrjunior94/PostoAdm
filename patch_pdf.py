with open("web/index.html", "r") as f:
    content = f.read()

import re

# 1. Update HTML table header
content = content.replace(
    '<th class="py-2.5 px-3 text-right">Erro %</th>\n                                            <th class="py-2.5 px-3">Laudo Inmetro</th>',
    '<th class="py-2.5 px-3 text-right">Erro %</th>\n                                            <th class="py-2.5 px-3">Laudo Inmetro</th>\n                                            <th class="py-2.5 px-3 text-right">Valor Aferição</th>'
)

# 2. Update render table logic
old_render = """                        <td class="py-2.5 px-3 text-hdTextSecondary">Laudo #${cal.laudo}</td>
                        <td class="py-2.5 px-3">
                            <span class="text-[10px] font-bold px-2 py-0.5 rounded-full ${cal.isConforme ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}">
                                ${cal.isConforme ? 'Aprovado' : 'Ajustar'}
                            </span>
                        </td>
                    </tr>
                `;
            });
            calibrationsTable.innerHTML = calHtml || '<tr><td colspan="7" class="py-4 text-center text-hdTextSecondary">Nenhum laudo lançado no período.</td></tr>';"""

new_render = """                        <td class="py-2.5 px-3 text-hdTextSecondary">Laudo #${cal.laudo}</td>
                        <td class="py-2.5 px-3 text-right font-semibold">R$ ${(cal.totalValue || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2 })}</td>
                        <td class="py-2.5 px-3">
                            <span class="text-[10px] font-bold px-2 py-0.5 rounded-full ${cal.isConforme ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}">
                                ${cal.isConforme ? 'Aprovado' : 'Ajustar'}
                            </span>
                        </td>
                    </tr>
                `;
            });
            
            if (filteredCalibrations.length > 0) {
                let totalLitros = filteredCalibrations.reduce((sum, cal) => sum + (cal.nominalVolume || 0), 0);
                let totalValor = filteredCalibrations.reduce((sum, cal) => sum + (cal.totalValue || 0), 0);
                calHtml += `
                    <tr class="bg-hdSurface font-bold text-xs border-t-2 border-hdBorder">
                        <td colspan="3" class="py-2.5 px-3 text-right">Total:</td>
                        <td class="py-2.5 px-3 text-right text-hdPrimary">${totalLitros.toLocaleString('pt-BR')} L</td>
                        <td colspan="2" class="py-2.5 px-3"></td>
                        <td class="py-2.5 px-3 text-right text-hdGreen">R$ ${totalValor.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}</td>
                        <td class="py-2.5 px-3"></td>
                    </tr>
                `;
            }
            
            calibrationsTable.innerHTML = calHtml || '<tr><td colspan="8" class="py-4 text-center text-hdTextSecondary">Nenhum laudo lançado no período.</td></tr>';"""

content = content.replace(old_render, new_render)

# 3. Update submitAddCalibration function
old_submit = """            const errorPercent = parseFloat(ANPUtils.calculateVolumeErrorPercent(deviationMl).toFixed(3));
            const inspector = document.getElementById('calInspectorInput').value;
            const laudo = document.getElementById('calLaudoInput').value;
            const cnpj = state.activeUser?.stationCnpj || "12.345.678/0001-99";

            const isConforme = ANPUtils.isVolumeErrorCompliant(deviationMl);

            const newCal = {
                id: Date.now(),
                date,
                referenceName,
                nominalVolume,
                measuredVolume,
                errorPercent,
                deviationMl,
                inspector,
                laudo,
                isConforme,
                stationCnpj: cnpj
            };"""

new_submit = """            const errorPercent = parseFloat(ANPUtils.calculateVolumeErrorPercent(deviationMl).toFixed(3));
            const inspector = document.getElementById('calInspectorInput').value;
            const laudo = document.getElementById('calLaudoInput').value;
            const cnpj = state.activeUser?.stationCnpj || "12.345.678/0001-99";

            const isConforme = ANPUtils.isVolumeErrorCompliant(deviationMl);
            
            let pricePerLiter = 5.799; // Default fallback
            const matchedNozzle = (state.nozzles || []).find(n => n.nozzleNumber === referenceName);
            if (matchedNozzle) {
                const matchedTank = (state.tanks || []).find(t => t.id === matchedNozzle.tankId);
                if (matchedTank) {
                    pricePerLiter = matchedTank.pricePerLiter;
                }
            }
            const totalValue = nominalVolume * pricePerLiter;

            const newCal = {
                id: Date.now(),
                date,
                referenceName,
                nominalVolume,
                measuredVolume,
                errorPercent,
                deviationMl,
                inspector,
                laudo,
                isConforme,
                pricePerLiter,
                totalValue,
                stationCnpj: cnpj
            };"""

content = content.replace(old_submit, new_submit)

# 4. Update the generateCalibrationsReport function
old_report = """            const tableData = selectedData.map(cal => [
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
            });"""

new_report = """            let totalLiters = 0;
            let totalValue = 0;
            
            const tableData = selectedData.map(cal => {
                totalLiters += (cal.nominalVolume || 0);
                totalValue += (cal.totalValue || 0);
                
                return [
                    cal.date,
                    cal.referenceName,
                    `${cal.nominalVolume}L / ${cal.measuredVolume}L`,
                    `${cal.errorPercent}%`,
                    cal.laudo,
                    `R$ ${(cal.totalValue || 0).toLocaleString('pt-BR', { minimumFractionDigits: 2 })}`,
                    cal.isConforme ? 'Aprovado' : 'Ajustar'
                ];
            });
            
            tableData.push([
                '',
                'TOTAL',
                `${totalLiters.toLocaleString('pt-BR')} L`,
                '',
                '',
                `R$ ${totalValue.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}`,
                ''
            ]);
            
            doc.autoTable({
                startY: 90,
                head: [['Data', 'Referência', 'Vol Nom/Med', 'Erro %', 'Laudo', 'Valor Aferição', 'Status']],
                body: tableData,
                styles: { fontSize: 8 },
                headStyles: { fillColor: [41, 128, 185] },
                didParseCell: function(data) {
                    // Make last row bold
                    if (data.row.index === tableData.length - 1) {
                        data.cell.styles.fontStyle = 'bold';
                        data.cell.styles.fillColor = [240, 240, 240];
                    }
                }
            });"""

content = content.replace(old_report, new_report)


with open("web/index.html", "w") as f:
    f.write(content)

print("Done")
