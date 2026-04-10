import SwiftUI
import shared

struct ExpenseDashboardView: View {
    @StateObject private var viewModel: ExpenseDashboardViewModel

    init(appGraph: SharedAppGraph) {
        _viewModel = StateObject(wrappedValue: ExpenseDashboardViewModel(appGraph: appGraph))
    }

    var body: some View {
        let viewState = viewModel.viewState

        NavigationStack {
            List {
                if let error = viewState.errorMessage {
                    Section {
                        Text(error)
                            .font(.subheadline)
                            .foregroundStyle(.red)
                    }
                }

                Section {
                    ExpenseBalanceCard(
                        balanceLabel: viewState.balanceLabel,
                        summaryCards: viewState.summaryCards
                    )
                    .listRowInsets(EdgeInsets())
                    .listRowBackground(Color.clear)
                }

                Section {
                    ExpenseSectionHeader(
                        title: "New entry",
                        supportingText: "Add income or an expense to the local database."
                    )

                    Picker("Type", selection: entryTypeBinding(viewState)) {
                        ForEach(ExpenseEntryKind.allCases) { kind in
                            Text(kind.rawValue).tag(kind)
                        }
                    }
                    .pickerStyle(.segmented)

                    TextField("Title", text: textBinding(viewState.draft.title, intent: ExpenseDashboardViewIntent.titleChanged))
                        .textInputAutocapitalization(.words)
                    if let titleError = viewState.draft.titleError {
                        ExpenseValidationMessage(text: titleError)
                    }

                    TextField("Amount", text: textBinding(viewState.draft.amount, intent: ExpenseDashboardViewIntent.amountChanged))
                        .keyboardType(.decimalPad)
                    if let amountError = viewState.draft.amountError {
                        ExpenseValidationMessage(text: amountError)
                    }

                    TextField("Note", text: textBinding(viewState.draft.note, intent: ExpenseDashboardViewIntent.noteChanged), axis: .vertical)
                        .lineLimit(2...4)

                    if !viewState.draft.entryType.isIncome && !viewState.availableCategories.isEmpty {
                        Picker("Category", selection: categoryBinding(viewState)) {
                            ForEach(viewState.availableCategories) { category in
                                Text(category.name).tag(category.name)
                            }
                        }
                        .pickerStyle(.menu)

                        if let categoryError = viewState.draft.categoryError {
                            ExpenseValidationMessage(text: categoryError)
                        }
                    }

                    Button {
                        viewModel.send(.submitEntry)
                    } label: {
                        if viewState.isSaving {
                            ProgressView()
                                .frame(maxWidth: .infinity)
                        } else {
                            Text(viewState.draft.entryType == .income ? "Save income" : "Save expense")
                                .frame(maxWidth: .infinity)
                        }
                    }
                    .buttonStyle(.borderedProminent)
                    .disabled(viewState.isSaving)
                }

                Section {
                    ExpenseSectionHeader(
                        title: "Analytics",
                        supportingText: "Spending breakdown for the selected period."
                    )

                    Picker("Period", selection: periodBinding(viewState)) {
                        ForEach(ExpenseDashboardPeriod.allCases) { period in
                            Text(period.rawValue).tag(period)
                        }
                    }
                    .pickerStyle(.segmented)

                    if viewState.chartPoints.contains(where: { $0.amount > 0 }) {
                        ExpenseChartView(points: viewState.chartPoints)
                    } else {
                        ExpenseEmptyStateCard(
                            title: "No chart data yet",
                            message: "Add a few expenses and the chart will start reflecting them."
                        )
                    }
                }

                Section("Categories") {
                    if viewState.categories.isEmpty {
                        ExpenseEmptyStateCard(
                            title: "No expense categories yet",
                            message: "Category totals appear after you save expense entries."
                        )
                    } else {
                        ForEach(viewState.categories) { category in
                            ExpenseCategorySummaryRow(category: category)
                        }
                    }
                }

                Section("Recent Transactions") {
                    if viewState.recentTransactions.isEmpty {
                        ExpenseEmptyStateCard(
                            title: "No entries yet",
                            message: "Your latest expenses and income will appear here after the first save."
                        )
                    } else {
                        ForEach(viewState.recentTransactions) { transaction in
                            ExpenseTransactionRow(transaction: transaction)
                                .swipeActions(edge: .trailing, allowsFullSwipe: true) {
                                    Button(role: .destructive) {
                                        viewModel.send(.deleteEntry(transaction.id))
                                    } label: {
                                        Label("Delete", systemImage: "trash")
                                    }
                                }
                        }
                    }
                }
            }
            .scrollContentBackground(.hidden)
            .background(ExpensePalette.background)
            .navigationTitle("Expenses")
            .navigationBarTitleDisplayMode(.large)
        }
        .task {
            viewModel.send(.load)
        }
    }

    private func textBinding(
        _ value: String,
        intent: @escaping (String) -> ExpenseDashboardViewIntent
    ) -> Binding<String> {
        Binding(
            get: { value },
            set: { viewModel.send(intent($0)) }
        )
    }

    private func entryTypeBinding(
        _ viewState: ExpenseDashboardViewState
    ) -> Binding<ExpenseEntryKind> {
        Binding(
            get: { viewState.draft.entryType },
            set: { viewModel.send(.entryTypeChanged($0)) }
        )
    }

    private func categoryBinding(
        _ viewState: ExpenseDashboardViewState
    ) -> Binding<String> {
        Binding(
            get: { viewState.draft.selectedCategory },
            set: { viewModel.send(.categorySelected($0)) }
        )
    }

    private func periodBinding(
        _ viewState: ExpenseDashboardViewState
    ) -> Binding<ExpenseDashboardPeriod> {
        Binding(
            get: { viewState.selectedPeriod },
            set: { viewModel.send(.periodSelected($0)) }
        )
    }
}
