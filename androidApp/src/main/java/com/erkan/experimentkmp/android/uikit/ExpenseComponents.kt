package com.erkan.experimentkmp.android.uikit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.erkan.experimentkmp.domain.model.ExpensePeriod
import com.erkan.experimentkmp.presentation.dashboard.CategoryOptionUi
import com.erkan.experimentkmp.presentation.dashboard.CategoryBreakdownUi
import com.erkan.experimentkmp.presentation.dashboard.SummaryCardUi
import com.erkan.experimentkmp.presentation.dashboard.TransactionItemUi

@Composable
fun ExpenseGlow(
    modifier: Modifier = Modifier,
    color: Color,
) {
    Box(
        modifier = modifier
            .size(220.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.12f)),
    )
}

@Composable
fun ExpenseSectionTitle(
    title: String,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
fun ExpenseRefreshChip(
    isLoading: Boolean,
    onRefresh: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clickable(enabled = !isLoading, onClick = onRefresh)
            .clip(RoundedCornerShape(18.dp))
            .background(ExpensePalette.SurfaceChip)
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        if (isLoading) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = ExpensePalette.AccentGold,
                )
                Text("Syncing", fontSize = 13.sp, color = ExpensePalette.TextPrimary)
            }
        } else {
            Text(
                text = "Refresh",
                color = ExpensePalette.TextPrimary,
                fontSize = 13.sp,
            )
        }
    }
}

@Composable
fun ExpenseHeroCard(
    balanceLabel: String,
    summaryCards: List<SummaryCardUi>,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(ExpenseHeroBrush)
            .padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Text(
            text = "Total balance",
            style = MaterialTheme.typography.labelLarge,
            color = Color(0xFF9BA8C8),
        )
        Text(
            text = balanceLabel,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Black,
            color = ExpensePalette.TextPrimary,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            summaryCards.forEach { card ->
                ExpenseMetricCard(
                    modifier = Modifier.weight(1f),
                    card = card,
                )
            }
        }
    }
}

@Composable
fun ExpenseMetricCard(
    modifier: Modifier = Modifier,
    card: SummaryCardUi,
) {
    val accent = expenseColorFromHex(card.accentHex)
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(ExpensePalette.SurfaceInset)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(width = 42.dp, height = 6.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(accent),
        )
        Text(card.title, style = MaterialTheme.typography.labelLarge, color = Color(0xFF96A3C4))
        Text(card.amountLabel, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(card.caption, style = MaterialTheme.typography.bodySmall, color = ExpensePalette.TextMuted)
    }
}

@Composable
fun ExpensePeriodSwitcher(
    selectedPeriod: ExpensePeriod,
    onPeriodSelected: (ExpensePeriod) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(ExpensePalette.SurfaceMuted)
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ExpensePeriod.entries.forEach { period ->
            val isSelected = period == selectedPeriod
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onPeriodSelected(period) }
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (isSelected) ExpenseWarmBrush else Brush.horizontalGradient(listOf(ExpensePalette.SurfaceMuted, ExpensePalette.SurfaceMuted)))
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = period.label,
                    color = if (isSelected) Color(0xFF1B130C) else Color(0xFF9AA7C6),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
fun ExpenseCategoryCard(
    modifier: Modifier = Modifier,
    category: CategoryBreakdownUi,
) {
    val accent = expenseColorFromHex(category.accentHex)
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(26.dp))
            .background(ExpensePalette.SurfaceMuted)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(category.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(accent),
            )
        }
        Text(category.amountLabel, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
        LinearProgressIndicator(
            progress = { category.progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(999.dp)),
            color = accent,
            trackColor = Color(0xFF212B43),
        )
        Text(category.shareLabel, style = MaterialTheme.typography.labelLarge, color = accent)
    }
}

@Composable
fun ExpenseTransactionCard(
    transaction: TransactionItemUi,
) {
    val accent = expenseColorFromHex(transaction.accentHex)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF10182B))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(accent.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = transaction.category.take(1),
                    color = accent,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(transaction.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(transaction.subtitle, style = MaterialTheme.typography.bodyMedium, color = ExpensePalette.TextMuted)
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                transaction.amountLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (transaction.isIncome) ExpensePalette.AccentSuccess else ExpensePalette.AccentGold,
            )
            Text(transaction.dateLabel, style = MaterialTheme.typography.bodySmall, color = ExpensePalette.TextMuted)
        }
    }
}

@Composable
fun ExpenseInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 3,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = ExpensePalette.SurfaceMuted,
            unfocusedContainerColor = ExpensePalette.SurfaceMuted,
            disabledContainerColor = ExpensePalette.SurfaceMuted,
            focusedBorderColor = ExpensePalette.AccentGold,
            unfocusedBorderColor = Color(0xFF22304F),
            focusedTextColor = ExpensePalette.TextPrimary,
            unfocusedTextColor = ExpensePalette.TextPrimary,
            focusedLabelColor = ExpensePalette.TextSecondary,
            unfocusedLabelColor = ExpensePalette.TextMuted,
            cursorColor = ExpensePalette.AccentGold,
        ),
        shape = RoundedCornerShape(22.dp),
    )
}

@Composable
fun ExpenseTypeSwitcher(
    isIncome: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(ExpensePalette.SurfaceMuted)
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ExpenseToggleChip(
            modifier = Modifier.weight(1f),
            title = "Expense",
            selected = !isIncome,
            onClick = { onToggle(false) },
        )
        ExpenseToggleChip(
            modifier = Modifier.weight(1f),
            title = "Income",
            selected = isIncome,
            onClick = { onToggle(true) },
        )
    }
}

@Composable
fun ExpenseCategorySelector(
    categories: List<CategoryOptionUi>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Category",
            style = MaterialTheme.typography.labelLarge,
            color = ExpensePalette.TextSecondary,
        )
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            categories.chunked(3).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { category ->
                        ExpenseToggleChip(
                            modifier = Modifier.weight(1f),
                            title = category.name,
                            selected = category.name == selectedCategory,
                            onClick = { onCategorySelected(category.name) },
                            accentHex = category.accentHex,
                        )
                    }
                    repeat(3 - row.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun ExpensePrimaryButton(
    title: String,
    isLoading: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
        shape = RoundedCornerShape(22.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = ExpensePalette.AccentGold,
            contentColor = Color(0xFF1B130C),
            disabledContainerColor = ExpensePalette.SurfaceChip,
            disabledContentColor = ExpensePalette.TextSecondary,
        ),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = Color(0xFF1B130C),
            )
        } else {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
fun ExpenseEmptyCard(
    title: String,
    message: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(ExpensePalette.SurfaceMuted)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = ExpensePalette.TextPrimary,
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = ExpensePalette.TextMuted,
        )
    }
}

@Composable
private fun ExpenseToggleChip(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentHex: String = "#FFA65B",
) {
    val accent = expenseColorFromHex(accentHex)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (selected) accent.copy(alpha = 0.18f) else ExpensePalette.SurfaceChip,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = if (selected) accent else ExpensePalette.TextSecondary,
        )
    }
}
