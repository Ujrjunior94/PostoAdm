import re

with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    text = f.read()

# 1. Revert isDraggingThis
text = text.replace("val isDraggingThis = draggedScheduleId == sched.id", "")

# 2. Delete CalibReportDialogInStock block at 9425
idx = text.find("CalibReportDialogInStock) {")
if idx != -1:
    # Find the preceding '}'
    start_idx = text.rfind("}", 0, idx)
    if start_idx == -1: start_idx = idx
    else: start_idx += 1 # start after the '}'
    
    count = 0
    end_idx = idx
    for i in range(idx, len(text)):
        if text[i] == '{': count += 1
        elif text[i] == '}': count -= 1
        if count == 0 and i > idx:
            end_idx = i + 1
            break
    text = text[:start_idx] + text[end_idx:]

# 3. Delete QualReportDialog block at 9483
idx = text.find("QualReportDialog) {")
if idx != -1:
    start_idx = text.rfind("}", 0, idx)
    if start_idx == -1: start_idx = idx
    else: start_idx += 1
    
    count = 0
    end_idx = idx
    for i in range(idx, len(text)):
        if text[i] == '{': count += 1
        elif text[i] == '}': count -= 1
        if count == 0 and i > idx:
            end_idx = i + 1
            break
    text = text[:start_idx] + text[end_idx:]

# 4. Also delete the stray `alog) {` if it exists
idx = text.find("alog) {")
if idx != -1:
    start_idx = text.rfind("}", 0, idx)
    if start_idx == -1: start_idx = idx
    else: start_idx += 1
    
    count = 0
    end_idx = idx
    for i in range(idx, len(text)):
        if text[i] == '{': count += 1
        elif text[i] == '}': count -= 1
        if count == 0 and i > idx:
            end_idx = i + 1
            break
    text = text[:start_idx] + text[end_idx:]

with open('app/src/main/java/com/example/ui/PostoUi.kt', 'w') as f:
    f.write(text)

