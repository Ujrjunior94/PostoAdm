with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    text = f.read()

import re

# Fix og at 1607
text = text.replace("}og) {", "}    if (showAddScheduleDialog) {")

# Fix isDraggingThis
text = text.replace("val isFolga = sched.shift.contains(\"Folga\", ignoreCase = true)", 
                    "val isDraggingThis = draggedSchedule?.id == sched.id\n                                        val isFolga = sched.shift.contains(\"Folga\", ignoreCase = true)")

with open('app/src/main/java/com/example/ui/PostoUi.kt', 'w') as f:
    f.write(text)
