package ru.medwedSa.javaTwe.chat.server.core;

import ru.medwedSa.javaTwe.chat.common.Messages;
import ru.medwedSa.javaTwe.network.SocketThread;
import ru.medwedSa.javaTwe.network.SocketThreadListener;

import java.net.Socket;

public class ClientThread extends SocketThread { // Расширяет функционал клиента со стороны сервера относительно SocketThread.
    private String nickname; // nickname внутри метода.
    private boolean isAuthorized; // Aвторизован.

    public ClientThread(SocketThreadListener listener, String name, Socket socket) { // Конструктор.
        super(listener, name, socket);
    }

    public String getNickname() { // Геттер.
        return nickname;
    }

    public boolean isAuthorized() { // Геттер.
        return isAuthorized;
    }

    void authAccept(String nickname) { // Принимает авторизацию со стороны сервера.
        isAuthorized = true;
        this.nickname = nickname;
        sendMessage(Messages.getAuthAccept(nickname));
    }

    void authFail() { // Ошибка авторизации со стороны сервера.
        sendMessage(Messages.getAuthDeny());
        close(); // со стороны сервера закрыли сокет.
    }

    void msgFormatError(String msg) { // На случай нераспознанного сообщения.
        sendMessage(Messages.getMsgFormatError(msg)); // "Клиент, мы тебя не поняли, высылаем обратно
                                                              // сообщение, которое мы не поняли."
        close(); // со стороны сервера закрыли сокет.
    }
}
