with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    lines = f.readlines()

lazy_start = 1856
brace_count = 0
for i in range(lazy_start - 1, len(lines)):
    line = lines[i]
    for char in line:
        if char == '{':
            brace_count += 1
        elif char == '}':
            brace_count -= 1
    
    if brace_count == 0:
        print(f"LazyColumn block starting at {lazy_start} CLOSES at line {i+1}")
        break
    
    if i + 1 == 2899:
        print(f"At line 2899, LazyColumn brace count is {brace_count}")
