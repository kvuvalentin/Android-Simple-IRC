package irc.simple.valentinshelkov.simpleircclient.data;

import java.net.Socket;

import irc.simple.valentinshelkov.simpleircclient.listener.ClientListener;
import irc.simple.valentinshelkov.simpleircclient.utils.ClientSender;

public class ClientInfo {
    private Socket socket;
    private ClientListener clientListener;
    private ClientSender clientSender;

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public ClientListener getClientListener() {
        return clientListener;
    }

    public void setClientListener(ClientListener clientListener) {
        this.clientListener = clientListener;
    }

    public ClientSender getClientSender() {
        return clientSender;
    }

    public void setClientSender(ClientSender clientSender) {
        this.clientSender = clientSender;
    }
}