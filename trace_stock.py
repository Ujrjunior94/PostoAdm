with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    lines = f.readlines()

start = 1783 # 1-based 1784
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
        print(f"StockScreen closes at line {i+1}")
        break
if count > 0:
    print(f"StockScreen remains open with count={count}")
