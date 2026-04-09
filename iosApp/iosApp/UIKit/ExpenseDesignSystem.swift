import SwiftUI

enum ExpensePalette {
    static let background = Color(hex: "#090E1A")
    static let backgroundTop = Color(hex: "#10192E")
    static let backgroundBottom = Color(hex: "#080C16")
    static let surface = Color(hex: "#10192E")
    static let surfaceMuted = Color(hex: "#11192D")
    static let surfaceStrong = Color(hex: "#121A2D")
    static let surfaceInset = Color(hex: "#0E1528")
    static let surfaceChip = Color(hex: "#18223A")
    static let textPrimary = Color.white
    static let textSecondary = Color(hex: "#93A0BE")
    static let textMuted = Color(hex: "#6F7C99")
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
