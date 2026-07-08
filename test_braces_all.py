with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    lines = f.readlines()

def check_function(name):
    start = -1
    for i, line in enumerate(lines):
        if line.strip().startswith("fun " + name) or line.strip().startswith("private fun " + name):
            start = i
            break
    if start == -1: return
    
    count = 0
    started = False
    for i in range(start, len(lines)):
        for char in lines[i]:
            if char == '{': 
                count += 1
                started = True
            elif char == '}': 
                count -= 1
        if started and count == 0:
            print(f"{name} closes at line {i+1}")
            return
    print(f"{name} NEVER CLOSES! count={count}")

funcs = ["CalendarScreen", "ReportsScreen", "SystemsScreen", "AfericaoScreen", "StockScreen", "EmployeesScreen", "DashboardScreen"]
for f in funcs: check_function(f)
