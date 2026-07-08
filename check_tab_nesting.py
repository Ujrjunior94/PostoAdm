with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    lines = f.readlines()

brace_count = 0
for i, line in enumerate(lines):
    for char in line:
        if char == '{':
            brace_count += 1
        elif char == '}':
            brace_count -= 1
    
    if "if (activeSubTab ==" in line:
        print(f"Line {i+1}: {brace_count} | {line.strip()}")
