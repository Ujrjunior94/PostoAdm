import re

with open("web/index.html", "r") as f:
    content = f.read()

pattern = re.compile(
    r"(\s*const newConf = \{\s*id: Date.now\(\),\s*date,\s*fuelType,\s*densityMeasured,\s*temperature,\s*correctedDensity,\s*waterPhaseVolume,\s*ethanolPercent,\s*aspectColor,\s*isConforme,\s*technicianName,\s*observation:[^,]*,)(\s*stationCnpj: cnpj\s*\};\s*state\.conformities\.unshift\(newConf\);)",
    re.DOTALL
)

def repl(m):
    return """
            const deliveryIdVal = document.getElementById('confDeliverySelect')?.value;
            const deliveryId = deliveryIdVal ? parseInt(deliveryIdVal) : null;
""" + m.group(1) + """
                deliveryId: deliveryId,""" + m.group(2) + """
            if (deliveryId) {
                const linkedDelivery = (state.deliveries || []).find(d => d.id === deliveryId);
                if (linkedDelivery) {
                    linkedDelivery.conformityId = newConf.id;
                }
            }
"""

new_content = pattern.sub(repl, content)

if content != new_content:
    with open("web/index.html", "w") as f:
        f.write(new_content)
    print("Fixed with regex")
else:
    print("Regex failed to match")

