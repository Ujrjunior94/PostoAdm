with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    text = f.read()

idx = text.find("fun ReportsScreen(viewModel: PostoViewModel) {")
if idx != -1:
    count = 0
    end_idx = -1
    for i in range(idx, len(text)):
        if text[i] == '{': count += 1
        elif text[i] == '}': count -= 1
        if count == 0 and i > idx:
            end_idx = i
            break
    print("ReportsScreen ends at index:", end_idx)
    # let's see what is near end_idx
    print("SURROUNDING TEXT AROUND END_IDX:")
    print(text[end_idx-200:end_idx+200])

