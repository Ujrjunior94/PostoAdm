with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    text = f.read()

idx = text.find("if (showCalibReportDialog) {")
print(text[idx:idx+2000])

