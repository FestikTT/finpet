with open(r'app/src/main/java/ru/finpet/app/data/QuestsBank.kt', 'r', encoding='utf-8') as f:
    text = f.read()

import re
matches = re.findall(r'FinancialQuest\s*\(\s*id\s*=\s*"([^"]+)",\s*orderIndex\s*=\s*(\d+),\s*title\s*=\s*"([^"]+)",\s*topic\s*=\s*QuestTopic\.([A-Z_]+)', text)
topics = {
    'BUDGETING': 1,
    'BANKING': 2,
    'SMART_SHOPPING': 3,
    'CYBER_SECURITY': 4,
    'INVESTMENTS': 5,
    'FAMILY_ECONOMICS': 6
}
for m in matches:
    u = topics.get(m[3], 0)
    print(f"orderIndex={m[1]}, id={m[0]}, topic={m[3]}, unit={u}, title={m[2]}")
