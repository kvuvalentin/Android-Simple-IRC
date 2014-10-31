package irc.simple.valentinshelkov.simpleircclient.listener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import irc.simple.valentinshelkov.simpleircclient.data.ClientInfo;
import irc.simple.valentinshelkov.simpleircclient.utils.ServerDispatcher;

public class ClientListener extends Thread {
    private final ServerDispatcher serverDispatcher;
    private final ClientInfo clientInfo;
    private BufferedReader bufferedReader;

    public ClientListener(ClientInfo clientInfo, ServerDispatcher serverDispatcher) {
        this.clientInfo = clientInfo;
        this.serverDispatcher = serverDispatcher;
    }

    public void init() throws IOException {
        Socket socket = clientInfo.getSocket();
        bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public void run() {
        try {
            while (!isInterrupted()) {
                String message = bufferedReader.readLine();
                if (message == null) break;
                serverDispatcher.dispatchMessage(clientInfo, message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        clientInfo.getClientSender().interrupt();
        serverDispatcher.deleteClient(clientInfo);
    }
}