import SwiftUI
import shared

struct SummaryCardModel: Identifiable, Equatable {
    let id: String
    let title: String
    let amountLabel: String
    let caption: String
    let accentHex: String
}

struct CategoryCardModel: Identifiable, Equatable {
    let id: String
    let name: String
    let amountLabel: String
    let shareLabel: String
    let progress: Double
    let accentHex: String
}

struct CategoryOptionModel: Identifiable, Equatable {
    let id: String
    let name: String
    let accentHex: String
}

struct ChartPointModel: Identifiable, Equatable {
    let id: String
    let label: String
    let amount: Double
}

struct TransactionRowModel: Identifiable, Equatable {
    let id: Int64
    let title: String
    let subtitle: String
    let dateLabel: String
    let category: String
    let amountLabel: String
    let accentHex: String
    let isIncome: Bool
}

@MainActor
final class ExpenseDashboardObservableState: ObservableObject {
    @Published private(set) var isLoading = false
    @Published private(set) var isSaving = false
    @Published private(set) var balanceLabel = "$0.00"
    @Published private(set) var incomeLabel = "$0.00"
    @Published private(set) var expenseLabel = "$0.00"
    @Published private(set) var selectedPeriodLabel = "Month"
    @Published private(set) var summaryCards: [SummaryCardModel] = []
    @Published private(set) var availableCategories: [CategoryOptionModel] = []
    @Published private(set) var chartPoints: [ChartPointModel] = []
    @Published private(set) var categories: [CategoryCardModel] = []
    @Published private(set) var recentTransactions: [TransactionRowModel] = []
    @Published private(set) var isEmpty = true
    @Published private(set) var errorMessage: String?

    private let stateHolder: ExpenseDashboardStateHolder
    private var observationHandle: ObservationHandle?

    init(appGraph: SharedAppGraph) {
        stateHolder = appGraph.expenseDashboardStateHolder()
        sync(with: stateHolder.currentState)
        observationHandle = stateHolder.watch { [weak self] state in
            DispatchQueue.main.async {
                self?.sync(with: state)
            }
        }
    }

    func load() {
        stateHolder.load()
    }

    func selectPeriod(_ period: String) {
        switch period {
        case "Week":
            stateHolder.selectWeek()
        case "Year":
            stateHolder.selectYear()
        default:
            stateHolder.selectMonth()
        }
    }

    func saveEntry(
        title: String,
        amountText: String,
        category: String,
        note: String,
        isIncome: Bool
    ) {
        stateHolder.saveEntry(
            title: title,
            amountText: amountText,
            category: category,
            note: note,
            isIncome: isIncome
        )
    }

    deinit {
        observationHandle?.dispose()
    }

    private func sync(with state: ExpenseDashboardUiState) {
        isLoading = state.isLoading
        isSaving = state.isSaving
        balanceLabel = state.balanceLabel
        incomeLabel = state.incomeLabel
        expenseLabel = state.expenseLabel
        selectedPeriodLabel = state.selectedPeriodLabel
        isEmpty = state.isEmpty
        errorMessage = state.errorMessage
        summaryCards = state.summaryCards.map { card in
            SummaryCardModel(
                id: card.title,
                title: card.title,
                amountLabel: card.amountLabel,
                caption: card.caption,
                accentHex: card.accentHex
            )
        }
        availableCategories = state.availableCategories.map { category in
            CategoryOptionModel(
                id: category.name,
                name: category.name,
                accentHex: category.accentHex
            )
        }
        chartPoints = state.chartPoints.map { point in
            ChartPointModel(
                id: point.label,
                label: point.label,
                amount: point.amount
            )
        }
        categories = state.categories.map { category in
            CategoryCardModel(
                id: category.name,
                name: category.name,
                amountLabel: category.amountLabel,
                shareLabel: category.shareLabel,
                progress: Double(category.progress),
                accentHex: category.accentHex
            )
        }
        recentTransactions = state.recentTransactions.map { transaction in
            TransactionRowModel(
                id: transaction.id,
                title: transaction.title,
                subtitle: transaction.subtitle,
                dateLabel: transaction.dateLabel,
                category: transaction.category,
                amountLabel: transaction.amountLabel,
                accentHex: transaction.accentHex,
                isIncome: transaction.isIncome
            )
        }
    }
}
