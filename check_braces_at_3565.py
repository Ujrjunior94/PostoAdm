with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    lines = f.readlines()

start_line = 1759
end_line = 3565
brace_count = 0
for i in range(start_line - 1, end_line):
    line = lines[i]
    for char in line:
        if char == '{':
            brace_count += 1
        elif char == '}':
            brace_count -= 1

print(f"Brace count at line {end_line}: {brace_count}")
