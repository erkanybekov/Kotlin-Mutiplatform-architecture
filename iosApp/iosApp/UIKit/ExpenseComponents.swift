import SwiftUI

struct ExpenseGlow: View {
    let color: Color

    var body: some View {
        Circle()
            .fill(color.opacity(0.12))
            .frame(width: 240, height: 240)
    }
}

struct ExpenseSectionTitle: View {
    let title: String

    var body: some View {
        Text(title)
            .font(.system(size: 22, weight: .bold))
            .foregroundStyle(ExpensePalette.textPrimary)
    }
}

struct ExpenseRefreshChip: View {
    let isLoading: Bool
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            HStack(spacing: 10) {
                if isLoading {
                    ProgressView()
                        .tint(ExpensePalette.accentGold)
                        .scaleEffect(0.8)
                    Text("Syncing")
                } else {
                    Text("Refresh")
                }
            }
            .font(.system(size: 13, weight: .semibold))
            .foregroundStyle(ExpensePalette.textPrimary)
            .padding(.horizontal, 14)
            .padding(.vertical, 10)
            .background(ExpensePalette.surfaceChip, in: RoundedRectangle(cornerRadius: 18, style: .continuous))
        }
        .buttonStyle(.plain)
        .disabled(isLoading)
    }
}

struct ExpenseTextField: View {
    let title: String
    @Binding var text: String
    var axis: Axis = .horizontal

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(title)
                .font(.system(size: 13, weight: .semibold))
                .foregroundStyle(ExpensePalette.textSecondary)

            TextField("", text: $text, axis: axis)
                .font(.system(size: 16, weight: .medium))
                .foregroundStyle(ExpensePalette.textPrimary)
                .padding(.horizontal, 16)
                .padding(.vertical, 14)
                .background(ExpensePalette.surfaceMuted, in: RoundedRectangle(cornerRadius: 22, style: .continuous))
        }
    }
}

struct ExpenseHeroCard: View {
    let balanceLabel: String
    let summaryCards: [SummaryCardModel]

    var body: some View {
        VStack(alignment: .leading, spacing: 18) {
            Text("Total balance")
                .font(.system(size: 13, weight: .semibold))
                .foregroundStyle(Color.expense(hex: "#9BA8C8"))

            Text(balanceLabel)
                .font(.system(size: 38, weight: .black))
                .foregroundStyle(ExpensePalette.textPrimary)

            HStack(spacing: 12) {
                ForEach(summaryCards) { card in
                    ExpenseMetricCard(card: card)
                }
            }
        }
        .padding(22)
        .background(
            LinearGradient(
                colors: [Color.expense(hex: "#1A2440"), Color.expense(hex: "#11192E"), Color.expense(hex: "#0D1325")],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            ),
            in: RoundedRectangle(cornerRadius: 32, style: .continuous)
        )
    }
}

struct ExpenseMetricCard: View {
    let card: SummaryCardModel

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Capsule()
                .fill(Color.expense(hex: card.accentHex))
                .frame(width: 42, height: 6)

            Text(card.title)
                .font(.system(size: 13, weight: .semibold))
                .foregroundStyle(Color.expense(hex: "#96A3C4"))

            Text(card.amountLabel)
                .font(.system(size: 22, weight: .bold))
                .foregroundStyle(ExpensePalette.textPrimary)

            Text(card.caption)
                .font(.system(size: 12, weight: .regular))
                .foregroundStyle(ExpensePalette.textMuted)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(16)
        .background(ExpensePalette.surfaceInset, in: RoundedRectangle(cornerRadius: 24, style: .continuous))
    }
}

struct ExpensePeriodSwitcher: View {
    let selectedPeriodLabel: String
    let onSelect: (String) -> Void

    private let periods = ["Week", "Month", "Year"]

    var body: some View {
        HStack(spacing: 8) {
            ForEach(periods, id: \.self) { period in
                let isSelected = selectedPeriodLabel == period
                Button(action: { onSelect(period) }) {
                    Text(period)
                        .font(.system(size: 13, weight: .bold))
                        .foregroundStyle(isSelected ? Color.expense(hex: "#1B130C") : Color.expense(hex: "#9AA7C6"))
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 12)
                        .background {
                            if isSelected {
                                RoundedRectangle(cornerRadius: 18, style: .continuous)
                                    .fill(
                                        LinearGradient(
                                            colors: [ExpensePalette.accentWarm, ExpensePalette.accentGold],
                                            startPoint: .leading,
                                            endPoint: .trailing
                                        )
                                    )
                            } else {
                                RoundedRectangle(cornerRadius: 18, style: .continuous)
                                    .fill(ExpensePalette.surfaceMuted)
                            }
                        }
                }
                .buttonStyle(.plain)
            }
        }
        .padding(6)
        .background(ExpensePalette.surfaceMuted, in: RoundedRectangle(cornerRadius: 24, style: .continuous))
    }
}

struct ExpenseCategoryCard: View {
    let category: CategoryCardModel

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack {
                Text(category.name)
                    .font(.system(size: 16, weight: .bold))
                    .foregroundStyle(ExpensePalette.textPrimary)
                Spacer()
                Circle()
                    .fill(Color.expense(hex: category.accentHex))
                    .frame(width: 12, height: 12)
            }

            Text(category.amountLabel)
                .font(.system(size: 22, weight: .black))
                .foregroundStyle(ExpensePalette.textPrimary)

            ProgressView(value: category.progress)
                .tint(Color.expense(hex: category.accentHex))
                .background(Color.expense(hex: "#212B43"), in: Capsule())

            Text(category.shareLabel)
                .font(.system(size: 13, weight: .bold))
                .foregroundStyle(Color.expense(hex: category.accentHex))
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(ExpensePalette.surfaceMuted, in: RoundedRectangle(cornerRadius: 26, style: .continuous))
    }
}

struct ExpenseTypeSwitcher: View {
    let isIncome: Bool
    let onSelect: (Bool) -> Void

    var body: some View {
        HStack(spacing: 8) {
            ExpenseSelectionChip(
                title: "Expense",
                selected: !isIncome,
                accentHex: "#FFA65B",
                action: { onSelect(false) }
            )
            ExpenseSelectionChip(
                title: "Income",
                selected: isIncome,
                accentHex: "#29D4A5",
                action: { onSelect(true) }
            )
        }
        .padding(6)
        .background(ExpensePalette.surfaceMuted, in: RoundedRectangle(cornerRadius: 22, style: .continuous))
    }
}

struct ExpenseCategorySelector: View {
    let categories: [CategoryOptionModel]
    let selectedCategory: String
    let onSelect: (String) -> Void

    private let columns = [
        GridItem(.flexible(), spacing: 10),
        GridItem(.flexible(), spacing: 10),
        GridItem(.flexible(), spacing: 10)
    ]

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            Text("Category")
                .font(.system(size: 13, weight: .semibold))
                .foregroundStyle(ExpensePalette.textSecondary)

            LazyVGrid(columns: columns, spacing: 10) {
                ForEach(categories) { category in
                    ExpenseSelectionChip(
                        title: category.name,
                        selected: category.name == selectedCategory,
                        accentHex: category.accentHex,
                        action: { onSelect(category.name) }
                    )
                }
            }
        }
    }
}

struct ExpensePrimaryButton: View {
    let title: String
    let isLoading: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            ZStack {
                if isLoading {
                    ProgressView()
                        .tint(Color.expense(hex: "#1B130C"))
                } else {
                    Text(title)
                        .font(.system(size: 17, weight: .bold))
                }
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 16)
            .foregroundStyle(Color.expense(hex: "#1B130C"))
            .background(ExpensePalette.accentGold, in: RoundedRectangle(cornerRadius: 22, style: .continuous))
        }
        .buttonStyle(.plain)
        .disabled(isLoading)
    }
}

struct ExpenseEmptyCard: View {
    let title: String
    let message: String

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(title)
                .font(.system(size: 18, weight: .bold))
                .foregroundStyle(ExpensePalette.textPrimary)

            Text(message)
                .font(.system(size: 15, weight: .regular))
                .foregroundStyle(ExpensePalette.textMuted)
        }
        .padding(18)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(ExpensePalette.surfaceMuted, in: RoundedRectangle(cornerRadius: 28, style: .continuous))
    }
}

struct ExpenseTransactionCard: View {
    let transaction: TransactionRowModel

    var body: some View {
        HStack(spacing: 14) {
            ZStack {
                RoundedRectangle(cornerRadius: 16, style: .continuous)
                    .fill(Color.expense(hex: transaction.accentHex).opacity(0.18))
                    .frame(width: 44, height: 44)

                Text(String(transaction.category.prefix(1)))
                    .font(.system(size: 18, weight: .bold))
                    .foregroundStyle(Color.expense(hex: transaction.accentHex))
            }

            VStack(alignment: .leading, spacing: 4) {
                Text(transaction.title)
                    .font(.system(size: 16, weight: .semibold))
                    .foregroundStyle(ExpensePalette.textPrimary)

                Text(transaction.subtitle)
                    .font(.system(size: 15, weight: .regular))
                    .foregroundStyle(ExpensePalette.textMuted)
            }

            Spacer()

            VStack(alignment: .trailing, spacing: 4) {
                Text(transaction.amountLabel)
                    .font(.system(size: 16, weight: .bold))
                    .foregroundStyle(transaction.isIncome ? ExpensePalette.accentSuccess : ExpensePalette.accentGold)

                Text(transaction.dateLabel)
                    .font(.system(size: 12, weight: .regular))
                    .foregroundStyle(ExpensePalette.textMuted)
            }
        }
        .padding(16)
        .background(Color.expense(hex: "#10182B"), in: RoundedRectangle(cornerRadius: 24, style: .continuous))
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
                .font(.system(size: 14, weight: .regular))
                .foregroundStyle(Color.expense(hex: "#7382A3"))
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
                                        Color.expense(hex: "#FFA65B").opacity(0.40),
                                        Color.expense(hex: "#FFA65B").opacity(0.08),
                                        .clear
                                    ],
                                    startPoint: .top,
                                    endPoint: .bottom
                                )
                            )

                        ExpenseLineShape(points: linePoints)
                            .stroke(
                                Color.expense(hex: "#FFA65B"),
                                style: StrokeStyle(lineWidth: 6, lineCap: .round, lineJoin: .round)
                            )

                        ForEach(Array(linePoints.enumerated()), id: \.offset) { _, point in
                            Circle()
                                .fill(Color.expense(hex: "#0D1325"))
                                .frame(width: 18, height: 18)
                                .position(point)

                            Circle()
                                .fill(Color.expense(hex: "#FFA65B"))
                                .frame(width: 10, height: 10)
                                .position(point)
                        }
                    }
                }
                .frame(height: 210)

                HStack {
                    ForEach(points) { point in
                        Text(point.label)
                            .font(.system(size: 12, weight: .regular))
                            .foregroundStyle(ExpensePalette.textMuted)
                            .frame(maxWidth: .infinity)
                    }
                }
            }
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

private struct ExpenseSelectionChip: View {
    let title: String
    let selected: Bool
    let accentHex: String
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.system(size: 13, weight: .bold))
                .foregroundStyle(selected ? Color.expense(hex: accentHex) : ExpensePalette.textSecondary)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 12)
                .background(
                    (selected ? Color.expense(hex: accentHex).opacity(0.18) : ExpensePalette.surfaceChip),
                    in: RoundedRectangle(cornerRadius: 18, style: .continuous)
                )
        }
        .buttonStyle(.plain)
    }
}
