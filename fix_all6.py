with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    text = f.read()

# fix owCalibReportDialogInStock
idx = text.find("owCalibReportDialogInStock)")
if idx != -1:
    start_idx = text.rfind("\n", 0, idx)
    if start_idx == -1: start_idx = idx
    count = 0
    end_idx = idx
    for i in range(idx, len(text)):
        if text[i] == '{': count += 1
        elif text[i] == '}': count -= 1
        if count == 0 and i > idx:
            end_idx = i + 1
            break
    text = text[:start_idx] + text[end_idx:]

idx = text.find("owQualReportDialog)")
if idx != -1:
    start_idx = text.rfind("\n", 0, idx)
    if start_idx == -1: start_idx = idx
    count = 0
    end_idx = idx
    for i in range(idx, len(text)):
        if text[i] == '{': count += 1
        elif text[i] == '}': count -= 1
        if count == 0 and i > idx:
            end_idx = i + 1
            break
    text = text[:start_idx] + text[end_idx:]

# Ensure @Composable on screens
for screen in ["CalendarScreen", "ReportsScreen", "AnaliseScreen"]:
    text = text.replace("fun " + screen, "@Composable\nfun " + screen)
    text = text.replace("@Composable\n@Composable\nfun " + screen, "@Composable\nfun " + screen)

with open('app/src/main/java/com/example/ui/PostoUi.kt', 'w') as f:
    f.write(text)
