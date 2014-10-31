package irc.simple.valentinshelkov.simpleircclient.utils;

import java.net.Socket;
import java.util.Vector;

import irc.simple.valentinshelkov.simpleircclient.data.ClientInfo;

public class ServerDispatcher extends Thread {
    private final Vector<String> messageQueue = new Vector<String>();
    private final Vector<ClientInfo> clients = new Vector<ClientInfo>();

    public synchronized void addClient(ClientInfo сlientInfo) {
        clients.add(сlientInfo);
    }

    public synchronized void deleteClient(ClientInfo clientInfo) {
        int clientIndex = clients.indexOf(clientInfo);
        if (clientIndex != -1) {
            clients.removeElementAt(clientIndex);
        }
    }

    public synchronized void dispatchMessage(ClientInfo clientInfo, String message) {
        Socket socket = clientInfo.getSocket();
        String senderIP = socket.getInetAddress().getHostAddress();
        String senderPort = "" + socket.getPort();
        message = senderIP + ":" + senderPort + ": " + message;
        messageQueue.add(message);
        notify();
    }

    private synchronized String getNextMessageFromQueue() throws InterruptedException {
        while (messageQueue.size() == 0) {
            wait();
        }
        String message = messageQueue.get(0);
        messageQueue.removeElementAt(0);
        return message;
    }

    private synchronized void sendMessageToAllClients(String message) {
        for (int i = 0; i < clients.size(); i++) {
            ClientInfo clientInfo = clients.get(i);
            clientInfo.getClientSender().sendMessage(message);
        }
    }

    public void run() {
        try {
            while (true) {
                String message = getNextMessageFromQueue();
                sendMessageToAllClients(message);
            }
        } catch (InterruptedException ie) {
            // Thread interrupted. Stop its execution
            return;
        }
    }
}

 