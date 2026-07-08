with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    lines = f.readlines()

new_lines = []
current_function = None
seen_vars = set()

for line in lines:
    if line.strip().startswith("fun ") and "{" in line:
        current_function = line.split("fun ")[1].split("(")[0]
        seen_vars = set()
    
    if "var " in line and "by remember" in line:
        var_name = line.split("var ")[1].split(" by")[0].strip()
        if var_name in seen_vars:
            continue
        seen_vars.add(var_name)
        
    new_lines.append(line)

with open('app/src/main/java/com/example/ui/PostoUi.kt', 'w') as f:
    f.writelines(new_lines)
