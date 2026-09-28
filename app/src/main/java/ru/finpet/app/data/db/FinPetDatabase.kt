package ru.finpet.app.data.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import ru.finpet.app.model.*

class FinPetDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "finpet_local.db"
        const val DATABASE_VERSION = 1

        private const val TABLE_PROFILE = "profile"
        private const val TABLE_BUDGET = "budget_plans"
        private const val TABLE_GOALS = "goals"
        private const val TABLE_TRANSACTIONS = "transactions"
        private const val TABLE_COMPLETED_QUESTS = "completed_quests"
        private const val TABLE_PARENT_TASKS = "parent_tasks"
        private const val TABLE_SETTINGS = "settings"
        private const val TABLE_REAL_EXPENSES = "real_expenses"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Таблица профиля игрока и питомца
        db.execSQL(
            """
            CREATE TABLE $TABLE_PROFILE (
                id INTEGER PRIMARY KEY,
                player_name TEXT NOT NULL,
                pet_name TEXT NOT NULL,
                pet_type TEXT NOT NULL,
                pet_color TEXT NOT NULL,
                pet_accessory TEXT NOT NULL,
                level INTEGER NOT NULL,
                exp INTEGER NOT NULL,
                exp_to_next INTEGER NOT NULL,
                hunger REAL NOT NULL,
                happiness REAL NOT NULL,
                care REAL NOT NULL,
                energy REAL NOT NULL,
                fin_score INTEGER NOT NULL,
                evolution_stage TEXT NOT NULL,
                current_period INTEGER NOT NULL,
                total_coins INTEGER NOT NULL,
                is_onboarding_completed INTEGER NOT NULL,
                last_advice TEXT NOT NULL
            )
            """.trimIndent()
        )

        // Таблица планов и фактов бюджета по периодам
        db.execSQL(
            """
            CREATE TABLE $TABLE_BUDGET (
                period_id INTEGER PRIMARY KEY,
                available_amount INTEGER NOT NULL,
                needs_plan INTEGER NOT NULL,
                wants_plan INTEGER NOT NULL,
                savings_plan INTEGER NOT NULL,
                needs_fact INTEGER NOT NULL,
                wants_fact INTEGER NOT NULL,
                savings_fact INTEGER NOT NULL,
                is_confirmed INTEGER NOT NULL
            )
            """.trimIndent()
        )

        // Таблица финансовых целей
        db.execSQL(
            """
            CREATE TABLE $TABLE_GOALS (
                id TEXT PRIMARY KEY,
                title TEXT NOT NULL,
                target_amount INTEGER NOT NULL,
                current_amount INTEGER NOT NULL,
                icon TEXT NOT NULL,
                is_custom INTEGER NOT NULL,
                is_achieved INTEGER NOT NULL,
                is_selected INTEGER NOT NULL
            )
            """.trimIndent()
        )

        // Таблица истории финансовых операций
        db.execSQL(
            """
            CREATE TABLE $TABLE_TRANSACTIONS (
                id TEXT PRIMARY KEY,
                title TEXT NOT NULL,
                amount INTEGER NOT NULL,
                envelope TEXT NOT NULL,
                is_income INTEGER NOT NULL,
                timestamp_str TEXT NOT NULL,
                period_id INTEGER NOT NULL,
                note TEXT NOT NULL
            )
            """.trimIndent()
        )

        // Завершенные финансовые квесты
        db.execSQL(
            """
            CREATE TABLE $TABLE_COMPLETED_QUESTS (
                quest_id TEXT PRIMARY KEY,
                selected_option INTEGER NOT NULL,
                earned_coins INTEGER NOT NULL,
                completed_at TEXT NOT NULL
            )
            """.trimIndent()
        )

        // Задания от родителей
        db.execSQL(
            """
            CREATE TABLE $TABLE_PARENT_TASKS (
                id TEXT PRIMARY KEY,
                title TEXT NOT NULL,
                description TEXT NOT NULL,
                reward_coins INTEGER NOT NULL,
                status TEXT NOT NULL,
                icon TEXT NOT NULL
            )
            """.trimIndent()
        )

        // Системные настройки (демо-режим, звук и т.д.)
        db.execSQL(
            """
            CREATE TABLE $TABLE_SETTINGS (
                key TEXT PRIMARY KEY,
                value TEXT NOT NULL
            )
            """.trimIndent()
        )

        // Реальные расходы пользователя (Правило 50/30/20)
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $TABLE_REAL_EXPENSES (
                id TEXT PRIMARY KEY,
                title TEXT NOT NULL,
                amount_rub INTEGER NOT NULL,
                category TEXT NOT NULL,
                date_str TEXT NOT NULL,
                note TEXT NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $TABLE_REAL_EXPENSES (
                id TEXT PRIMARY KEY,
                title TEXT NOT NULL,
                amount_rub INTEGER NOT NULL,
                category TEXT NOT NULL,
                date_str TEXT NOT NULL,
                note TEXT NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PROFILE")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_BUDGET")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_GOALS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TRANSACTIONS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_COMPLETED_QUESTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PARENT_TASKS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SETTINGS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_REAL_EXPENSES")
        onCreate(db)
    }

    fun resetToDefault() {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.execSQL("DELETE FROM $TABLE_PROFILE")
            db.execSQL("DELETE FROM $TABLE_BUDGET")
            db.execSQL("DELETE FROM $TABLE_GOALS")
            db.execSQL("DELETE FROM $TABLE_TRANSACTIONS")
            db.execSQL("DELETE FROM $TABLE_COMPLETED_QUESTS")
            db.execSQL("DELETE FROM $TABLE_PARENT_TASKS")
            db.execSQL("DELETE FROM $TABLE_SETTINGS")
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    // --- Методы работы с профилем ---

    fun saveProfile(
        playerName: String,
        pet: Pet,
        totalCoins: Int,
        currentPeriod: Int,
        isOnboardingCompleted: Boolean,
        lastAdvice: String
    ) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("id", 1)
            put("player_name", playerName)
            put("pet_name", pet.name)
            put("pet_type", pet.type.name)
            put("pet_color", pet.color.name)
            put("pet_accessory", pet.accessory.name)
            put("level", pet.level)
            put("exp", pet.exp)
            put("exp_to_next", pet.expToNextLevel)
            put("hunger", pet.hunger)
            put("happiness", pet.happiness)
            put("care", pet.care)
            put("energy", pet.energy)
            put("fin_score", pet.finScore)
            put("evolution_stage", pet.evolutionStage.name)
            put("current_period", currentPeriod)
            put("total_coins", totalCoins)
            put("is_onboarding_completed", if (isOnboardingCompleted) 1 else 0)
            put("last_advice", lastAdvice)
        }
        db.insertWithOnConflict(TABLE_PROFILE, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    data class LoadedProfile(
        val playerName: String,
        val pet: Pet,
        val totalCoins: Int,
        val currentPeriod: Int,
        val isOnboardingCompleted: Boolean,
        val lastAdvice: String
    )

    fun loadProfile(): LoadedProfile? {
        val db = readableDatabase
        val cursor = db.query(TABLE_PROFILE, null, "id = 1", null, null, null, null)
        return cursor.use {
            if (it.moveToFirst()) {
                val playerName = it.getString(it.getColumnIndexOrThrow("player_name"))
                val petName = it.getString(it.getColumnIndexOrThrow("pet_name"))
                val petType = PetType.valueOf(it.getString(it.getColumnIndexOrThrow("pet_type")))
                val petColor = PetColor.valueOf(it.getString(it.getColumnIndexOrThrow("pet_color")))
                val petAccessory = try {
                    PetAccessory.valueOf(it.getString(it.getColumnIndexOrThrow("pet_accessory")))
                } catch (e: Exception) {
                    PetAccessory.NONE
                }
                val level = it.getInt(it.getColumnIndexOrThrow("level"))
                val exp = it.getInt(it.getColumnIndexOrThrow("exp"))
                val expToNext = it.getInt(it.getColumnIndexOrThrow("exp_to_next"))
                val hunger = it.getFloat(it.getColumnIndexOrThrow("hunger"))
                val happiness = it.getFloat(it.getColumnIndexOrThrow("happiness"))
                val care = it.getFloat(it.getColumnIndexOrThrow("care"))
                val energy = it.getFloat(it.getColumnIndexOrThrow("energy"))
                val finScore = it.getInt(it.getColumnIndexOrThrow("fin_score"))
                val evolutionStage = EvolutionStage.valueOf(it.getString(it.getColumnIndexOrThrow("evolution_stage")))
                val currentPeriod = it.getInt(it.getColumnIndexOrThrow("current_period"))
                val totalCoins = it.getInt(it.getColumnIndexOrThrow("total_coins"))
                val isOnboardingCompleted = it.getInt(it.getColumnIndexOrThrow("is_onboarding_completed")) == 1
                val lastAdvice = it.getString(it.getColumnIndexOrThrow("last_advice"))

                val pet = Pet(
                    id = "pet_1",
                    name = petName,
                    type = petType,
                    color = petColor,
                    accessory = petAccessory,
                    level = level,
                    exp = exp,
                    expToNextLevel = expToNext,
                    hunger = hunger,
                    happiness = happiness,
                    care = care,
                    energy = energy,
                    finScore = finScore,
                    evolutionStage = evolutionStage
                )

                LoadedProfile(playerName, pet, totalCoins, currentPeriod, isOnboardingCompleted, lastAdvice)
            } else {
                null
            }
        }
    }

    // --- Методы работы с бюджетом ---

    fun saveBudget(plan: BudgetPeriodPlan) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("period_id", plan.periodId)
            put("available_amount", plan.availableAmount)
            put("needs_plan", plan.needsPlan)
            put("wants_plan", plan.wantsPlan)
            put("savings_plan", plan.savingsPlan)
            put("needs_fact", plan.needsFact)
            put("wants_fact", plan.wantsFact)
            put("savings_fact", plan.savingsFact)
            put("is_confirmed", if (plan.isConfirmed) 1 else 0)
        }
        db.insertWithOnConflict(TABLE_BUDGET, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun loadBudget(periodId: Int): BudgetPeriodPlan? {
        val db = readableDatabase
        val cursor = db.query(TABLE_BUDGET, null, "period_id = ?", arrayOf(periodId.toString()), null, null, null)
        return cursor.use {
            if (it.moveToFirst()) {
                BudgetPeriodPlan(
                    periodId = it.getInt(it.getColumnIndexOrThrow("period_id")),
                    availableAmount = it.getInt(it.getColumnIndexOrThrow("available_amount")),
                    needsPlan = it.getInt(it.getColumnIndexOrThrow("needs_plan")),
                    wantsPlan = it.getInt(it.getColumnIndexOrThrow("wants_plan")),
                    savingsPlan = it.getInt(it.getColumnIndexOrThrow("savings_plan")),
                    needsFact = it.getInt(it.getColumnIndexOrThrow("needs_fact")),
                    wantsFact = it.getInt(it.getColumnIndexOrThrow("wants_fact")),
                    savingsFact = it.getInt(it.getColumnIndexOrThrow("savings_fact")),
                    isConfirmed = it.getInt(it.getColumnIndexOrThrow("is_confirmed")) == 1
                )
            } else null
        }
    }

    // --- Методы работы с целями ---

    fun saveGoal(goal: FinancialGoal, isSelected: Boolean) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("id", goal.id)
            put("title", goal.title)
            put("target_amount", goal.targetAmount)
            put("current_amount", goal.currentAmount)
            put("icon", goal.icon)
            put("is_custom", if (goal.isCustom) 1 else 0)
            put("is_achieved", if (goal.isAchieved) 1 else 0)
            put("is_selected", if (isSelected) 1 else 0)
        }
        db.insertWithOnConflict(TABLE_GOALS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun loadGoals(): Pair<List<FinancialGoal>, String?> {
        val db = readableDatabase
        val list = mutableListOf<FinancialGoal>()
        var selectedId: String? = null
        val cursor = db.query(TABLE_GOALS, null, null, null, null, null, null)
        cursor.use {
            while (it.moveToNext()) {
                val id = it.getString(it.getColumnIndexOrThrow("id"))
                val title = it.getString(it.getColumnIndexOrThrow("title"))
                val targetAmount = it.getInt(it.getColumnIndexOrThrow("target_amount"))
                val currentAmount = it.getInt(it.getColumnIndexOrThrow("current_amount"))
                val icon = it.getString(it.getColumnIndexOrThrow("icon"))
                val isCustom = it.getInt(it.getColumnIndexOrThrow("is_custom")) == 1
                val isAchieved = it.getInt(it.getColumnIndexOrThrow("is_achieved")) == 1
                val isSelected = it.getInt(it.getColumnIndexOrThrow("is_selected")) == 1
                if (isSelected) {
                    selectedId = id
                }
                list.add(
                    FinancialGoal(
                        id = id,
                        title = title,
                        targetAmount = targetAmount,
                        currentAmount = currentAmount,
                        icon = icon,
                        isCustom = isCustom,
                        isAchieved = isAchieved
                    )
                )
            }
        }
        return Pair(list, selectedId)
    }

    // --- Методы работы с транзакциями ---

    fun addTransaction(tx: FinTransaction) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("id", tx.id)
            put("title", tx.title)
            put("amount", tx.amount)
            put("envelope", tx.envelope.name)
            put("is_income", if (tx.isIncome) 1 else 0)
            put("timestamp_str", tx.timestampFormatted)
            put("period_id", tx.periodId)
            put("note", tx.note)
        }
        db.insertWithOnConflict(TABLE_TRANSACTIONS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun loadTransactions(): List<FinTransaction> {
        val db = readableDatabase
        val list = mutableListOf<FinTransaction>()
        val cursor = db.query(TABLE_TRANSACTIONS, null, null, null, null, null, "rowid DESC")
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    FinTransaction(
                        id = it.getString(it.getColumnIndexOrThrow("id")),
                        title = it.getString(it.getColumnIndexOrThrow("title")),
                        amount = it.getInt(it.getColumnIndexOrThrow("amount")),
                        envelope = EnvelopeType.valueOf(it.getString(it.getColumnIndexOrThrow("envelope"))),
                        isIncome = it.getInt(it.getColumnIndexOrThrow("is_income")) == 1,
                        timestampFormatted = it.getString(it.getColumnIndexOrThrow("timestamp_str")),
                        periodId = it.getInt(it.getColumnIndexOrThrow("period_id")),
                        note = it.getString(it.getColumnIndexOrThrow("note"))
                    )
                )
            }
        }
        return list
    }

    // --- Методы работы с квестами ---

    fun saveQuestCompletion(questId: String, selectedOption: Int, earnedCoins: Int, completedAt: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("quest_id", questId)
            put("selected_option", selectedOption)
            put("earned_coins", earnedCoins)
            put("completed_at", completedAt)
        }
        db.insertWithOnConflict(TABLE_COMPLETED_QUESTS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun loadCompletedQuests(): Map<String, Int> {
        val db = readableDatabase
        val map = mutableMapOf<String, Int>()
        val cursor = db.query(TABLE_COMPLETED_QUESTS, null, null, null, null, null, null)
        cursor.use {
            while (it.moveToNext()) {
                val qId = it.getString(it.getColumnIndexOrThrow("quest_id"))
                val opt = it.getInt(it.getColumnIndexOrThrow("selected_option"))
                map[qId] = opt
            }
        }
        return map
    }

    // --- Методы работы с родительскими заданиями ---

    fun saveParentTasks(tasks: List<ParentTask>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.execSQL("DELETE FROM $TABLE_PARENT_TASKS")
            for (t in tasks) {
                val values = ContentValues().apply {
                    put("id", t.id)
                    put("title", t.title)
                    put("description", t.description)
                    put("reward_coins", t.rewardCoins)
                    put("status", t.status.name)
                    put("icon", t.icon)
                }
                db.insert(TABLE_PARENT_TASKS, null, values)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun loadParentTasks(): List<ParentTask> {
        val db = readableDatabase
        val list = mutableListOf<ParentTask>()
        val cursor = db.query(TABLE_PARENT_TASKS, null, null, null, null, null, null)
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    ParentTask(
                        id = it.getString(it.getColumnIndexOrThrow("id")),
                        title = it.getString(it.getColumnIndexOrThrow("title")),
                        description = it.getString(it.getColumnIndexOrThrow("description")),
                        rewardCoins = it.getInt(it.getColumnIndexOrThrow("reward_coins")),
                        status = TaskStatus.valueOf(it.getString(it.getColumnIndexOrThrow("status"))),
                        icon = it.getString(it.getColumnIndexOrThrow("icon"))
                    )
                )
            }
        }
        return list
    }

    // --- Настройки ---

    fun setSetting(key: String, value: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("key", key)
            put("value", value)
        }
        db.insertWithOnConflict(TABLE_SETTINGS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getSetting(key: String, default: String): String {
        val db = readableDatabase
        val cursor = db.query(TABLE_SETTINGS, arrayOf("value"), "key = ?", arrayOf(key), null, null, null)
        return cursor.use {
            if (it.moveToFirst()) it.getString(0) else default
        }
    }

    // --- Реальные расходы пользователя (Правило 50/30/20) ---

    fun loadRealExpenses(): List<RealExpenseItem> {
        val db = readableDatabase
        val list = mutableListOf<RealExpenseItem>()
        val cursor = db.query(TABLE_REAL_EXPENSES, null, null, null, null, null, "id DESC")
        cursor.use {
            while (it.moveToNext()) {
                val catStr = it.getString(it.getColumnIndexOrThrow("category"))
                val cat = try {
                    RealCategory.valueOf(catStr)
                } catch (e: Exception) {
                    RealCategory.NEEDS
                }
                list.add(
                    RealExpenseItem(
                        id = it.getString(it.getColumnIndexOrThrow("id")),
                        title = it.getString(it.getColumnIndexOrThrow("title")),
                        amountRub = it.getInt(it.getColumnIndexOrThrow("amount_rub")),
                        category = cat,
                        dateFormatted = it.getString(it.getColumnIndexOrThrow("date_str")),
                        note = it.getString(it.getColumnIndexOrThrow("note"))
                    )
                )
            }
        }
        return list
    }

    fun saveRealExpense(item: RealExpenseItem) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("id", item.id)
            put("title", item.title)
            put("amount_rub", item.amountRub)
            put("category", item.category.name)
            put("date_str", item.dateFormatted)
            put("note", item.note)
        }
        db.insertWithOnConflict(TABLE_REAL_EXPENSES, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun deleteRealExpense(id: String) {
        val db = writableDatabase
        db.delete(TABLE_REAL_EXPENSES, "id = ?", arrayOf(id))
    }
}
