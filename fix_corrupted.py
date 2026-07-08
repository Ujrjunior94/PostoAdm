with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    text = f.read()

def remove_blocks(text, marker):
    while True:
        idx = text.find(marker)
        if idx == -1: break
        
        # delete until the matching brace
        count = 0
        end_idx = idx
        for i in range(idx, len(text)):
            if text[i] == '{': count += 1
            elif text[i] == '}': count -= 1
            if count == 0 and i > idx:
                end_idx = i + 1
                break
        
        text = text[:idx] + text[end_idx:]
    return text

text = remove_blocks(text, " (showCalibReportDialogInStock) {")
text = remove_blocks(text, " (showQualReportDialog) {")

with open('app/src/main/java/com/example/ui/PostoUi.kt', 'w') as f:
    f.write(text)
