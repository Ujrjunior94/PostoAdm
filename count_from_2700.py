with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    lines = f.readlines()

start_line = 1759
brace_count = 0
for i in range(start_line - 1, 3600):
    line = lines[i]
    for char in line:
        if char == '{':
            brace_count += 1
        elif char == '}':
            brace_count -= 1
    
    if i >= 2880:
        print(f"Line {i + 1}: {brace_count} | {line.strip()}")
