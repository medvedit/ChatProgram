package ru.medwedSa.javaTwe.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ServerSocketThread extends Thread{
    private int port;
    private int timeout;
    private ServerSocketThreadListener listener;

    public ServerSocketThread(ServerSocketThreadListener listener, String log, int port, int timeout) {
        super(log);
        this.port = port;
        this.timeout = timeout;
        this.listener = listener;
        start();
    }

    @Override
    public void run() {
        listener.onServerStart(this);
        try (ServerSocket server = new ServerSocket(port)) {
            server.setSoTimeout(timeout);
            listener.onServerSocketCreated(this, server);
            while (!isInterrupted()) { // будет работать пока в метод не прилетит interrupt();
                Socket client;
                try {
                    client = server.accept();
                } catch (SocketTimeoutException e) {
                    listener.onServerSoTimeout(this, server);
                    continue;
                }
                listener.onSocketAccepted(this, server, client);
            }
        } catch (IOException e) {
            listener.onServerException(this, e);
        } finally {
            listener.onServerStop(this);
        }
    }
}
