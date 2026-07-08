with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    text = f.read()

start_calib = text.find("if (showCalibReportDialogInStock) {")
print(text[start_calib:start_calib+500])
