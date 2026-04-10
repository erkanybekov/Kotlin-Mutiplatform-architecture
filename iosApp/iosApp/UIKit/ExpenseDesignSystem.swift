import SwiftUI
import UIKit

enum ExpensePalette {
    static let background = Color(uiColor: .systemGroupedBackground)
    static let surface = Color(uiColor: .secondarySystemGroupedBackground)
    static let surfaceMuted = Color(uiColor: .secondarySystemGroupedBackground)
    static let surfaceStrong = Color(uiColor: .secondarySystemGroupedBackground)
    static let surfaceInset = Color(uiColor: .tertiarySystemGroupedBackground)
    static let surfaceChip = Color(uiColor: .tertiarySystemGroupedBackground)
    static let textPrimary = Color.primary
    static let textSecondary = Color.secondary
    static let textMuted = Color(uiColor: .tertiaryLabel)
    static let accentWarm = Color(hex: "#FF8A5B")
    static let accentGold = Color(hex: "#FFBE4D")
    static let accentSuccess = Color(hex: "#2AD5A6")
    static let accentIndigo = Color(hex: "#7C83FF")
    static let error = Color(hex: "#FF6E7A")
}

private extension Color {
    init(hex: String) {
        let cleaned = hex.replacingOccurrences(of: "#", with: "")
        var value: UInt64 = 0
        Scanner(string: cleaned).scanHexInt64(&value)

        let red: Double
        let green: Double
        let blue: Double

        switch cleaned.count {
        case 6:
            red = Double((value >> 16) & 0xFF) / 255
            green = Double((value >> 8) & 0xFF) / 255
            blue = Double(value & 0xFF) / 255
        default:
            red = 1
            green = 0.745
            blue = 0.302
        }

        self.init(.sRGB, red: red, green: green, blue: blue, opacity: 1)
    }
}

extension Color {
    static func expense(hex: String) -> Color {
        Color(hex: hex)
    }
}
