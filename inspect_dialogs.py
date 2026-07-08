with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    text = f.read()

start = text.find("fun StockScreen")
end = text.find("fun EmployeesScreen", start)
stock_code = text[start:end]

start_c = stock_code.find("if (showCalibReportDialogInStock) {")
end_c = stock_code.find("if (showQualReportDialog) {", start_c)
print(stock_code[start_c:end_c])

start_q = stock_code.find("if (showQualReportDialog) {")
end_q = stock_code.find("if (showAddConformityDialog) {", start_q)
if end_q == -1: end_q = stock_code.find("if (showAddDeliveryDialog) {", start_q)
if end_q == -1: end_q = len(stock_code)
print(stock_code[start_q:end_q])

