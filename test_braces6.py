with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    text = f.read()

end_idx = 333496
print(text[end_idx-1000:end_idx+200])

