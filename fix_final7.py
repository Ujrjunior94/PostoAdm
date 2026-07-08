with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    text = f.read()

idx = text.find("fun AnaliseScreen")
if idx != -1:
    count = 0
    end_idx = idx
    for i in range(idx, len(text)):
        if text[i] == '{': count += 1
        elif text[i] == '}': count -= 1
        if count == 0 and i > idx:
            end_idx = i + 1
            break
    text = text[:end_idx] + "\n"

with open('app/src/main/java/com/example/ui/PostoUi.kt', 'w') as f:
    f.write(text)
