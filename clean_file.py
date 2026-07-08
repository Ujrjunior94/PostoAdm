with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    text = f.read()

# find `@Composablefu`
idx = text.rfind("@Composablefu")
if idx != -1:
    text = text[:idx]

with open('app/src/main/java/com/example/ui/PostoUi.kt', 'w') as f:
    f.write(text)
