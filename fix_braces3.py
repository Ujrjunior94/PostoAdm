with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    text = f.read()

text += "                    }\n                }\n            }\n        }\n    }\n}\n"

with open('app/src/main/java/com/example/ui/PostoUi.kt', 'w') as f:
    f.write(text)
