import os

# 1. Clean PetRoomSceneView.kt
with open(r'app/src/main/java/ru/finpet/app/ui/components/PetRoomSceneView.kt', 'r', encoding='utf-8') as f:
    code = f.read()

# Fix drawSkirtingAndCorners call and def
code = code.replace(
    'drawSkirtingAndCorners(\n                w = w,\n                h = h,\n                leftWallX = leftWallX,',
    'drawSkirtingAndCorners(\n                w = w,\n                leftWallX = leftWallX,'
)
code = code.replace(
    'private fun DrawScope.drawSkirtingAndCorners(\n    w: Float,\n    h: Float,',
    'private fun DrawScope.drawSkirtingAndCorners(\n    w: Float,'
)

# Fix drawWardrobe call and def
code = code.replace(
    'drawWardrobe(\n                    leftWallX = leftWallX,\n                    floorBackY = floorBackY,\n                    floorFrontY = floorFrontY,\n                    topWallY = topWallY\n                )',
    'drawWardrobe(\n                    leftWallX = leftWallX,\n                    floorBackY = floorBackY,\n                    topWallY = topWallY\n                )'
)
code = code.replace(
    'private fun DrawScope.drawWardrobe(\n    leftWallX: Float,\n    floorBackY: Float,\n    floorFrontY: Float,\n    topWallY: Float\n)',
    'private fun DrawScope.drawWardrobe(\n    leftWallX: Float,\n    floorBackY: Float,\n    topWallY: Float\n)'
)

# Fix drawDesk call and def
code = code.replace(
    'drawDesk(\n                    deskId = state.equippedDesk,\n                    rightWallX = rightWallX,\n                    floorBackY = floorBackY,\n                    floorFrontY = floorFrontY,\n                    w = w\n                )',
    'drawDesk(\n                    deskId = state.equippedDesk,\n                    rightWallX = rightWallX,\n                    floorBackY = floorBackY,\n                    floorFrontY = floorFrontY,\n                    w = w\n                )'
)

# Fix drawLamp call and def
code = code.replace(
    'drawLamp(\n                    rightWallX = rightWallX,\n                    floorBackY = floorBackY,\n                    floorFrontY = floorFrontY,\n                    isNight = isNight\n                )',
    'drawLamp(\n                    rightWallX = rightWallX,\n                    floorBackY = floorBackY,\n                    isNight = isNight\n                )'
)
code = code.replace(
    'private fun DrawScope.drawLamp(\n    rightWallX: Float,\n    floorBackY: Float,\n    floorFrontY: Float,\n    isNight: Boolean\n)',
    'private fun DrawScope.drawLamp(\n    rightWallX: Float,\n    floorBackY: Float,\n    isNight: Boolean\n)'
)

# Fix drawWindowSunbeam
code = code.replace(
    'drawWindowSunbeam(\n                    winLeft = winLeft,\n                    winTop = winTop,\n                    winW = winW,\n                    winH = winH,\n                    floorBackY = floorBackY,\n                    floorFrontY = floorFrontY,\n                    isSunset = isSunset,\n                    h = h\n                )',
    'drawWindowSunbeam(\n                    winLeft = winLeft,\n                    winTop = winTop,\n                    winW = winW,\n                    winH = winH,\n                    isSunset = isSunset,\n                    h = h\n                )'
)
code = code.replace(
    'private fun DrawScope.drawWindowSunbeam(\n    winLeft: Float,\n    winTop: Float,\n    winW: Float,\n    winH: Float,\n    floorBackY: Float,\n    floorFrontY: Float,\n    isSunset: Boolean,\n    h: Float\n)',
    'private fun DrawScope.drawWindowSunbeam(\n    winLeft: Float,\n    winTop: Float,\n    winW: Float,\n    winH: Float,\n    isSunset: Boolean,\n    h: Float\n)'
)

# Fix drawAtmosphericLighting hasLamp
code = code.replace(
    'drawAtmosphericLighting(\n                w = w,\n                h = h,\n                isNight = isNight,\n                isSunset = isSunset,\n                hasLamp = state.equippedLamp != null,\n                rightWallX = rightWallX,\n                floorBackY = floorBackY\n            )',
    'drawAtmosphericLighting(\n                w = w,\n                h = h,\n                isNight = isNight,\n                isSunset = isSunset,\n                rightWallX = rightWallX,\n                floorBackY = floorBackY\n            )'
)
code = code.replace(
    'private fun DrawScope.drawAtmosphericLighting(\n    w: Float,\n    h: Float,\n    isNight: Boolean,\n    isSunset: Boolean,\n    hasLamp: Boolean,\n    rightWallX: Float,\n    floorBackY: Float\n)',
    'private fun DrawScope.drawAtmosphericLighting(\n    w: Float,\n    h: Float,\n    isNight: Boolean,\n    isSunset: Boolean,\n    rightWallX: Float,\n    floorBackY: Float\n)'
)

with open(r'app/src/main/java/ru/finpet/app/ui/components/PetRoomSceneView.kt', 'w', encoding='utf-8') as f:
    f.write(code)

# 2. Clean QuestsScreen.kt
with open(r'app/src/main/java/ru/finpet/app/ui/screens/QuestsScreen.kt', 'r', encoding='utf-8') as f:
    code_q = f.read()

code_q = code_q.replace('    val currentTheme = LocalAppTheme.current\n', '')
code_q = code_q.replace('    var activeTheoryTopic by remember { mutableStateOf<QuestTopic?>(null) }\n', '')
code_q = code_q.replace(
    'text = if (isAllCompleted) "Сундук главы $unitIndex готов!" else "Сундук главы $unitIndex",',
    'text = if (isAllCompleted) "Сундук «${biome.name}» готов!" else "Сундук «${biome.name}» (Глава $unitIndex)",'
)
code_q = code_q.replace(
    'text = "Фин-блиц!",',
    'text = "Фин-блиц #$branchIndex",',
)

with open(r'app/src/main/java/ru/finpet/app/ui/screens/QuestsScreen.kt', 'w', encoding='utf-8') as f:
    f.write(code_q)

# 3. Clean ShopScreen.kt
with open(r'app/src/main/java/ru/finpet/app/ui/screens/ShopScreen.kt', 'r', encoding='utf-8') as f:
    code_s = f.read()

code_s = code_s.replace('    onClose: () -> Unit = {}\n', '')

with open(r'app/src/main/java/ru/finpet/app/ui/screens/ShopScreen.kt', 'w', encoding='utf-8') as f:
    f.write(code_s)

print("Warnings cleaned successfully!")
