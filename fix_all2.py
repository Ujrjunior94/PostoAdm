with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    text = f.read()

# 1. Fix EmployeesScreen
text = text.replace("    fun EmployeesScreen", "    @Composable\n    fun EmployeesScreen")

# 2. Fix the broken block at 9424: `}showCalibReportDialogInStock) {`
# We'll just delete this block. Let's find `showCalibReportDialogInStock) {`
idx = text.find("showCalibReportDialogInStock) {")
if idx != -1:
    count = 0
    end_idx = idx
    for i in range(idx, len(text)):
        if text[i] == '{': count += 1
        elif text[i] == '}': count -= 1
        if count == 0 and i > idx:
            end_idx = i + 1
            break
    
    # We should delete from right after the `}` that preceded it
    # But let's just delete `showCalibReportDialogInStock) { ... }`
    text = text[:idx] + text[end_idx:]

# 3. Same for ` (showQualReportDialog) {`
idx = text.find(" (showQualReportDialog) {")
if idx != -1:
    count = 0
    end_idx = idx
    for i in range(idx, len(text)):
        if text[i] == '{': count += 1
        elif text[i] == '}': count -= 1
        if count == 0 and i > idx:
            end_idx = i + 1
            break
    text = text[:idx] + text[end_idx:]

with open('app/src/main/java/com/example/ui/PostoUi.kt', 'w') as f:
    f.write(text)
