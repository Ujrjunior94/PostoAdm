with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    lines = f.readlines()

brace_count = 0
for i in range(len(lines)):
    line = lines[i]
    for char in line:
        if char == '{':
            brace_count += 1
        elif char == '}':
            brace_count -= 1
    
    if i + 1 in [3559, 3560, 3561, 3562, 3563, 3564, 3565]:
        print(f"Line {i + 1}: {brace_count} | {line.strip()}")
