package irc.simple.valentinshelkov.simpleircclient.utils;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Vector;

import irc.simple.valentinshelkov.simpleircclient.data.ClientInfo;

public class ClientSender extends Thread {
    private final Vector<String> messageQueue = new Vector<String>();
    private final ServerDispatcher serverDispatcher;
    private final ClientInfo clientInfo;
    private PrintWriter printWriter;


    public ClientSender(ClientInfo aClientInfo, ServerDispatcher aServerDispatcher) {
        clientInfo = aClientInfo;
        serverDispatcher = aServerDispatcher;
    }

    public void init() throws IOException {
        Socket socket = clientInfo.getSocket();
        printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    public synchronized void sendMessage(String message) {
        messageQueue.add(message);
        notify();
    }

    private synchronized String getNextMessageFromQueue() throws InterruptedException {
        while (messageQueue.size() == 0) {
            wait();
        }
        String message = (String) messageQueue.get(0);
        messageQueue.removeElementAt(0);
        return message;

    }

    private void sendMessageToClient(String message) {
        printWriter.println(message);
        printWriter.flush();
    }

    public void run() {
        try {
            while (!isInterrupted()) {
                String message = getNextMessageFromQueue();
                sendMessageToClient(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        clientInfo.getClientSender().interrupt();
        serverDispatcher.deleteClient(clientInfo);
    }
}