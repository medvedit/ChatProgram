package ru.medwedSa.javaTwe.chat.server.core;

import ru.medwedSa.javaTwe.chat.common.Messages;
import ru.medwedSa.javaTwe.network.ServerSocketThread;
import ru.medwedSa.javaTwe.network.ServerSocketThreadListener;
import ru.medwedSa.javaTwe.network.SocketThread;
import ru.medwedSa.javaTwe.network.SocketThreadListener;

import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Vector;

public class ChatServer implements ServerSocketThreadListener, SocketThreadListener { //
    private final DateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");
    private final int SERVER_SOCKET_TIMEOUT = 2000;
    private final Vector<SocketThread> clients = new Vector<>();

    int counter = 0;
    ServerSocketThread server; // создали server от класса ServerSocketThread
    ChatSeverListener listener;

    public ChatServer(ChatSeverListener listener) {
        this.listener = listener;
    }


    public void start(int port) { // метод запуска чат_сервера
        if (server != null && server.isAlive()) { // если сервер существует (активен) И уже живой (работает),
            putLog("Сервер уже работает!"); // просто выводим лог в консоль
        } else { // иначе запускаем сервер
            server = new ServerSocketThread(this,"Чат_приложение-" + counter++, port, SERVER_SOCKET_TIMEOUT); // говорим, что server это новый ServerSocketThread
        }
    }

    public void stop() { // метод остановки чат_сервера
        if (server == null || !server.isAlive()) { // если сервер не существует (не активен) ИЛИ сервер не живой, то
            putLog("Сервер уже остановлен или еще не запускался..."); // выводим лог в консоль
        } else { // иначе останавливаем сервер
            server.interrupt(); // отправили команду прерывания работы сервера в ServerSocketThread, в метод run
        }
    }

    private void putLog(String msg) {
        msg = DATE_FORMAT.format(System.currentTimeMillis()) +
                " " + Thread.currentThread().getName() + ": " + msg;
        listener.onChatServerMessage(msg);
    }

    //<editor-fold desc="Переопределенные метода от интерфейса ServerSocketThreadListener">
    @Override
    public void onServerStart(ServerSocketThread thread) {
        putLog("Сервер запущен...");
        SqlClient.connect(); // Подключаемся к базе данных.
    }

    @Override
    public void onServerStop(ServerSocketThread thread) {
        putLog("Сервер сокет остановлен.");
        SqlClient.disconnect(); // Отключаемся от базы данных.
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
        new ClientThread(this, name, client); // И раз мы создали дополнительные свойства у ClientThread,
                                        // то и в этой строке создаем не SocketThread (как было ранее), а ClientThread.
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
        clients.remove(t);
    }

    @Override
    public void onSocketReady(SocketThread t, Socket socket) {
        putLog("Клиент готов к общению...");
        clients.add(t);
    }

    @Override
    public void onReceiveString(SocketThread t, Socket s, String msg) {
        ClientThread client = (ClientThread) t;
        if (client.isAuthorized()) { // Если клиент авторизован, то
            handleAuthMsg(client, msg);
        } else { // Иначе
            handleNonAuthMsg(client, msg);
        }
    }

    @Override
    public void onSocketException(SocketThread t, Throwable e) {
        e.printStackTrace();
    }
    //</editor-fold

    private void handleNonAuthMsg(ClientThread client, String msg) {
        String[] arr = msg.split(Messages.DELIMITER);
        if (arr.length != 3 || !arr[0].equals(Messages.AUTH_REQUEST)) {
            client.msgFormatError(msg);
            return;
        }
        String login = arr[1];
        String password = arr[2];
        String nickname = SqlClient.getNick(login, password);
        if (nickname == null) {
            putLog("Ошибка ввода login " + login);
            client.authFail();
            return;
        }
        client.authAccept(nickname);
        sendToAllAuthorized(Messages.getMsgBroadcast("Server connect", nickname + " подключен."));
    }

    private void handleAuthMsg(ClientThread client, String msg) {
        sendToAllAuthorized(msg);
    }

    private void sendToAllAuthorized(String msg) {
        for (SocketThread socketThread : clients) {
            ClientThread client = (ClientThread) socketThread;
            if (!client.isAuthorized()) {
                continue;
            } else {
                client.sendMessage(msg);
            }
        }
    }
}

