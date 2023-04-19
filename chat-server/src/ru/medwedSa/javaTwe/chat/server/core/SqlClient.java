package ru.medwedSa.javaTwe.chat.server.core;

import java.sql.*;

public class SqlClient { // Класс который, будет работать с БД
    private static Connection connection; // Для работы с JDBC.
    private static Statement statement;

    synchronized static void connect() {
        try {
            Class.forName("org.sqlite.JDBC"); // Инициализация драйвера.
            connection = DriverManager.getConnection("jdbc:sqlite:chat-server/client-db.sqlite"); // У драйвера запрос соединения.
            statement = connection.createStatement(); // У соединения запрос - как с тобой общаться?
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    synchronized static void disconnect() {
        try {
            connection.close(); // Закрыли соединение с JDBC.
        } catch (SQLException throwables) {
            throw new RuntimeException(throwables);
        }
    }

    synchronized static String getNick(String login, String password) { // Метод извлечения nickname.
        String query = String.format(
                "select nickname from users where login='%s' and password='%s'", login, password); // запрос на
                                                                            // извлечение nickname по login, password.
        try (ResultSet set = statement.executeQuery(query)) { // Ожидаем, что set будет nickname
            if (set.next()) // Если в set есть что читать, не пуст, то
                return set.getString("nickname"); // сОхранили в set nickname.
        } catch (SQLException e) {
            throw  new RuntimeException(e);
        }
        return null; // Если ничего не нашли по запросу, то возвращаем null.
    }
}
