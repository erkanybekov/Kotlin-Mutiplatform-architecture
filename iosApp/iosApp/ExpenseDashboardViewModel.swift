import SwiftUI
import shared

enum ExpenseEntryKind: String, CaseIterable, Identifiable {
    case expense = "Expense"
    case income = "Income"

    var id: String { rawValue }
    var isIncome: Bool { self == .income }
}

enum ExpenseDashboardPeriod: String, CaseIterable, Identifiable {
    case week = "Week"
    case month = "Month"
    case year = "Year"

    var id: String { rawValue }

    init(label: String) {
        switch label {
        case Self.week.rawValue:
            self = .week
        case Self.year.rawValue:
            self = .year
        default:
            self = .month
        }
    }
}

enum ExpenseDashboardViewIntent {
    case load
    case dismissError
    case titleChanged(String)
    case amountChanged(String)
    case noteChanged(String)
    case entryTypeChanged(ExpenseEntryKind)
    case categorySelected(String)
    case periodSelected(ExpenseDashboardPeriod)
    case submitEntry
    case deleteEntry(Int64)
}

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

struct ExpenseEntryDraftViewState: Equatable {
    let title: String
    let amount: String
    let note: String
    let entryType: ExpenseEntryKind
    let selectedCategory: String
    let titleError: String?
    let amountError: String?
    let categoryError: String?

    static let empty = ExpenseEntryDraftViewState(
        title: "",
        amount: "",
        note: "",
        entryType: .expense,
        selectedCategory: "",
        titleError: nil,
        amountError: nil,
        categoryError: nil
    )
}

struct ExpenseDashboardViewState: Equatable {
    let isLoading: Bool
    let isSaving: Bool
    let balanceLabel: String
    let incomeLabel: String
    let expenseLabel: String
    let selectedPeriod: ExpenseDashboardPeriod
    let summaryCards: [SummaryCardModel]
    let availableCategories: [CategoryOptionModel]
    let chartPoints: [ChartPointModel]
    let categories: [CategoryCardModel]
    let recentTransactions: [TransactionRowModel]
    let isEmpty: Bool
    let errorMessage: String?
    let draft: ExpenseEntryDraftViewState

    static let empty = ExpenseDashboardViewState(
        isLoading: false,
        isSaving: false,
        balanceLabel: "$0.00",
        incomeLabel: "$0.00",
        expenseLabel: "$0.00",
        selectedPeriod: .month,
        summaryCards: [],
        availableCategories: [],
        chartPoints: [],
        categories: [],
        recentTransactions: [],
        isEmpty: true,
        errorMessage: nil,
        draft: .empty
    )
}

@MainActor
final class ExpenseDashboardViewModel: ObservableObject {
    @Published private(set) var viewState = ExpenseDashboardViewState.empty

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

    func send(_ intent: ExpenseDashboardViewIntent) {
        switch intent {
        case .load:
            stateHolder.load()
        case .dismissError:
            stateHolder.clearError()
        case .titleChanged(let value):
            stateHolder.updateTitle(value: value)
        case .amountChanged(let value):
            stateHolder.updateAmount(value: value)
        case .noteChanged(let value):
            stateHolder.updateNote(value: value)
        case .entryTypeChanged(let kind):
            stateHolder.updateEntryType(isIncome: kind.isIncome)
        case .categorySelected(let category):
            stateHolder.selectCategory(category: category)
        case .periodSelected(let period):
            switch period {
            case .week:
                stateHolder.selectWeek()
            case .month:
                stateHolder.selectMonth()
            case .year:
                stateHolder.selectYear()
            }
        case .submitEntry:
            stateHolder.submitEntry()
        case .deleteEntry(let id):
            stateHolder.deleteEntry(id: id)
        }
    }

    deinit {
        observationHandle?.dispose()
    }

    private func sync(with state: ExpenseDashboardUiState) {
        viewState = ExpenseDashboardViewState(
            isLoading: state.isLoading,
            isSaving: state.isSaving,
            balanceLabel: state.balanceLabel,
            incomeLabel: state.incomeLabel,
            expenseLabel: state.expenseLabel,
            selectedPeriod: ExpenseDashboardPeriod(label: state.selectedPeriodLabel),
            summaryCards: state.summaryCards.map { card in
                SummaryCardModel(
                    id: card.title,
                    title: card.title,
                    amountLabel: card.amountLabel,
                    caption: card.caption,
                    accentHex: card.accentHex
                )
            },
            availableCategories: state.availableCategories.map { category in
                CategoryOptionModel(
                    id: category.name,
                    name: category.name,
                    accentHex: category.accentHex
                )
            },
            chartPoints: state.chartPoints.map { point in
                ChartPointModel(
                    id: point.label,
                    label: point.label,
                    amount: point.amount
                )
            },
            categories: state.categories.map { category in
                CategoryCardModel(
                    id: category.name,
                    name: category.name,
                    amountLabel: category.amountLabel,
                    shareLabel: category.shareLabel,
                    progress: Double(category.progress),
                    accentHex: category.accentHex
                )
            },
            recentTransactions: state.recentTransactions.map { transaction in
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
            },
            isEmpty: state.isEmpty,
            errorMessage: state.errorMessage,
            draft: ExpenseEntryDraftViewState(
                title: state.entryDraft.title,
                amount: state.entryDraft.amount,
                note: state.entryDraft.note,
                entryType: state.entryDraft.isIncome ? .income : .expense,
                selectedCategory: state.entryDraft.selectedCategory,
                titleError: state.entryDraft.titleError,
                amountError: state.entryDraft.amountError,
                categoryError: state.entryDraft.categoryError
            )
        )
    }
}
