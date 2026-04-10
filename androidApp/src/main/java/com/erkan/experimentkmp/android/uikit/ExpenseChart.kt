package com.erkan.experimentkmp.android.uikit

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.erkan.experimentkmp.presentation.dashboard.ChartPointUi

@Composable
fun ExpenseLineChart(
    points: List<ChartPointUi>,
) {
    if (points.isEmpty()) {
        Text(
            text = "No chart data yet.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }

    val lineColor = MaterialTheme.colorScheme.primary
    val markerSurfaceColor = MaterialTheme.colorScheme.surface
    val fillBrush = Brush.verticalGradient(
        colors = listOf(
            lineColor.copy(alpha = 0.32f),
            lineColor.copy(alpha = 0.08f),
            Color.Transparent,
        ),
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp),
        ) {
            val maxValue = points.maxOf { it.amount }
            val minValue = points.minOf { it.amount }
            val verticalRange = (maxValue - minValue).takeIf { it > 0.0 } ?: 1.0
            val leftPadding = 8.dp.toPx()
            val rightPadding = 8.dp.toPx()
            val bottomPadding = 18.dp.toPx()
            val topPadding = 8.dp.toPx()
            val graphWidth = size.width - leftPadding - rightPadding
            val graphHeight = size.height - topPadding - bottomPadding
            val stepX = if (points.size == 1) 0f else graphWidth / (points.size - 1)

            val linePath = Path()
            val fillPath = Path()

            points.forEachIndexed { index, point ->
                val x = leftPadding + stepX * index
                val normalized = ((point.amount - minValue) / verticalRange).toFloat()
                val y = topPadding + graphHeight - (normalized * graphHeight)

                if (index == 0) {
                    linePath.moveTo(x, y)
                    fillPath.moveTo(x, size.height - bottomPadding)
                    fillPath.lineTo(x, y)
                } else {
                    linePath.lineTo(x, y)
                    fillPath.lineTo(x, y)
                }
            }

            fillPath.lineTo(leftPadding + stepX * (points.size - 1), size.height - bottomPadding)
            fillPath.close()

            drawPath(path = fillPath, brush = fillBrush)
            drawPath(
                path = linePath,
                color = lineColor,
                style = Stroke(width = 8f, cap = StrokeCap.Round, join = StrokeJoin.Round),
            )

            points.forEachIndexed { index, point ->
                val x = leftPadding + stepX * index
                val normalized = ((point.amount - minValue) / verticalRange).toFloat()
                val y = topPadding + graphHeight - (normalized * graphHeight)

                drawCircle(color = markerSurfaceColor, radius = 11f, center = Offset(x, y))
                drawCircle(color = lineColor, radius = 6f, center = Offset(x, y))
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            points.forEach { point ->
                Text(
                    text = point.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
