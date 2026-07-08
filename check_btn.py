with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    text = f.read()

start = text.find("fun AfericaoScreen")
end = text.find("fun AnaliseScreen", start)
afericao_code = text[start:end]

print("Afericao Btn: ", "showCalibReportDialogInStock = true" in afericao_code)

start = text.find("fun AnaliseScreen")
end = len(text)
analise_code = text[start:end]
print("Analise Btn: ", "showQualReportDialog = true" in analise_code)

