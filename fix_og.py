import re
with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    text = f.read()

text = text.replace("og(onDismissRequest = { showCalibReportDialogInStock = false }) {", "if (showCalibReportDialogInStock) {\n        Dialog(onDismissRequest = { showCalibReportDialogInStock = false }) {")
text = text.replace("}og(onDismissRequest = { showQualReportDialog = false }) {", "    }\n    if (showQualReportDialog) {\n        Dialog(onDismissRequest = { showQualReportDialog = false }) {")

with open('app/src/main/java/com/example/ui/PostoUi.kt', 'w') as f:
    f.write(text)
