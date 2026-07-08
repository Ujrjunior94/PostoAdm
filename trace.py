with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    text = f.read()

idx = text.find("showAddReportDialog = false")
if idx == -1: print("Not found"); exit()

# trace backwards
count = 0
for i in range(idx, -1, -1):
    if text[i] == '}': count += 1
    elif text[i] == '{': count -= 1
    if count < 0:
        print("Wrapper starts near:")
        print(text[i-100:i+50])
        count = 0
