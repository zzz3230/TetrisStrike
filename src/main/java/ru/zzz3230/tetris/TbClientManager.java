package ru.zzz3230.tetris;

import ru.zzz3230.tblib.TbConnection;
import ru.zzz3230.tblib.client.LocalClient;
import ru.zzz3230.tblib.client.NetworkClient;
import ru.zzz3230.tetris.model.MainMenuContext;

public class TbClientManager {
    private NetworkClient networkClient;
    private final LocalClient localClient;

    public TbClientManager() {
        TbConnection tbConnection = new TbConnection("game-tetris-backend", "F2RHTute.qv2MBaUW");
        tbConnection.initialize();
        localClient = new LocalClient();
        networkClient = new NetworkClient(tbConnection, localClient);
    }

    public NetworkClient getNetworkClient() {
        return networkClient;
    }
    public LocalClient getLocalClient() {
        return localClient;
    }
}
