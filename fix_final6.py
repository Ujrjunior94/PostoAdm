with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    text = f.read()

# Remove the broken showQualReportDialog block at the end
idx = text.rfind("if (showQualReportDialog) {")
if idx != -1:
    text = text[:idx]

# Remove the broken showCalibReportDialogInStock block in AfericaoScreen
# Wait, AfericaoScreen is earlier in the file.
idx2 = text.find("if (showCalibReportDialogInStock) {", text.find("fun AfericaoScreen"))
if idx2 != -1:
    count = 0
    end_idx = idx2
    for i in range(idx2, len(text)):
        if text[i] == '{': count += 1
        elif text[i] == '}': count -= 1
        if count == 0 and i > idx2:
            end_idx = i + 1
            break
    text = text[:idx2] + text[end_idx:]

with open('app/src/main/java/com/example/ui/PostoUi.kt', 'w') as f:
    f.write(text)
