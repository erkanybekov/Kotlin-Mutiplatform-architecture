import SwiftUI
import shared

struct ExpenseDashboardView: View {
    @StateObject private var dashboardState: ExpenseDashboardObservableState
    @State private var title = ""
    @State private var amount = ""
    @State private var note = ""
    @State private var isIncome = false
    @State private var selectedCategory = ""
    @State private var wasSaving = false

    init(appGraph: SharedAppGraph) {
        _dashboardState = StateObject(wrappedValue: ExpenseDashboardObservableState(appGraph: appGraph))
    }

    var body: some View {
        ZStack {
            LinearGradient(
                colors: [ExpensePalette.background, ExpensePalette.backgroundTop, ExpensePalette.backgroundBottom],
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()

            ExpenseGlow(color: ExpensePalette.accentWarm)
                .offset(x: 170, y: -290)

            ExpenseGlow(color: ExpensePalette.accentIndigo)
                .offset(x: -180, y: 100)

            ScrollView(showsIndicators: false) {
                VStack(alignment: .leading, spacing: 22) {
                    HeaderSection()

                    if let error = dashboardState.errorMessage {
                        Text(error)
                            .font(.system(size: 14, weight: .medium))
                            .foregroundStyle(ExpensePalette.error)
                    }

                    ExpenseHeroCard(
                        balanceLabel: dashboardState.balanceLabel,
                        summaryCards: dashboardState.summaryCards
                    )

                    QuickEntrySection(
                        state: dashboardState,
                        title: $title,
                        amount: $amount,
                        note: $note,
                        isIncome: $isIncome,
                        selectedCategory: $selectedCategory
                    )

                    ExpensePeriodSwitcher(
                        selectedPeriodLabel: dashboardState.selectedPeriodLabel,
                        onSelect: dashboardState.selectPeriod
                    )

                    AnalyticsSection(points: dashboardState.chartPoints)
                    CategoriesSection(categories: dashboardState.categories)
                    TransactionsSection(transactions: dashboardState.recentTransactions)
                }
                .padding(.horizontal, 22)
                .padding(.top, 12)
                .padding(.bottom, 36)
            }
        }
        .preferredColorScheme(.dark)
        .onAppear {
            dashboardState.load()
            if selectedCategory.isEmpty {
                selectedCategory = dashboardState.availableCategories.first?.name ?? ""
            }
        }
        .onChange(of: dashboardState.availableCategories) { categories in
            if selectedCategory.isEmpty {
                selectedCategory = categories.first?.name ?? ""
            }
        }
        .onChange(of: dashboardState.isSaving) { isSaving in
            if wasSaving && !isSaving && dashboardState.errorMessage == nil {
                title = ""
                amount = ""
                note = ""
                isIncome = false
                selectedCategory = dashboardState.availableCategories.first?.name ?? ""
            }
            wasSaving = isSaving
        }
    }
}

private struct HeaderSection: View {
    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text("Personal ledger")
                .font(.system(size: 14, weight: .semibold))
                .foregroundStyle(ExpensePalette.textSecondary)

            Text("Expense Flow")
                .font(.system(size: 34, weight: .black))
                .foregroundStyle(ExpensePalette.textPrimary)

            Text("Track real entries locally. No seeded data, no sync required.")
                .font(.system(size: 15, weight: .regular))
                .foregroundStyle(ExpensePalette.textMuted)
        }
    }
}

private struct QuickEntrySection: View {
    @ObservedObject var state: ExpenseDashboardObservableState
    @Binding var title: String
    @Binding var amount: String
    @Binding var note: String
    @Binding var isIncome: Bool
    @Binding var selectedCategory: String

    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            ExpenseSectionTitle(title: "New entry")

            Text("Save an expense or income directly to your local database.")
                .font(.system(size: 15, weight: .regular))
                .foregroundStyle(ExpensePalette.textMuted)

            ExpenseTypeSwitcher(
                isIncome: isIncome,
                onSelect: { isIncome = $0 }
            )

            ExpenseTextField(title: "Title", text: $title)
            ExpenseTextField(title: "Amount", text: $amount)
            ExpenseTextField(title: "Note (optional)", text: $note, axis: .vertical)

            if !isIncome && !state.availableCategories.isEmpty {
                ExpenseCategorySelector(
                    categories: state.availableCategories,
                    selectedCategory: selectedCategory,
                    onSelect: { selectedCategory = $0 }
                )
            }

            ExpensePrimaryButton(
                title: isIncome ? "Save income" : "Save expense",
                isLoading: state.isSaving,
                action: {
                    state.saveEntry(
                        title: title,
                        amountText: amount,
                        category: isIncome ? "" : selectedCategory,
                        note: note,
                        isIncome: isIncome
                    )
                }
            )
        }
        .padding(20)
        .background(ExpensePalette.surfaceStrong, in: RoundedRectangle(cornerRadius: 30, style: .continuous))
    }
}

private struct AnalyticsSection: View {
    let points: [ChartPointModel]

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            HStack(alignment: .center) {
                VStack(alignment: .leading, spacing: 4) {
                    ExpenseSectionTitle(title: "Analytics")

                    Text("Spending over the selected period")
                        .font(.system(size: 15, weight: .regular))
                        .foregroundStyle(Color.expense(hex: "#7382A3"))
                }

                Spacer()
            }

            if points.contains(where: { $0.amount > 0 }) {
                ExpenseChartView(points: points)
            } else {
                ExpenseEmptyCard(
                    title: "No chart data yet",
                    message: "Add a few expenses and this section will show your spending pattern."
                )
            }
        }
        .padding(20)
        .background(ExpensePalette.surfaceStrong, in: RoundedRectangle(cornerRadius: 30, style: .continuous))
    }
}

private struct CategoriesSection: View {
    let categories: [CategoryCardModel]

    private let columns = [
        GridItem(.flexible(), spacing: 12),
        GridItem(.flexible(), spacing: 12)
    ]

    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            ExpenseSectionTitle(title: "Categories")

            if categories.isEmpty {
                ExpenseEmptyCard(
                    title: "No expense categories yet",
                    message: "Category totals appear after you save expense entries."
                )
            } else {
                LazyVGrid(columns: columns, spacing: 12) {
                    ForEach(categories) { category in
                        ExpenseCategoryCard(category: category)
                    }
                }
            }
        }
    }
}

private struct TransactionsSection: View {
    let transactions: [TransactionRowModel]

    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            ExpenseSectionTitle(title: "Recent transactions")

            if transactions.isEmpty {
                ExpenseEmptyCard(
                    title: "No entries yet",
                    message: "Your latest expenses and income will appear here after the first save."
                )
            } else {
                VStack(spacing: 12) {
                    ForEach(transactions) { transaction in
                        ExpenseTransactionCard(transaction: transaction)
                    }
                }
            }
        }
    }
}
