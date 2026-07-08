with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    text = f.read()

def remove_blocks(text, marker):
    while True:
        idx = text.find(marker)
        if idx == -1: break
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

text = remove_blocks(text, "if (showCalibReportDialogInStock) {")
text = remove_blocks(text, "if (showQualReportDialog) {")

# Also delete any extra } at the end of the file.
text = text.rstrip()
while text.endswith("}") or text.endswith("@Composable"):
    if text.endswith("@Composable"): text = text[:-len("@Composable")].rstrip()
    if text.endswith("}"): text = text[:-1].rstrip()

with open('app/src/main/java/com/example/ui/PostoUi.kt', 'w') as f:
    f.write(text)

