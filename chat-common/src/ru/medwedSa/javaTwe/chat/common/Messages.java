package ru.medwedSa.javaTwe.chat.common;

public class Messages { // Протоколы сообщений. Формирует единообразные сообщения и на клиенте, и на сервере.
    /**
     * Префиксы. Будут находиться вначале выводимых сообщений о статусах подключаемых к чату пользователей.
     */
    public static final String DELIMITER = "<<<&>>>"; // Делитель между блоками в выводимом сообщении.
    public static final String AUTH_REQUEST = "/auth_request"; // Запрос авторизации.
    public static final String AUTH_ACCEPT = "/auth_accept"; // Состоялась авторизация.
    public static final String AUTH_DENY = "/auth_deny"; // Разрыв авторизации.
    public static final String MSG_BROADCAST = "/bcast"; // Сообщение для всех участников сети.
    public static final String MSG_FORMAT_ERROR = "/msg_error"; // Не смог прочитать сообщение. Ошибка чтения.

    public static String getAuthRequest(String login, String password) { // Протокольный запрос авторизации по
                                                                          // переданному login, password.
        return AUTH_REQUEST + DELIMITER + login + DELIMITER + password;
    }

    public static String getAuthAccept(String nickname) { // Подтверждение (будет от сервера) авторизации по nickname.
        return AUTH_ACCEPT + DELIMITER + nickname;
    }

    public static String getAuthDeny() { // Отказ в авторизации.
        return AUTH_DENY;
    }

    public static String getMsgFormatError(String massage) { // Мы не смогли распознать ваше сообщение.
                                                         // Отсылает обратно сообщение , которое не смогли распознать.
        return MSG_FORMAT_ERROR + DELIMITER + massage;

    }
    public static String getMsgBroadcast(String src, String message) { // От кого и что, какое сообщение послано.
                                                                        // Сообщение всем авторизованным пользователям.
        return MSG_BROADCAST + DELIMITER +  System.currentTimeMillis() +
                DELIMITER + src + DELIMITER + message;
    }
}
