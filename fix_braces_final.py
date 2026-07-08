with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    text = f.read()

# Let's find exactly the spot where it goes negative.
count = 0
for i, char in enumerate(text):
    if char == '{': count += 1
    elif char == '}': count -= 1
    
    if count < 0:
        # found the extra brace! Let's delete it!
        text = text[:i] + text[i+1:]
        break

with open('app/src/main/java/com/example/ui/PostoUi.kt', 'w') as f:
    f.write(text)
