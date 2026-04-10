import SwiftUI

struct ExpenseSectionHeader: View {
    let title: String
    var supportingText: String? = nil

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(title)
                .font(.headline)
                .foregroundStyle(ExpensePalette.textPrimary)

            if let supportingText {
                Text(supportingText)
                    .font(.subheadline)
                    .foregroundStyle(ExpensePalette.textSecondary)
            }
        }
    }
}

struct ExpenseBalanceCard: View {
    let balanceLabel: String
    let summaryCards: [SummaryCardModel]

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Current balance")
                .font(.subheadline.weight(.semibold))
                .foregroundStyle(ExpensePalette.textSecondary)

            Text(balanceLabel)
                .font(.system(size: 34, weight: .bold, design: .rounded))
                .foregroundStyle(ExpensePalette.textPrimary)

            HStack(spacing: 12) {
                ForEach(summaryCards) { card in
                    ExpenseMetricCard(card: card)
                }
            }
        }
        .padding(20)
        .background(
            RoundedRectangle(cornerRadius: 20, style: .continuous)
                .fill(ExpensePalette.surface)
        )
    }
}

private struct ExpenseMetricCard: View {
    let card: SummaryCardModel

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(card.title)
                .font(.caption.weight(.semibold))
                .foregroundStyle(ExpensePalette.textSecondary)

            Text(card.amountLabel)
                .font(.headline)
                .foregroundStyle(ExpensePalette.textPrimary)

            Text(card.caption)
                .font(.caption)
                .foregroundStyle(Color.expense(hex: card.accentHex))
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(14)
        .background(
            RoundedRectangle(cornerRadius: 16, style: .continuous)
                .fill(ExpensePalette.surfaceInset)
        )
    }
}

struct ExpenseValidationMessage: View {
    let text: String

    var body: some View {
        Text(text)
            .font(.caption)
            .foregroundStyle(.red)
    }
}

struct ExpenseEmptyStateCard: View {
    let title: String
    let message: String

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(title)
                .font(.headline)
            Text(message)
                .font(.subheadline)
                .foregroundStyle(ExpensePalette.textSecondary)
        }
        .padding(.vertical, 6)
    }
}

struct ExpenseCategorySummaryRow: View {
    let category: CategoryCardModel

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack(alignment: .firstTextBaseline) {
                Text(category.name)
                    .font(.body.weight(.semibold))
                Spacer()
                Text(category.amountLabel)
                    .font(.body.weight(.semibold))
            }

            ProgressView(value: category.progress)
                .tint(Color.expense(hex: category.accentHex))

            Text(category.shareLabel)
                .font(.caption.weight(.medium))
                .foregroundStyle(Color.expense(hex: category.accentHex))
        }
        .padding(.vertical, 4)
    }
}

struct ExpenseTransactionRow: View {
    let transaction: TransactionRowModel

    var body: some View {
        HStack(spacing: 12) {
            ZStack {
                Circle()
                    .fill(Color.expense(hex: transaction.accentHex).opacity(0.16))
                    .frame(width: 36, height: 36)

                Text(String(transaction.category.prefix(1)))
                    .font(.subheadline.weight(.bold))
                    .foregroundStyle(Color.expense(hex: transaction.accentHex))
            }

            VStack(alignment: .leading, spacing: 3) {
                Text(transaction.title)
                    .font(.body.weight(.semibold))
                    .foregroundStyle(ExpensePalette.textPrimary)
                Text(transaction.subtitle)
                    .font(.subheadline)
                    .foregroundStyle(ExpensePalette.textSecondary)
            }

            Spacer()

            VStack(alignment: .trailing, spacing: 3) {
                Text(transaction.amountLabel)
                    .font(.body.weight(.semibold))
                    .foregroundStyle(transaction.isIncome ? ExpensePalette.accentSuccess : Color.expense(hex: transaction.accentHex))
                Text(transaction.dateLabel)
                    .font(.caption)
                    .foregroundStyle(ExpensePalette.textMuted)
            }
        }
        .padding(.vertical, 4)
    }
}

struct ExpenseLineShape: Shape {
    let points: [CGPoint]

    func path(in rect: CGRect) -> Path {
        var path = Path()
        guard let first = points.first else { return path }
        path.move(to: first)
        for point in points.dropFirst() {
            path.addLine(to: point)
        }
        return path
    }
}

struct ExpenseFillShape: Shape {
    let points: [CGPoint]

    func path(in rect: CGRect) -> Path {
        var path = Path()
        guard let first = points.first else { return path }
        path.move(to: first)
        for point in points.dropFirst() {
            path.addLine(to: point)
        }
        path.closeSubpath()
        return path
    }
}

struct ExpenseChartView: View {
    let points: [ChartPointModel]

    var body: some View {
        if points.isEmpty {
            Text("No chart data yet.")
                .font(.subheadline)
                .foregroundStyle(ExpensePalette.textSecondary)
        } else {
            VStack(spacing: 10) {
                GeometryReader { geometry in
                    let frame = geometry.frame(in: .local)
                    let linePoints = chartCoordinates(in: frame.size)
                    let fillPoints = fillCoordinates(in: frame.size)

                    ZStack {
                        ExpenseFillShape(points: fillPoints)
                            .fill(
                                LinearGradient(
                                    colors: [
                                        ExpensePalette.accentWarm.opacity(0.28),
                                        ExpensePalette.accentWarm.opacity(0.08),
                                        .clear,
                                    ],
                                    startPoint: .top,
                                    endPoint: .bottom
                                )
                            )

                        ExpenseLineShape(points: linePoints)
                            .stroke(
                                ExpensePalette.accentWarm,
                                style: StrokeStyle(lineWidth: 4, lineCap: .round, lineJoin: .round)
                            )

                        ForEach(Array(linePoints.enumerated()), id: \.offset) { _, point in
                            Circle()
                                .fill(ExpensePalette.surface)
                                .frame(width: 14, height: 14)
                                .position(point)

                            Circle()
                                .fill(ExpensePalette.accentWarm)
                                .frame(width: 8, height: 8)
                                .position(point)
                        }
                    }
                }
                .frame(height: 180)

                HStack {
                    ForEach(points) { point in
                        Text(point.label)
                            .font(.caption)
                            .foregroundStyle(ExpensePalette.textSecondary)
                            .frame(maxWidth: .infinity)
                    }
                }
            }
            .padding(.vertical, 8)
        }
    }

    private func chartCoordinates(in size: CGSize) -> [CGPoint] {
        let maxValue = points.map(\.amount).max() ?? 1
        let minValue = points.map(\.amount).min() ?? 0
        let range = max(maxValue - minValue, 1)
        let stepX = points.count > 1 ? size.width / CGFloat(points.count - 1) : 0
        let graphHeight = max(size.height - 18, 1)

        return points.enumerated().map { index, point in
            let normalized = (point.amount - minValue) / range
            return CGPoint(
                x: CGFloat(index) * stepX,
                y: graphHeight - CGFloat(normalized) * graphHeight
            )
        }
    }

    private func fillCoordinates(in size: CGSize) -> [CGPoint] {
        let line = chartCoordinates(in: size)
        guard let first = line.first, let last = line.last else { return [] }
        return [CGPoint(x: first.x, y: size.height)] + line + [CGPoint(x: last.x, y: size.height)]
    }
}
