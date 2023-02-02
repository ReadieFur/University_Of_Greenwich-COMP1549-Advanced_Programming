package readiefur.helpers.sockets;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import readiefur.helpers.Event;

public class ServerClientHost extends Thread
{
    private Boolean isDisposed = false;
    private Socket socket;

    //TODO: Accessability modifiers.
    public Event<Object> onMessage = new Event<>();
    public Event<Void> onClose = new Event<>();
    public Event<Exception> onError = new Event<>();

    public ServerClientHost(Socket socket)
    {
        this.socket = socket;
        start();
    }

    //TODO: Needs fixing.
    public void Dispose()
    {
        if (isDisposed)
            return;
        isDisposed = true;

        //Close the socket.
        try { socket.close(); }
        catch (Exception ex) { onError.Invoke(ex); }
        socket = null;
        onClose.Invoke(null);

        //If the thread is still running, interrupt it.
        try
        {
            if (this.isAlive())
                this.interrupt();
        }
        catch (Exception ex) { onError.Invoke(ex); }
    }

    //Called immediately after construction.
    @Override
    public void run()
    {
        if (isDisposed)
        {
            this.interrupt();
            return; //I don't think this is needed if the thread interrupt is called.
        }

        ObjectInputStream inputStream;
        try { inputStream = new ObjectInputStream(socket.getInputStream()); }
        catch (Exception ex)
        {
            onError.Invoke(ex);
            return;
        }

        while (!isDisposed && !socket.isClosed())
        {
            Object data;
            try { data = inputStream.readObject(); }
            catch (Exception ex)
            {
                onError.Invoke(ex);
                continue;
            }

            onMessage.Invoke(data);
        }

        Dispose();
    }

    public void SendMessage(Object data)
    {
        if (isDisposed)
            return;

        try
        {
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.writeObject(data);
        }
        catch (Exception ex)
        {
            onError.Invoke(ex);
        }
    }
}