with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    text = f.read()

import re

# Remove any remaining fragment of showCalibReportDialog
match = re.search(r"if \(showCalibReportDialog\)\s*\{[\s\S]*?Button\(\s*onClick = \{[\s\S]*?showCalibReportDialog = false[\s\S]*?\}\s*\)\s*\{[\s\S]*?\}\s*\}\s*\}\s*\}", text)
if match:
    text = text[:match.start()] + text[match.end():]
    print("Cleaned up dangling showCalibReportDialog.")
    
match2 = re.search(r"if \(showQualReportDialog\)\s*\{[\s\S]*?Button\(\s*onClick = \{[\s\S]*?showQualReportDialog = false[\s\S]*?\}\s*\)\s*\{[\s\S]*?\}\s*\}\s*\}\s*\}", text)
if match2:
    text = text[:match2.start()] + text[match2.end():]
    print("Cleaned up dangling showQualReportDialog.")

with open('app/src/main/java/com/example/ui/PostoUi.kt', 'w') as f:
    f.write(text)

