with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    text = f.read()

start = text.find("fun AfericaoScreen")
end = text.find("fun AnaliseScreen", start)
if end == -1: end = len(text)

afericao_code = text[start:end]

print("showAddCalibDialog in AfericaoScreen:", "if (showAddCalibDialog)" in afericao_code)
print("showCalibReportDialogInStock in AfericaoScreen:", "if (showCalibReportDialogInStock)" in afericao_code)
