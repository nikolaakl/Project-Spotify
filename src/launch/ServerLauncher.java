package launch;

import server.SpotifyServer;

public class ServerLauncher {
    static void main() {
        new SpotifyServer().start();
    }
}