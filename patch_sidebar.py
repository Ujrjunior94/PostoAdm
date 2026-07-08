with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    text = f.read()

target = """        SidebarItem(
            label = "Sistemas",
            icon = Icons.Default.Lock,
            selected = currentScreen == "SISTEMAS",
            onClick = { viewModel.navigateTo("SISTEMAS") }
        )"""

replacement = """        SidebarItem(
            label = "Sistemas",
            icon = Icons.Default.Lock,
            selected = currentScreen == "SISTEMAS",
            onClick = { viewModel.navigateTo("SISTEMAS") }
        )
        SidebarItem(
            label = "Análises",
            icon = Icons.Default.Science,
            selected = currentScreen == "ANALISES",
            onClick = { viewModel.navigateTo("ANALISES") }
        )
        SidebarItem(
            label = "Aferições",
            icon = Icons.Default.Build,
            selected = currentScreen == "AFERICOES",
            onClick = { viewModel.navigateTo("AFERICOES") }
        )"""

if target in text:
    text = text.replace(target, replacement)
    with open('app/src/main/java/com/example/ui/PostoUi.kt', 'w') as f:
        f.write(text)
    print("Patched!")
else:
    print("Target not found!")
