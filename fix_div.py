import re
with open("web/index.html", "r") as f:
    content = f.read()
content = re.sub(r'(<span>Nova Credencial</span>\s*</button>)', r'\1 </div>', content)
with open("web/index.html", "w") as f:
    f.write(content)
