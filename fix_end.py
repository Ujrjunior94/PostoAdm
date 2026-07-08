with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    text = f.read()

# We want to completely delete everything after AnaliseScreen.
# AnaliseScreen starts with `fun AnaliseScreen(viewModel: PostoViewModel) {`
# Let's find its matching brace, and then delete everything after that brace!
idx = text.find("fun AnaliseScreen(viewModel: PostoViewModel) {")
if idx != -1:
    count = 0
    end_idx = -1
    for i in range(idx, len(text)):
        if text[i] == '{': count += 1
        elif text[i] == '}': count -= 1
        if count == 0 and i > idx:
            end_idx = i + 1
            break
    if end_idx != -1:
        text = text[:end_idx] + "\n"

with open('app/src/main/java/com/example/ui/PostoUi.kt', 'w') as f:
    f.write(text)
