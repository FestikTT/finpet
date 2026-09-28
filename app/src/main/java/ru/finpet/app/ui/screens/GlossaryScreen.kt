package ru.finpet.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finpet.app.data.GameState
import ru.finpet.app.model.FinancialGlossary
import ru.finpet.app.model.GlossaryTerm
import ru.finpet.app.ui.components.BalanceChips
import ru.finpet.app.ui.components.CoinBadge
import ru.finpet.app.ui.theme.*

@Composable
fun GlossaryScreen(
    state: GameState,
    onBack: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredTerms = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            FinancialGlossary.terms
        } else {
            FinancialGlossary.terms.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.shortDefinition.contains(searchQuery, ignoreCase = true) ||
                it.example.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val currentTheme = LocalAppTheme.current

        // Шапка (без обрезки заголовка и подзаголовка, Аудит п.5)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Назад",
                        tint = currentTheme.primaryColor
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "📖 Азбука юного финансиста",
                        fontSize = 15.sp,
                        lineHeight = 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = currentTheme.primaryColor,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Финансовые понятия простыми словами",
                        fontSize = 11.5.sp,
                        lineHeight = 15.sp,
                        color = TextSecondary,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(modifier = Modifier.width(6.dp))
            BalanceChips(coins = state.totalCoins, savings = state.totalSavingsAmount)
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Поиск термина...", fontSize = 13.sp) },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrandVioletPrimary,
                unfocusedBorderColor = OutlineLight,
                focusedContainerColor = SurfaceLight,
                unfocusedContainerColor = SurfaceLight
            ),
            leadingIcon = { Text("🔍", fontSize = 14.sp) }
        )

        if (filteredTerms.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("Ничего не найдено по запросу", color = TextSecondary, fontSize = 13.sp)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(filteredTerms) { term ->
                    GlossaryCard(term = term)
                }
            }
        }
    }
}

@Composable
private fun GlossaryCard(term: GlossaryTerm) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = SurfaceLight,
        border = androidx.compose.foundation.BorderStroke(1.dp, OutlineLight)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(term.emoji, fontSize = 20.sp)
                Text(
                    text = term.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    lineHeight = 19.sp,
                    color = TextPrimary,
                    maxLines = 2,
                    modifier = Modifier.weight(1f, fill = false)
                )
            }

            Text(
                text = term.shortDefinition,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = TextSecondary
            )

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = SurfaceSubtle,
                border = androidx.compose.foundation.BorderStroke(1.dp, OutlineLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "💡 Пример: ${term.example}",
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    color = TextPrimary,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}
