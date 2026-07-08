with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    text = f.read()

import re
match = re.search(r"                            Button\(\s*onClick = \{\s*val filtered = calibrations\.filter \{ it\.date == reportDate.*?\}\s*\}", text, re.DOTALL)
if match:
    text = text[:match.start()] + text[match.end():]
    print("Cleaned!")
else:
    print("Not found!")

with open('app/src/main/java/com/example/ui/PostoUi.kt', 'w') as f:
    f.write(text)
