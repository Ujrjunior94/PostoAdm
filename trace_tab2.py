with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    lines = f.readlines()

start_line = 2584
brace_count = 0
for i in range(start_line - 1, 10208):
    line = lines[i]
    for char in line:
        if char == '{':
            brace_count += 1
        elif char == '}':
            brace_count -= 1
    
    if brace_count == 0:
        print(f"Tab 2 at 2584 CLOSES at {i+1}")
        break
