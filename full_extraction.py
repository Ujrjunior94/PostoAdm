import re

with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    lines = f.readlines()

def find_block(start_line):
    brace_count = 0
    end_line = start_line
    for i in range(start_line - 1, len(lines)):
        line = lines[i]
        for char in line:
            if char == '{':
                brace_count += 1
            elif char == '}':
                brace_count -= 1
        if brace_count == 0:
            end_line = i + 1
            break
    return start_line, end_line

def find_block_by_str(s):
    for i, line in enumerate(lines):
        if s in line:
            return find_block(i + 1)
    return 0, 0

a_start, a_end = find_block_by_str("if (activeSubTab == 2) {")
c_start, c_end = find_block_by_str("if (activeSubTab == 3) {")
d1_start, d1_end = find_block_by_str("if (showAddCalibDialog) {")
d2_start, d2_end = find_block_by_str("if (showAddConformityDialog) {")

print(f"Afericoes: {a_start}-{a_end}, Dialog: {d1_start}-{d1_end}")
print(f"Conformidade: {c_start}-{c_end}, Dialog: {d2_start}-{d2_end}")

