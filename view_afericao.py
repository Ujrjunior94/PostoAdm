import re

with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    text = f.read()

# Find AfericaoScreen
start = text.find("fun AfericaoScreen")
if start != -1:
    end = text.find("fun AnaliseScreen", start)
    if end == -1:
        end = len(text)
    print("AfericaoScreen found, length:", end - start)
    print(text[start:start+1000])
    
