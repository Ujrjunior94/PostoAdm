with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    lines = f.readlines()

for target in [2584, 2794]:
    brace_count = 0
    for i in range(target - 1, len(lines)):
        line = lines[i]
        for char in line:
            if char == '{':
                brace_count += 1
            elif char == '}':
                brace_count -= 1
        
        if brace_count == 0:
            print(f"Block at {target} closes at {i+1}")
            break
