import re

with open("web/index.html", "r") as f:
    content = f.read()

pattern = r'(<button onclick="clearTabData\(\'systems\'\)" class="[^"]*">.*?</button>)<button onclick="openAddCredentialModal\(\)" class="[^"]*">.*?</button>'

def repl(m):
    return '<div class="flex space-x-2">\n' + m.group(0) + '\n</div>'

new_content = re.sub(pattern, repl, content, flags=re.DOTALL)
if new_content != content:
    with open("web/index.html", "w") as f:
        f.write(new_content)
    print("Grouped systems buttons")

