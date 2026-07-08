with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    text = f.read()

start = text.find("fun AfericaoScreen")
end = text.find("fun AnaliseScreen", start)
afericao_code = text[start:end]
print("Afericao Report Dialog:", "if (showCalibReportDialogInStock)" in afericao_code)

start = text.find("fun AnaliseScreen")
analise_code = text[start:]
print("Analise Report Dialog:", "if (showQualReportDialog)" in analise_code)
