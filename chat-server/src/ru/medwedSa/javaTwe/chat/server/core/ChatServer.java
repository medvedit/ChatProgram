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
            server = new ServerSocketThread(this, "Чат_приложение-" + counter++, port, SERVER_SOCKET_TIMEOUT); // говорим, что server это новый ServerSocketThread
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
        for (SocketThread client : clients) { // все клиенты остановлены при остановке сервера.
            client.close();
        }
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
        String name = "SocketThread" + client.getInetAddress() + ": " + client.getPort();
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
    public synchronized void onSocketStart(SocketThread t, Socket s) {
        putLog("Клиент подключился.");
    }

    @Override
    public synchronized void onSocketStop(SocketThread t) {
        ClientThread client = (ClientThread) t; // Взяли ние авторизованного клиента.
        clients.remove(client); // Клиент удаляется...
        if (client.isAuthorized() && client.isReconnecting()) { // Если наш клиент был вообще авторизован, то
            sendToAllAuthorized(Messages.getMsgBroadcast("На сервере ",
                    client.getNickname() + " отключился.")); // Отправляем всем подключенным клиентам сообщение
            // том, что клиент отключился.
        }
        sendToAllAuthorized(Messages.getUserList(getUsers()));
    }

    @Override
    public synchronized void onSocketReady(SocketThread t, Socket socket) {
        putLog("Клиент готов к общению...");
        clients.add(t);
    }

    @Override
    public synchronized void onReceiveString(SocketThread t, Socket s, String msg) {
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

    /**
     * 1. Если строка не такая, которую ожидаем || у желающего подключиться пользователя не соответствует
     * login || password - вернули msgFormatError
     * 2. Если nickname пустой -> ошибка авторизации, отключили со стороны сервера.
     * 3. Иначе принимаем авторизацию пользователя, подключаем сос стороны сервера и сообщаем всем авторизованным
     * об подключении этого nickname.
     * @param client
     * @param msg
     */
    private void handleNonAuthMsg(ClientThread client, String msg) {
        String[] arr = msg.split(Messages.DELIMITER);
        if (arr.length != 3 || !arr[0].equals(Messages.AUTH_REQUEST)) {
            client.msgFormatError(msg);
            return;
        }
        String login = arr[1]; // Забрали login.
        String password = arr[2]; // Забрали password.
        String nickname = SqlClient.getNick(login, password); // Если тут -> SqlClient.getNick все ok,то забрали nickname.
        if (nickname == null) {
            putLog("Ошибка ввода login " + login);
            client.authFail(); // отключаем клиента со стороны сервера.
            return;
        } else {
            ClientThread oldClient = findClientByNickname(nickname);
            client.authAccept(nickname); // Иначе принимаем авторизацию пользователя
            if (oldClient == null) {
                sendToAllAuthorized(Messages.getMsgBroadcast("Server connect", nickname + " подключен."));
            } else {
                oldClient.reconnect();
                clients.remove(oldClient);
            }
        }
        sendToAllAuthorized(Messages.getUserList(getUsers()));
    }

    private void handleAuthMsg(ClientThread client, String msg) {
        String[] arr = msg.split(Messages.DELIMITER);
        String msgType = arr[0];
        switch (msgType) {
            case Messages.USER_BROADCAST:
                sendToAllAuthorized(Messages.getMsgBroadcast(client.getNickname(), arr[1]));
                break;
            default:
                client.msgFormatError(msg);
        }
    }

    private synchronized void sendToAllAuthorized(String msg) {
        for (SocketThread socketThread : clients) {
            ClientThread client = (ClientThread) socketThread;
            if (!client.isAuthorized()) {
                continue;
            } else {
                client.sendMessage(msg);
            }
        }
    }

    private String getUsers() { // Собираем всех авторизованных пользователей.
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < clients.size(); i++) {
            ClientThread client = (ClientThread) clients.get(i);
            if (!client.isAuthorized()) continue; // если не авторизован - ничего не делаем
            sb.append(client.getNickname()).append(Messages.DELIMITER); // иначе в sb добавили пользователя
            // с никнеймом + делиметр.
        }
        return sb.toString();
    }

    /**
     * Для того, что бы реализовывать в приложении возможность авторизации с разных устройств для одного пользователя
     * или наоборот, запрет таких действий или .... Нужно понимать, а есть ли в данный момент времени,
     * зарегистрированный/подключенный пользователь с таким nickname.
     * @param nickname
     * @return
     */
    private synchronized ClientThread findClientByNickname(String nickname) { // Найти клиента по нику.
        for (int i = 0; i < clients.size(); i++) {
            ClientThread client = (ClientThread) clients.get(i);
            if (!client.isAuthorized()) continue; // если не авторизован - ничего не делаем
            if (client.getNickname().equals(nickname)) // если есть совпадение, то
                return client; // сообщили об этом.
        }
        return null; // иначе...
    }
}

