with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    text = f.read()

# Let's count braces leading up to generateCSV
idx = text.find("private fun generateCSV")
sub = text[:idx]

b_count = 0
for c in sub:
    if c == '{': b_count += 1
    elif c == '}': b_count -= 1

print("Braces before generateCSV:", b_count)
