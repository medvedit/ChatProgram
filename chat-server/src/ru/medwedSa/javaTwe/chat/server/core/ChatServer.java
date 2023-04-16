package ru.medwedSa.javaTwe.chat.server.core;

import ru.medwedSa.javaTwe.network.ServerSocketThread;
import ru.medwedSa.javaTwe.network.ServerSocketThreadListener;
import ru.medwedSa.javaTwe.network.SocketThread;
import ru.medwedSa.javaTwe.network.SocketThreadListener;

import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class ChatServer implements ServerSocketThreadListener, SocketThreadListener { //
    private final DateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");
    private final int SERVER_SOCKET_TIMEOUT = 2000;
    int counter = 0;
    ServerSocketThread server; // создали server от класса ServerSocketThread

    public void start(int port) { // метод запуска чат_сервера
        if (server != null && server.isAlive()) { // если сервер существует (активен) И уже живой (работает),
            System.out.println("Сервер уже работает!"); // просто выводим лог в консоль
        } else { // иначе запускаем сервер
            server = new ServerSocketThread(this,"Чат_приложение." + counter++, port, SERVER_SOCKET_TIMEOUT); // говорим, что server это новый ServerSocketThread
        }
    }
    public void stop() { // метод остановки чат_сервера
        if (server == null || !server.isAlive()) { // если сервер не существует (не активен) ИЛИ сервер не живой, то
            System.out.println("Сервер уже остановлен или еще не запускался..."); // выводим лог в консоль
        } else { // иначе останавливаем сервер
            server.interrupt(); // отправили команду прерывания работы сервера в ServerSocketThread, в метод run
        }
    }
    private void putLog(String msg) {
        msg = DATE_FORMAT.format(System.currentTimeMillis()) + " " + Thread.currentThread().getName() + ": " + msg;
        System.out.println(msg);
    }

    //<editor-fold desc="Переопределенные метода от интерфейса ServerSocketThreadListener">
    @Override
    public void onServerStart(ServerSocketThread thread) {
        putLog("Сервер запущен...");
    }

    @Override
    public void onServerStop(ServerSocketThread thread) {
        putLog("Сервер сокет остановлен.");
    }

    @Override
    public void onServerSocketCreated(ServerSocketThread t, ServerSocket s) {
        putLog("Сервер сокет создан...");
    }

    @Override
    public void onServerSoTimeout(ServerSocketThread t, ServerSocket s) {
//        putLog("Таймаут сервера");
    }

    @Override
    public void onSocketAccepted(ServerSocketThread t, ServerSocket s, Socket client) {
        putLog("Клиент подключается...");
        String name = "SocketThread" + client.getInetAddress() + ": " +  client.getPort();
        new SocketThread(this, name, client);
    }

    @Override
    public void onServerException(ServerSocketThread t, Throwable e) {
        putLog("Сервер остановлен!");
        e.printStackTrace();
    }
    //</editor-fold>

     //<editor-fold desc="Переопределенные метода от интерфейса SocketThreadListener">
    @Override
    public void onSocketStart(SocketThread t, Socket s) {
        putLog("Клиент подключился.");
    }

    @Override
    public void onSocketStop(SocketThread t) {
        putLog("Клиент отключен.");
    }

    @Override
    public void onSocketReady(SocketThread t, Socket socket) {
        putLog("Клиент готов к общению...");
    }

    @Override
    public void onReceiveString(SocketThread t, Socket s, String msg) {
        t.sendMessage("echo: " + msg);
    }

    @Override
    public void onSocketException(SocketThread t, Throwable e) {
        e.printStackTrace();
    }
    //</editor-fold
}

