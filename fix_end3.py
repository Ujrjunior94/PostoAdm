with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    text = f.read()

idx = text.rfind("Text(\"Baixar PDF de Qualidade\")")
if idx != -1:
    end_idx = idx
    count = 0
    for i in range(idx, len(text)):
        if text[i] == '}':
            count += 1
            if count == 6:
                text = text[:i+1] + "\n"
                break

with open('app/src/main/java/com/example/ui/PostoUi.kt', 'w') as f:
    f.write(text)
