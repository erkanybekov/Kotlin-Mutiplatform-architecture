import SwiftUI
import shared

@main
struct iOSApp: App {
    private let appGraph = SharedAppGraph(storageDirectoryPath: Self.storageDirectoryPath)

    private static let storageDirectoryPath: String = {
        FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first?.path ?? NSTemporaryDirectory()
    }()

	var body: some Scene {
		WindowGroup {
			ContentView(appGraph: appGraph)
		}
	}
}
