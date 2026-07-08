with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    text = f.read()

# Delete anything matching `alog(onDismissRequest...`
idx = text.find("alog(onDismissRequest = { showCalibReportDialogInStock = false }) {")
if idx != -1:
    count = 0
    end_idx = idx
    for i in range(idx, len(text)):
        if text[i] == '{': count += 1
        elif text[i] == '}': count -= 1
        if count == 0 and i > idx:
            end_idx = i + 1
            break
    text = text[:idx] + text[end_idx:]

idx = text.find("alog(onDismissRequest = { showQualReportDialog = false }) {")
if idx != -1:
    count = 0
    end_idx = idx
    for i in range(idx, len(text)):
        if text[i] == '{': count += 1
        elif text[i] == '}': count -= 1
        if count == 0 and i > idx:
            end_idx = i + 1
            break
    text = text[:idx] + text[end_idx:]

with open('app/src/main/java/com/example/ui/PostoUi.kt', 'w') as f:
    f.write(text)
