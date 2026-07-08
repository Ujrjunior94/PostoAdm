with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    lines = f.readlines()

start_line = 1856
brace_count = 0
# We start at 1 because we are looking at the block INSIDE LazyColumn { ... }
# but actually the loop will count the '{' at line 1856.
for i in range(start_line - 1, 4966):
    line = lines[i]
    for char in line:
        if char == '{':
            brace_count += 1
        elif char == '}':
            brace_count -= 1
    
    if (i+1) in [2794, 2795, 2796, 2898, 2899, 3008, 3051, 3193, 3194, 3204, 3205]:
        print(f"Line {i + 1}: {brace_count} | {line.strip()}")
