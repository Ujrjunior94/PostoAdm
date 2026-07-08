with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    lines = f.readlines()

def find_stock_boundaries():
    for i, line in enumerate(lines):
        if "fun StockScreen(" in line:
            return i
    return 0

start_stock = find_stock_boundaries()

# Now remove all duplicate declarations inside StockScreen ONLY.
seen = set()
to_remove = set()
brace_count = 0

for i in range(start_stock, len(lines)):
    line = lines[i]
    for char in line:
        if char == '{': brace_count += 1
        elif char == '}': brace_count -= 1
    
    if "var " in line and "by remember" in line:
        var_name = line.split("var ")[1].split(" by")[0].strip()
        if var_name in seen:
            to_remove.add(i)
        else:
            seen.add(var_name)
    
    if brace_count == 0:
        break

new_lines = [line for i, line in enumerate(lines) if i not in to_remove]

with open('app/src/main/java/com/example/ui/PostoUi.kt', 'w') as f:
    f.writelines(new_lines)
