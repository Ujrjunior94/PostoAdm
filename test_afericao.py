with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    text = f.read()

idx1 = text.find("fun ReportsScreen(viewModel: PostoViewModel) {")
idx2 = text.find("fun AfericaoScreen(viewModel: PostoViewModel) {")
idx3 = text.find("fun AnaliseScreen(viewModel: PostoViewModel) {")

print("ReportsScreen:", idx1)
print("AfericaoScreen:", idx2)
print("AnaliseScreen:", idx3)
