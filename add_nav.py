with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    text = f.read()

# Update MainLayout
main_layout_target = """                    "SISTEMAS" -> SystemsScreen(viewModel)
                }"""
main_layout_replacement = """                    "SISTEMAS" -> SystemsScreen(viewModel)
                    "ANALISES" -> AnaliseScreen(viewModel)
                    "AFERICOES" -> AfericaoScreen(viewModel)
                }"""
text = text.replace(main_layout_target, main_layout_replacement)

# Update NavigationSidebar
sidebar_target = """                NavigationSidebarItem(
                    icon = Icons.Default.Lock,
                    label = "Sistemas",
                    isSelected = currentScreen == "SISTEMAS",
                    onClick = { viewModel.navigateTo("SISTEMAS") }
                )
            }
        }"""
sidebar_replacement = """                NavigationSidebarItem(
                    icon = Icons.Default.Lock,
                    label = "Sistemas",
                    isSelected = currentScreen == "SISTEMAS",
                    onClick = { viewModel.navigateTo("SISTEMAS") }
                )
                NavigationSidebarItem(
                    icon = Icons.Default.Science,
                    label = "Análises",
                    isSelected = currentScreen == "ANALISES",
                    onClick = { viewModel.navigateTo("ANALISES") }
                )
                NavigationSidebarItem(
                    icon = Icons.Default.Scale,
                    label = "Aferições",
                    isSelected = currentScreen == "AFERICOES",
                    onClick = { viewModel.navigateTo("AFERICOES") }
                )
            }
        }"""
if Icons_Default_Scale := text.find("Icons.Default.Scale") == -1:
    # Need to add import or check if available
    pass

# Wait, is `Icons.Default.Scale` available?
# There is `Icons.Default.Balance` or something?
# Let's use `Icons.Default.CheckCircle` for Aferição and `Icons.Default.Science` for Análise.
# Actually I can see which icon was used for aferição before.

