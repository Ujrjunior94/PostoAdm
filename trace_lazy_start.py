with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    lines = f.readlines()

start_line = 1856
brace_count = 0
for i in range(start_line - 1, 2793):
    line = lines[i]
    for char in line:
        if char == '{':
            brace_count += 1
        elif char == '}':
            brace_count -= 1
    
    if brace_count > 1 and "item" not in line and "items" not in line:
        # Check if we are inside an if or something
        if "{" in line:
            print(f"Line {i+1}: {brace_count} | {line.strip()}")
