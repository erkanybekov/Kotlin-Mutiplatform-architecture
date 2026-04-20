import SwiftUI
import shared

struct ContentView: View {
    private let appGraph: SharedAppGraph

    init(appGraph: SharedAppGraph) {
        self.appGraph = appGraph
    }

    var body: some View {
        ChatAppView(appGraph: appGraph)
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView(appGraph: SharedAppGraph(storageDirectoryPath: NSTemporaryDirectory()))
    }
}
