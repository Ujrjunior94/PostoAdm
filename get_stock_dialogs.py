with open('app/src/main/java/com/example/ui/PostoUi.kt', 'r') as f:
    text = f.read()

start = text.find("fun StockScreen")
end = text.find("fun EmployeesScreen", start)

stock_code = text[start:end]

print("Calib report dialog:")
start_c = stock_code.find("if (showCalibReportDialogInStock) {")
if start_c != -1:
    end_c = stock_code.find("if (showQualReportDialog) {", start_c)
    print(stock_code[start_c:start_c+500])

print("\nQual report dialog:")
start_q = stock_code.find("if (showQualReportDialog) {")
if start_q != -1:
    print(stock_code[start_q:start_q+500])
