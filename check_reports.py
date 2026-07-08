with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    text = f.read()

print("AfericaoScreen:", "AfericaoScreen" in text)
print("AnaliseScreen:", "AnaliseScreen" in text)

print("Afericao report text:", "Baixar PDF de Aferição" in text)
print("Analise report text:", "Baixar PDF de Análise" in text)

