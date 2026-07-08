        function toggleSelectAllCals(source) {
            const checkboxes = document.querySelectorAll('.cal-checkbox');
            checkboxes.forEach(cb => cb.checked = source.checked);
        }

        function toggleSelectAllConfs(source) {
            const checkboxes = document.querySelectorAll('.conf-checkbox');
            checkboxes.forEach(cb => cb.checked = source.checked);
        }

        function generateCalibrationsReport() {
            const checkboxes = document.querySelectorAll('.cal-checkbox:checked');
            if (checkboxes.length === 0) {
                alert('Selecione pelo menos um registro de aferição para gerar o relatório.');
                return;
            }
            alert(`Gerando relatório PDF de ${checkboxes.length} aferição(ões) selecionada(s)... (simulação)`);
            // Aqui normalmente seria chamada uma biblioteca como jsPDF para gerar e baixar o arquivo.
        }

        function generateQualityReport() {
            const checkboxes = document.querySelectorAll('.conf-checkbox:checked');
            if (checkboxes.length === 0) {
                alert('Selecione pelo menos uma análise de qualidade para gerar o relatório.');
                return;
            }
            alert(`Gerando relatório PDF de ${checkboxes.length} análise(s) selecionada(s)... (simulação)`);
        }
        
        window.toggleSelectAllCals = toggleSelectAllCals;
        window.toggleSelectAllConfs = toggleSelectAllConfs;
        window.generateCalibrationsReport = generateCalibrationsReport;
        window.generateQualityReport = generateQualityReport;
