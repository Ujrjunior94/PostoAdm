with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    text = f.read()

text = text.replace("}}}private fun generateCSV", "}}}\nprivate fun generateCSV")

with open('app/src/main/java/com/example/ui/PostoUi.kt', 'w') as f:
    f.write(text)
