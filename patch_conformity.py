with open("web/index.html", "r") as f:
    content = f.read()

import re

# 1. Update addConformityModal form HTML
old_modal_html = """                <div class="grid grid-cols-2 gap-3">
                    <div>
                        <label class="block text-xs font-bold text-hdTextSecondary mb-1">Data do Teste</label>
                        <input type="date" id="confDateInput" required class="w-full bg-hdSurface border border-hdBorder rounded-xl px-3 py-2 text-xs">
                    </div>
                    <div>
                        <label class="block text-xs font-bold text-hdTextSecondary mb-1">Combustível</label>
                        <select id="confFuelSelect" class="w-full bg-hdSurface border border-hdBorder rounded-xl px-3 py-2 text-xs" onchange="toggleWaterPhaseInput(); updateConformityCalculator();">
                            <option value="Gasolina Comum">Gasolina Comum</option>
                            <option value="Gasolina Aditivada">Gasolina Aditivada</option>
                            <option value="Etanol Comum">Etanol Comum</option>
                            <option value="Diesel S10">Diesel S10</option>
                            <option value="Diesel S500">Diesel S500</option>
                        </select>
                    </div>
                </div>"""

new_modal_html = """                <div class="grid grid-cols-2 gap-3">
                    <div>
                        <label class="block text-xs font-bold text-hdTextSecondary mb-1">Data do Teste</label>
                        <input type="date" id="confDateInput" required class="w-full bg-hdSurface border border-hdBorder rounded-xl px-3 py-2 text-xs">
                    </div>
                    <div>
                        <label class="block text-xs font-bold text-hdTextSecondary mb-1">Combustível</label>
                        <select id="confFuelSelect" class="w-full bg-hdSurface border border-hdBorder rounded-xl px-3 py-2 text-xs" onchange="toggleWaterPhaseInput(); updateConformityCalculator();">
                            <option value="Gasolina Comum">Gasolina Comum</option>
                            <option value="Gasolina Aditivada">Gasolina Aditivada</option>
                            <option value="Etanol Comum">Etanol Comum</option>
                            <option value="Diesel S10">Diesel S10</option>
                            <option value="Diesel S500">Diesel S500</option>
                        </select>
                    </div>
                </div>
                
                <div class="border-t border-hdBorder/60 my-2 pt-2">
                    <label class="block text-xs font-bold text-hdTextSecondary mb-1">Vincular a Nota de Combustível (Opcional)</label>
                    <select id="confDeliverySelect" class="w-full bg-hdSurface border border-hdBorder rounded-xl px-3 py-2 text-xs">
                        <!-- Loaded dynamically via JavaScript -->
                    </select>
                </div>"""

content = content.replace(old_modal_html, new_modal_html)

# 2. Update openAddConformityModal to populate the delivery select
old_open = """        function openAddConformityModal() {
            document.getElementById('confDateInput').value = new Date().toISOString().split('T')[0];
            document.getElementById('addConformityModal').classList.remove('hidden');
            window.toggleWaterPhaseInput();
            window.updateConformityCalculator();
        }"""

new_open = """        function openAddConformityModal() {
            document.getElementById('confDateInput').value = new Date().toISOString().split('T')[0];
            
            const deliverySelect = document.getElementById('confDeliverySelect');
            if (deliverySelect) {
                let optionsHtml = '<option value="">-- Nenhuma Nota Vinculada --</option>';
                const delList = state.deliveries || [];
                delList.forEach(d => {
                    optionsHtml += `<option value="${d.id}">${d.invoiceNumber} - ${d.date} (${d.fuelType})</option>`;
                });
                deliverySelect.innerHTML = optionsHtml;
            }
            
            document.getElementById('addConformityModal').classList.remove('hidden');
            window.toggleWaterPhaseInput();
            window.updateConformityCalculator();
        }"""

content = content.replace(old_open, new_open)

# 3. Update submitAddConformity to save delivery ID
old_submit = """            const newConf = {
                id: Date.now(),
                date,
                fuelType,
                densityMeasured,
                temperature,
                ethanolPercent: ethanolVal,
                aspectColor,
                isConforme: validation.conforme,
                technicianName: technician,
                observation,
                stationCnpj: cnpj
            };

            if (!state.conformities) state.conformities = [];
            state.conformities.unshift(newConf);"""

new_submit = """            const deliveryIdVal = document.getElementById('confDeliverySelect')?.value;
            const deliveryId = deliveryIdVal ? parseInt(deliveryIdVal) : null;
            
            const newConf = {
                id: Date.now(),
                date,
                fuelType,
                densityMeasured,
                temperature,
                ethanolPercent: ethanolVal,
                aspectColor,
                isConforme: validation.conforme,
                technicianName: technician,
                observation,
                deliveryId: deliveryId,
                stationCnpj: cnpj
            };

            if (!state.conformities) state.conformities = [];
            state.conformities.unshift(newConf);
            
            // Also link the delivery back to this conformity
            if (deliveryId) {
                const linkedDelivery = (state.deliveries || []).find(d => d.id === deliveryId);
                if (linkedDelivery) {
                    linkedDelivery.conformityId = newConf.id;
                }
            }
            """

content = content.replace(old_submit, new_submit)

# 4. Update the conformity table header and rows to show linked Nota Fiscal
old_table_header = """                                            <th class="py-2.5 px-3">Data</th>
                                            <th class="py-2.5 px-3">Combustível</th>"""

new_table_header = """                                            <th class="py-2.5 px-3">Data</th>
                                            <th class="py-2.5 px-3">Combustível</th>
                                            <th class="py-2.5 px-3">Nota Fiscal</th>"""

content = content.replace(old_table_header, new_table_header)

old_table_row = """                        <td class="py-2.5 px-3 font-bold text-hdPrimary">${rec.fuelType}</td>
                        <td class="py-2.5 px-3 text-right">${rec.densityMeasured} / ${rec.temperature}ºC</td>"""

new_table_row = """                        <td class="py-2.5 px-3 font-bold text-hdPrimary">${rec.fuelType}</td>
                        <td class="py-2.5 px-3 text-hdTextSecondary">${rec.deliveryId ? ((state.deliveries || []).find(d => d.id === rec.deliveryId)?.invoiceNumber || '-') : '-'}</td>
                        <td class="py-2.5 px-3 text-right">${rec.densityMeasured} / ${rec.temperature}ºC</td>"""

content = content.replace(old_table_row, new_table_row)

old_col_span = "colspan=\"7\""
new_col_span = "colspan=\"8\""
content = content.replace("colspan=\"7\" class=\"py-4 text-center text-hdTextSecondary\">Sem análises de laboratório lançadas no período.</td></tr>",
                          "colspan=\"8\" class=\"py-4 text-center text-hdTextSecondary\">Sem análises de laboratório lançadas no período.</td></tr>")


# 5. Update generateQualityReport
old_pdf_head = "head: [['Data', 'Combustível', 'Densidade / Temp', 'Etanol %', 'Técnico', 'Status']],"
new_pdf_head = "head: [['Data', 'Combustível', 'Nota Fiscal', 'Densidade / Temp', 'Etanol %', 'Técnico', 'Status']],"

old_pdf_body = """            const tableData = selectedData.map(rec => [
                rec.date,
                rec.fuelType,
                `${rec.densityMeasured} / ${rec.temperature}ºC`,
                `${rec.ethanolPercent}%`,
                rec.technicianName,
                rec.isConforme ? 'Regular' : 'Irregular'
            ]);"""

new_pdf_body = """            const tableData = selectedData.map(rec => [
                rec.date,
                rec.fuelType,
                rec.deliveryId ? (state.deliveries.find(d => d.id === rec.deliveryId)?.invoiceNumber || '-') : '-',
                `${rec.densityMeasured} / ${rec.temperature}ºC`,
                `${rec.ethanolPercent}%`,
                rec.technicianName,
                rec.isConforme ? 'Regular' : 'Irregular'
            ]);"""

content = content.replace(old_pdf_head, new_pdf_head)
content = content.replace(old_pdf_body, new_pdf_body)


with open("web/index.html", "w") as f:
    f.write(content)

print("Patched conformity")
