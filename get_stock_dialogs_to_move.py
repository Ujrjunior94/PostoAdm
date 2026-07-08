import re

with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    text = f.read()

start = text.find("fun StockScreen")
end = text.find("fun EmployeesScreen", start)

stock_code = text[start:end]

start_c = stock_code.find("if (showCalibReportDialogInStock) {")
end_c = stock_code.find("if (showQualReportDialog) {", start_c)
calib_dialog = stock_code[start_c:end_c]

start_q = stock_code.find("if (showQualReportDialog) {")
end_q = stock_code.find("if (showAddConformityDialog) {", start_q)
if end_q == -1: end_q = stock_code.find("if (showAddDeliveryDialog) {", start_q)
if end_q == -1: end_q = len(stock_code)
qual_dialog = stock_code[start_q:end_q]

# Check if they really exist
print(len(calib_dialog), len(qual_dialog))

# Remove them from StockScreen
new_text = text.replace(calib_dialog, "").replace(qual_dialog, "")

# Now insert calib_dialog at the end of AfericaoScreen
# wait, AfericaoScreen ends with `}\n\n@Composable\nfun AnaliseScreen`
a_end = new_text.find("}\n@Composable\nfun AnaliseScreen")
if a_end == -1:
    a_end = new_text.find("}\n\n@Composable\nfun AnaliseScreen")

# insert before the closing brace of AfericaoScreen
new_text = new_text[:a_end] + "\n" + calib_dialog + new_text[a_end:]

# insert qual_dialog at the end of AnaliseScreen
new_text = new_text + "\n" + qual_dialog

with open('app/src/main/java/com/example/ui/PostoUi.kt', 'w') as f:
    f.write(new_text)

