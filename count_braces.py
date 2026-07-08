with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    text = f.read()

count = 0
for char in text:
    if char == '{': count += 1
    elif char == '}': count -= 1

print("Brace count diff:", count)
