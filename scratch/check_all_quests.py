with open(r'app/src/main/java/ru/finpet/app/data/QuestsBank.kt', 'r', encoding='utf-8') as f:
    text = f.read()

import re
matches = re.findall(r'FinancialQuest\s*\(\s*id\s*=\s*"([^"]+)",\s*orderIndex\s*=\s*(\d+),\s*title\s*=\s*"([^"]+)",\s*topic\s*=\s*QuestTopic\.([A-Z_]+)', text)
for m in matches:
    print(f"#{m[1]}: {m[0]} | Topic={m[3]} | Title={m[2]}")
