package ru.finpet.app

import org.junit.Assert.*
import org.junit.Test
import ru.finpet.app.model.*

class FinPetUnitTest {

    @Test
    fun testBudgetPlanBalanceCalculation() {
        val plan = BudgetPeriodPlan(
            periodId = 1,
            availableAmount = 100,
            needsPlan = 50,
            wantsPlan = 20,
            savingsPlan = 30
        )
        assertEquals(100, plan.totalPlanned)
        assertEquals(0, plan.unallocatedAmount)
        assertTrue(plan.isBalanced)

        val overspentPlan = plan.copy(needsPlan = 60)
        assertEquals(110, overspentPlan.totalPlanned)
        assertEquals(-10, overspentPlan.unallocatedAmount)
        assertFalse(overspentPlan.isBalanced)
    }

    @Test
    fun testGoalCalculationsAndEstimatedPeriods() {
        val goal = FinancialGoal(
            id = "g1",
            title = "Самокат",
            targetAmount = 150,
            currentAmount = 50
        )
        assertEquals(100, goal.remainingAmount)
        assertEquals(50f / 150f, goal.progress, 0.001f)
        assertFalse(goal.isAchieved)

        // При откладывании по 25 монет за период: 100 / 25 = 4 периода
        val periods = goal.calculateEstimatedPeriods(avgSavingsPerPeriod = 25)
        assertEquals(4, periods)

        val achievedGoal = goal.copy(currentAmount = 150)
        assertTrue(achievedGoal.currentAmount >= achievedGoal.targetAmount)
        assertEquals(0, achievedGoal.remainingAmount)
        assertEquals(0, achievedGoal.calculateEstimatedPeriods(25))
    }

    @Test
    fun testPetMoodCalculation() {
        val petHappy = Pet(hunger = 0.9f, happiness = 0.8f, care = 0.7f, energy = 0.9f)
        assertEquals(PetMood.HAPPY, petHappy.calculateMood())

        val petHungry = Pet(hunger = 0.2f, happiness = 0.8f, care = 0.7f, energy = 0.9f)
        assertEquals(PetMood.HUNGRY, petHungry.calculateMood())

        val petTired = Pet(hunger = 0.8f, happiness = 0.8f, care = 0.7f, energy = 0.15f)
        assertEquals(PetMood.TIRED, petTired.calculateMood())
    }

    @Test
    fun testPetEvolutionStages() {
        val baby = Pet(level = 1)
        assertEquals(EvolutionStage.BABY, baby.getStage())

        val teen = Pet(level = 3)
        assertEquals(EvolutionStage.TEEN, teen.getStage())

        val master = Pet(level = 6)
        assertEquals(EvolutionStage.MASTER, master.getStage())
    }

    @Test
    fun testPetCombinationsMeetRequirements() {
        // ТЗ 2.6: не менее 9 визуально различимых комбинаций
        val totalTypes = PetType.values().size
        val totalColors = PetColor.values().size
        val totalAccessories = PetAccessory.values().size
        val combinations = totalTypes * totalColors * totalAccessories

        assertTrue("Комбинаций должно быть не менее 9", combinations >= 9)
        assertEquals(3 * 3 * 5, combinations) // 45 комбинаций
    }

    @Test
    fun testQuestsContentRequirements() {
        val topics = QuestTopic.values()
        assertEquals(6, topics.size)
        assertTrue(topics.contains(QuestTopic.BUDGETING))
        assertTrue(topics.contains(QuestTopic.SMART_SHOPPING))
        assertTrue(topics.contains(QuestTopic.BANKING))
        assertTrue(topics.contains(QuestTopic.CYBER_SECURITY))
        assertTrue(topics.contains(QuestTopic.FAMILY_ECONOMICS))
        assertTrue(topics.contains(QuestTopic.INVESTMENTS))

        val allQuests = ru.finpet.app.data.QuestsBank.getAll54Quests()
        assertEquals("Должно быть ровно 54 квеста как у Duolingo", 54, allQuests.size)
        allQuests.forEachIndexed { index, quest ->
            assertEquals(index + 1, quest.orderIndex)
            assertTrue("Название не должно быть пустым", quest.title.isNotBlank())
            assertTrue("Текст ситуации не должен быть пустым", quest.dilemmaText.isNotBlank())
            assertTrue("Вариантов ответа должно быть 3 или 4", quest.options.size in 3..4)
            assertTrue("Должен быть хотя бы один оптимальный вариант", quest.options.any { it.isOptimal })
            quest.options.forEach { opt ->
                assertTrue("Вариант не пустой", opt.text.isNotBlank())
                assertTrue("Объяснение не пустое", opt.feedbackExplanation.isNotBlank())
            }
        }
    }
}
