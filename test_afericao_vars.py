with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    text = f.read()

idx = text.find("fun AfericaoScreen")
if idx != -1:
    print(text[idx:idx+1500])
