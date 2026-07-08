with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    text = f.read()

count = 0
for i, char in enumerate(text):
    if char == '{': count += 1
    elif char == '}': count -= 1
    if count < 0:
        print("Negative brace count at", i)
        print(text[i-100:i+100])
        break
