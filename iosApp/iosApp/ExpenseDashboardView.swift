import SwiftUI
import shared

struct ExpenseDashboardView: View {
    @StateObject private var dashboardState: ExpenseDashboardObservableState
    @State private var title = ""
    @State private var amount = ""
    @State private var note = ""
    @State private var isIncome = false
    @State private var selectedCategory = ""
    @State private var titleError: String?
    @State private var amountError: String?
    @State private var categoryError: String?

    init(appGraph: SharedAppGraph) {
        _dashboardState = StateObject(wrappedValue: ExpenseDashboardObservableState(appGraph: appGraph))
    }

    var body: some View {
        NavigationStack {
            List {
                if let error = dashboardState.errorMessage {
                    Section {
                        Text(error)
                            .font(.subheadline)
                            .foregroundStyle(.red)
                    }
                }

                Section {
                    ExpenseBalanceCard(
                        balanceLabel: dashboardState.balanceLabel,
                        summaryCards: dashboardState.summaryCards
                    )
                    .listRowInsets(EdgeInsets())
                    .listRowBackground(Color.clear)
                }

                Section {
                    ExpenseSectionHeader(
                        title: "New entry",
                        supportingText: "Add income or an expense to the local database."
                    )

                    Picker("Type", selection: $isIncome) {
                        Text("Expense").tag(false)
                        Text("Income").tag(true)
                    }
                    .pickerStyle(.segmented)
                    .onChange(of: isIncome) { _ in
                        categoryError = nil
                    }

                    TextField("Title", text: $title)
                        .textInputAutocapitalization(.words)
                    if let titleError {
                        ExpenseValidationMessage(text: titleError)
                    }

                    TextField("Amount", text: $amount)
                        .keyboardType(.decimalPad)
                    if let amountError {
                        ExpenseValidationMessage(text: amountError)
                    }

                    TextField("Note", text: $note, axis: .vertical)
                        .lineLimit(2...4)

                    if !isIncome && !dashboardState.availableCategories.isEmpty {
                        Picker("Category", selection: $selectedCategory) {
                            ForEach(dashboardState.availableCategories) { category in
                                Text(category.name).tag(category.name)
                            }
                        }
                        .pickerStyle(.menu)
                        .onChange(of: selectedCategory) { _ in
                            categoryError = nil
                        }

                        if let categoryError {
                            ExpenseValidationMessage(text: categoryError)
                        }
                    }

                    Button(action: saveEntry) {
                        if dashboardState.isSaving {
                            ProgressView()
                                .frame(maxWidth: .infinity)
                        } else {
                            Text(isIncome ? "Save income" : "Save expense")
                                .frame(maxWidth: .infinity)
                        }
                    }
                    .buttonStyle(.borderedProminent)
                    .disabled(dashboardState.isSaving)
                }

                Section {
                    ExpenseSectionHeader(
                        title: "Analytics",
                        supportingText: "Spending breakdown for the selected period."
                    )

                    Picker("Period", selection: Binding(
                        get: { dashboardState.selectedPeriodLabel },
                        set: { dashboardState.selectPeriod($0) }
                    )) {
                        Text("Week").tag("Week")
                        Text("Month").tag("Month")
                        Text("Year").tag("Year")
                    }
                    .pickerStyle(.segmented)

                    if dashboardState.chartPoints.contains(where: { $0.amount > 0 }) {
                        ExpenseChartView(points: dashboardState.chartPoints)
                    } else {
                        ExpenseEmptyStateCard(
                            title: "No chart data yet",
                            message: "Add a few expenses and the chart will start reflecting them."
                        )
                    }
                }

                Section("Categories") {
                    if dashboardState.categories.isEmpty {
                        ExpenseEmptyStateCard(
                            title: "No expense categories yet",
                            message: "Category totals appear after you save expense entries."
                        )
                    } else {
                        ForEach(dashboardState.categories) { category in
                            ExpenseCategorySummaryRow(category: category)
                        }
                    }
                }

                Section("Recent Transactions") {
                    if dashboardState.recentTransactions.isEmpty {
                        ExpenseEmptyStateCard(
                            title: "No entries yet",
                            message: "Your latest expenses and income will appear here after the first save."
                        )
                    } else {
                        ForEach(dashboardState.recentTransactions) { transaction in
                            ExpenseTransactionRow(transaction: transaction)
                                .swipeActions(edge: .trailing, allowsFullSwipe: true) {
                                    Button(role: .destructive) {
                                        dashboardState.deleteEntry(id: transaction.id)
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
        .onChange(of: title) { value in
            if titleError != nil {
                titleError = validateTitle(value)
            }
        }
        .onChange(of: amount) { value in
            if amountError != nil {
                amountError = validateAmount(value)
            }
        }
        .onChange(of: dashboardState.saveSuccessCount) { saveSuccessCount in
            if saveSuccessCount > 0 {
                title = ""
                amount = ""
                note = ""
                isIncome = false
                selectedCategory = dashboardState.availableCategories.first?.name ?? ""
                titleError = nil
                amountError = nil
                categoryError = nil
            }
        }
    }

    private func saveEntry() {
        titleError = validateTitle(title)
        amountError = validateAmount(amount)
        categoryError = validateCategory(
            isIncome: isIncome,
            selectedCategory: selectedCategory
        )

        if titleError == nil && amountError == nil && categoryError == nil {
            dashboardState.saveEntry(
                title: title,
                amountText: amount,
                category: isIncome ? "" : selectedCategory,
                note: note,
                isIncome: isIncome
            )
        }
    }
}

private func validateTitle(_ value: String) -> String? {
    value.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty ? "Title is required." : nil
}

private func validateAmount(_ value: String) -> String? {
    let normalized = value
        .trimmingCharacters(in: .whitespacesAndNewlines)
        .replacingOccurrences(of: ",", with: ".")

    guard !normalized.isEmpty else { return "Amount is required." }
    guard let amount = Double(normalized), amount > 0 else { return "Enter a valid amount." }
    return nil
}

private func validateCategory(
    isIncome: Bool,
    selectedCategory: String
) -> String? {
    if !isIncome && selectedCategory.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
        return "Choose a category."
    }
    return nil
}
