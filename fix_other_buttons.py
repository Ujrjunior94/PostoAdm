import re

with open("web/index.html", "r") as f:
    content = f.read()

def inject_before(content, pattern, text_to_insert):
    return re.sub(pattern, text_to_insert + r"\n\1", content)

# 1. Employees
emp_pat = r'(<button onclick="openAddEmployeeModal\(\)" class="[^"]*">)'
emp_btn = """                            <button onclick="clearTabData('employees')" class="bg-red-50 text-red-600 border border-red-200 font-bold px-4 py-2.5 rounded-xl hover:bg-red-100 transition flex items-center space-x-1.5 text-xs">
                                <span class="material-symbols-outlined text-sm font-bold">delete</span>
                                <span>Limpar Dados</span>
                            </button>"""
content = inject_before(content, emp_pat, emp_btn)

# 2. Nozzles
noz_pat = r'(<button onclick="openAddNozzleModal\(\)" class="[^"]*">)'
noz_btn = """                        <div class="flex space-x-2 self-start">
                            <button onclick="clearTabData('nozzles')" class="bg-red-50 text-red-600 border border-red-200 font-bold px-4 py-2.5 rounded-xl hover:bg-red-100 transition flex items-center space-x-1.5 text-xs">
                                <span class="material-symbols-outlined text-sm font-bold">delete</span>
                                <span>Limpar Dados</span>
                            </button>"""
content = re.sub(noz_pat, noz_btn + r"\n                            \1\n                        </div>", content)

# 3. Systems
sys_pat = r'(<button onclick="openAddCredentialModal\(\)" class="[^"]*">)'
sys_btn = """                                    <button onclick="clearTabData('systems')" class="bg-red-50 text-red-600 border border-red-200 font-bold px-3 py-2 rounded-xl hover:bg-red-100 transition text-xs flex items-center space-x-1">
                                        <span class="material-symbols-outlined text-sm font-bold">delete</span>
                                        <span>Limpar Dados</span>
                                    </button>"""
content = inject_before(content, sys_pat, sys_btn)

with open("web/index.html", "w") as f:
    f.write(content)

print("Fixed other buttons")
