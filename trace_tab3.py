with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    lines = f.readlines()

start_line = 3202
brace_count = 0
for i in range(start_line - 1, len(lines)):
    line = lines[i]
    for char in line:
        if char == '{':
            brace_count += 1
        elif char == '}':
            brace_count -= 1
    
    if brace_count == 0:
        print(f"Tab 3 at 3202 CLOSES at {i+1}")
        break
