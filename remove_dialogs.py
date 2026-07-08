with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    text = f.read()

import re
# Removing showCalibReportDialog logic block
match1 = re.search(r"if \(showCalibReportDialog\)\s*\{[\s\S]*?Dialog\(onDismissRequest = \{ showCalibReportDialog = false \}\) \{[\s\S]*?\}\s*\}\s*\}", text)
if match1:
    text = text[:match1.start()] + text[match1.end():]
    print("Removed showCalibReportDialog block.")

# Removing showQualReportDialog logic block
match2 = re.search(r"if \(showQualReportDialog\)\s*\{[\s\S]*?Dialog\(onDismissRequest = \{ showQualReportDialog = false \}\) \{[\s\S]*?\}\s*\}\s*\}", text)
if match2:
    text = text[:match2.start()] + text[match2.end():]
    print("Removed showQualReportDialog block.")

with open('app/src/main/java/com/example/ui/PostoUi.kt', 'w') as f:
    f.write(text)
