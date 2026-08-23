package server;

import common.JsonPacket;

/**
 * Interface that decouples the web engine from the application logic.
 */
public interface ServerListener {
    void onClientConnected(ClientSession client);
    void onClientDisconnected(ClientSession client);
    void onPacketReceived(ClientSession client, JsonPacket packet);
}
