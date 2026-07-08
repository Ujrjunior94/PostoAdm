with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    text = f.read()

idx = text.find("fun ReportsScreen(viewModel: PostoViewModel) {")
if idx != -1:
    idx = text.find("{", idx)
    count = 0
    end_idx = -1
    for i in range(idx, len(text)):
        if text[i] == '{': count += 1
        elif text[i] == '}': count -= 1
        if count == 0:
            end_idx = i
            break
    if end_idx != -1:
        print("ReportsScreen ends at:", end_idx)
        print("SURROUNDING TEXT AROUND END_IDX:")
        print(text[end_idx-200:end_idx+200])
    else:
        print("ReportsScreen never ends (reaches end of file)!")

