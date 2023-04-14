package ru.medwedSa.javaTwe.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ServerSocketThread extends Thread{ // Создали многопоточный  класс ServerSocketThread,
                                                // который будет вызываться в ChatServer.
    private int port;
    private int timeout;
    private ServerSocketThreadListener listener;

    public ServerSocketThread(ServerSocketThreadListener listener, String log, int port, int timeout) { // Конструктор метода принимающий имя и порт.
        super(log);
        this.port = port;
        this.timeout = timeout;
        this.listener = listener;
        start(); // Запустили сервер.
    }

    @Override // Переопределенный метод run
    public void run() { // Который будет работать пока в метод не прилетит interrupt();
        listener.onServerStart(this);
//        System.out.println("Сервер подключен!"); // Лог в консоль при запуске.
        try (ServerSocket server = new ServerSocket(port)) {
            server.setSoTimeout(timeout);
            listener.onServerSocketCreated(this, server);
//            System.out.println("Серверный сокет создан...");
            while (!isInterrupted()) {
                Socket client;
                try {
                    client = server.accept();
                } catch (SocketTimeoutException e) {
                    listener.onServerSoTimeout(this, server);
//                    e.printStackTrace();
                    continue;
                }
                listener.onSocketAccepted(this, server, client);
//                System.out.println("Клиент соединился...");
            }
        } catch (IOException e) {
            listener.onServerException(this, e);
//            e.printStackTrace();
        } finally {
            listener.onServerStop(this);
//            System.out.println("Серверный сокет отключился.");
        }
//        System.out.println("Сервер отключен!"); // Лог в консоль при остановке.
    }
}
