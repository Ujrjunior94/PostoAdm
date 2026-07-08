with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    text = f.read()

import re

# Fix syntax error at 1607
text = text.replace("}    if (showAddScheduleDialog) {", "}\n    if (showAddScheduleDialog) {")

# Remove isDraggingThis reassignments
text = text.replace("isDraggingThis = true", "")
text = text.replace("isDraggingThis = false", "")

# Fix ShiftCellContent
shift_cell_idx = text.find("private fun ShiftCellContent(")
if shift_cell_idx != -1:
    old_str = """val isDraggingThis = draggedSchedule?.id == sched.id
                                        val isFolga = sched.shift.contains("Folga", ignoreCase = true)"""
    new_str = """val isFolga = sched.shift.contains("Folga", ignoreCase = true)"""
    # Replace ONLY after shift_cell_idx
    after_text = text[shift_cell_idx:].replace(old_str, new_str, 1)
    text = text[:shift_cell_idx] + after_text

with open('app/src/main/java/com/example/ui/PostoUi.kt', 'w') as f:
    f.write(text)
