with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    text = f.read()

idx = text.find("if (showAddReportDialog) {")
if idx != -1:
    count = 0
    end_idx = -1
    for i in range(idx, len(text)):
        if text[i] == '{': count += 1
        elif text[i] == '}': count -= 1
        if count == 0:
            end_idx = i
            break
    print("showAddReportDialog block ends near:")
    print(text[end_idx-100:end_idx+200])
