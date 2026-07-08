with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    text = f.read()

# strip everything that's broken at the end
# and just leave it with a proper end
idx = text.rfind("Text(\"Baixar PDF de Qualidade\")")
if idx != -1:
    # find the 5th closing brace after this
    end_idx = idx
    count = 0
    for i in range(idx, len(text)):
        if text[i] == '}':
            count += 1
            if count == 7:
                text = text[:i+1] + "\n"
                break

with open('app/src/main/java/com/example/ui/PostoUi.kt', 'w') as f:
    f.write(text)
