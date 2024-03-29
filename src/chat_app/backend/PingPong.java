package chat_app.backend;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import readiefur.misc.Pair;
import readiefur.sockets.ServerClientHost;
import readiefur.sockets.ServerManager;

import chat_app.backend.net_data.EType;
import chat_app.backend.net_data.EmptyPayload;
import chat_app.backend.net_data.NetMessage;

public class PingPong extends Thread
{
    ////20 seconds seems to be the standard interval for a websocket ping/pong so I will mimic that.
    public static final int PING_PONG_INTERVAL_MS = 10_000;

    private ServerManager serverManager;
    private ConcurrentHashMap<UUID, Boolean> pongedPeers = new ConcurrentHashMap<>();

    public PingPong(ServerManager serverManager)
    {
        this.serverManager = serverManager;
        serverManager.onMessage.Add(this::OnMessage);
    }

    @Override
    public void run()
    {
        //Try to set the thread name to the class name, not required but useful for debugging.
        try { setName(getClass().getSimpleName()); }
        catch (Exception e) {}

        while (true)
        {
            try
            {
                Thread.sleep(PING_PONG_INTERVAL_MS);
            }
            catch (Exception e) {}
            if (serverManager.IsDisposed() || !this.isAlive())
                break;

            //Disconnect any peers that haven't responded to a ping.
            pongedPeers.forEach((UUID peerID, Boolean ponged) ->
            {
                if (!ponged)
                {
                    try { serverManager.DisconnectClient(peerID); }
                    catch (Exception ex) {}
                }
            });

            //Reset tje pongedPeers dictionary.
            pongedPeers.clear();
            //We only want to add peers for the current frame.
            serverManager.GetClientHosts().forEach((UUID peerID, ServerClientHost serverClientHost) -> pongedPeers.put(peerID, false));

            //Send a new ping.
            NetMessage<EmptyPayload> message = new NetMessage<>();
            message.type = EType.PING;
            message.payload = new EmptyPayload();
            serverManager.BroadcastMessage(message);
        }
    }

    private void OnMessage(Pair<UUID, Object> message)
    {
        NetMessage<?> netMessage = (NetMessage<?>)message.item2;

        if (netMessage.type != EType.PONG)
            return;

        if (pongedPeers.containsKey(message.item1))
            pongedPeers.put(message.item1, true);
    }
}
