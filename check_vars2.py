with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    text = f.read()

start_a = text.find("fun AfericaoScreen")
end_a = text.find("LazyColumn(", start_a)
print("Afericao reportDate:", "reportDate" in text[start_a:end_a])

start_q = text.find("fun AnaliseScreen")
end_q = text.find("LazyColumn(", start_q)
print("Analise reportDate:", "reportDate" in text[start_q:end_q])
