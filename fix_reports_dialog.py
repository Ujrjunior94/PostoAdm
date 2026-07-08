import re
with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    text = f.read()

idx = text.find("Dialog(onDismissRequest = { showAddReportDialog = false }) {")
if idx != -1:
    text = text[:idx] + "if (showAddReportDialog) {\n" + text[idx:]

with open('app/src/main/java/com/example/ui/PostoUi.kt', 'w') as f:
    f.write(text)
