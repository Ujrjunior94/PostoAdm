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
                    icon = Icons.Default.Build,
                    label = "Aferições",
                    isSelected = currentScreen == "AFERICOES",
                    onClick = { viewModel.navigateTo("AFERICOES") }
                )
            }
        }"""
text = text.replace(sidebar_target, sidebar_replacement)

# Update MobileBottomNavBar
bottom_nav_target = """        NavigationBarItem(
            icon = { Icon(Icons.Default.Lock, contentDescription = "Sistemas") },
            label = { Text("Sistemas", fontSize = 10.sp, fontWeight = if (currentScreen == "SISTEMAS") FontWeight.Bold else FontWeight.Normal) },
            selected = currentScreen == "SISTEMAS",
            onClick = { viewModel.navigateTo("SISTEMAS") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = HdPrimary,
                unselectedIconColor = HdTextSecondary,
                selectedTextColor = HdPrimary,
                unselectedTextColor = HdTextSecondary,
                indicatorColor = HdPrimaryLight
            )
        )
    }
}"""
bottom_nav_replacement = """        NavigationBarItem(
            icon = { Icon(Icons.Default.Lock, contentDescription = "Sistemas") },
            label = { Text("Sistemas", fontSize = 10.sp, fontWeight = if (currentScreen == "SISTEMAS") FontWeight.Bold else FontWeight.Normal) },
            selected = currentScreen == "SISTEMAS",
            onClick = { viewModel.navigateTo("SISTEMAS") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = HdPrimary,
                unselectedIconColor = HdTextSecondary,
                selectedTextColor = HdPrimary,
                unselectedTextColor = HdTextSecondary,
                indicatorColor = HdPrimaryLight
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Science, contentDescription = "Análises") },
            label = { Text("Análises", fontSize = 10.sp, fontWeight = if (currentScreen == "ANALISES") FontWeight.Bold else FontWeight.Normal) },
            selected = currentScreen == "ANALISES",
            onClick = { viewModel.navigateTo("ANALISES") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = HdPrimary,
                unselectedIconColor = HdTextSecondary,
                selectedTextColor = HdPrimary,
                unselectedTextColor = HdTextSecondary,
                indicatorColor = HdPrimaryLight
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Build, contentDescription = "Aferições") },
            label = { Text("Aferições", fontSize = 10.sp, fontWeight = if (currentScreen == "AFERICOES") FontWeight.Bold else FontWeight.Normal) },
            selected = currentScreen == "AFERICOES",
            onClick = { viewModel.navigateTo("AFERICOES") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = HdPrimary,
                unselectedIconColor = HdTextSecondary,
                selectedTextColor = HdPrimary,
                unselectedTextColor = HdTextSecondary,
                indicatorColor = HdPrimaryLight
            )
        )
    }
}"""
text = text.replace(bottom_nav_target, bottom_nav_replacement)

# Ensure Icons.Default.Build is imported
if "import androidx.compose.material.icons.filled.Build" not in text:
    text = text.replace("import androidx.compose.material.icons.filled.Lock", "import androidx.compose.material.icons.filled.Lock\nimport androidx.compose.material.icons.filled.Build")

with open('app/src/main/java/com/example/ui/PostoUi.kt', 'w') as f:
    f.write(text)

