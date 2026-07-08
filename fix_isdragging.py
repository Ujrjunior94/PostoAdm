with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    text = f.read()

text = text.replace("daySchedules.forEach { sched ->", "daySchedules.forEach { sched ->\n                                        val isDraggingThis = draggedScheduleId == sched.id")

with open('app/src/main/java/com/example/ui/PostoUi.kt', 'w') as f:
    f.write(text)
