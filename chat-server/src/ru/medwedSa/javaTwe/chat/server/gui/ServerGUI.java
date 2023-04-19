package ru.medwedSa.javaTwe.chat.server.gui;

import ru.medwedSa.javaTwe.chat.server.core.ChatServer;
import ru.medwedSa.javaTwe.chat.server.core.ChatSeverListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// GUI (графический пользовательский интерфейс)
public class ServerGUI extends JFrame implements ActionListener, Thread.UncaughtExceptionHandler, ChatSeverListener { // Внешний вид чат_бота.

    private static final int POS_X = 1065; // X
    private static final int POS_Y = 65; // Y
    private static final int WINDOW_WIDTH = 600; // WIDTH
    private static final int WINDOW_HEIGHT = 300; // HEIGHT

    private final ChatServer server = new ChatServer(this);
    private final JButton btnStart = new JButton("Start"); // Кнопка в визуальное окно.
    private final JButton btnStop = new JButton("Stop"); // Кнопка в визуальное окно.
    private final JPanel panelTop = new JPanel(new GridLayout(1,2));
    private final JTextArea log = new JTextArea();

    private ServerGUI() { // Конструктор визуализации окна приложения.
        //(Thread.setDefaultUncaughtExceptionHandler-перевод) установить обработчик по умолчанию для не пойманных исключений.
        Thread.setDefaultUncaughtExceptionHandler(this); // Передаем в функцию себя, что потребует добавления
        // Thread - класс отвечающий за потоки              // интерфейса Thread.UncaughtExceptionHandler в класс ServerGUI
        // (что и сделал), а соответственно добавление интерфейса
        // потребует новое переопределение метода uncaughtException
        // ниже по коду добавлен.
        setDefaultCloseOperation(EXIT_ON_CLOSE); // закрытие по нажатию на красный крестик.
        setBounds(POS_X, POS_Y, WINDOW_WIDTH, WINDOW_HEIGHT); // визуальное окно по созданным размерам в переменных.
        setResizable(false); // Рамка окна не изменяема.
        setTitle("Сервер запуска чата"); // Заголовок, имя созданного окна.
        setAlwaysOnTop(true); // Должно ли созданное окно быть на переднем плане среди всех окон.
        log.setEditable(false);
        log.setLineWrap(true);
        JScrollPane scrollLog = new JScrollPane(log);
        btnStart.addActionListener(this); // Говорим, что кнопка btnStart - это объект ActionListener.
        btnStop.addActionListener(this); // и btnStop так же.
        // Соответственно! Нажатие кнопок btnStart и btnStop приведет исполнение кода в переопределенный метод
        // actionPerformed(ActionEvent e) от интерфейса ActionListener.
        panelTop.add(btnStart);
        panelTop.add(btnStop);
        add(panelTop, BorderLayout.NORTH);
        add(scrollLog, BorderLayout.CENTER);
        setVisible(true); // Запустили визуализацию.
    }

    //<editor-fold desc="Переопределенный метод для реализации интерфейса ActionListener">
    @Override
    public void actionPerformed(ActionEvent e) { // переопределенный метод для реализации интерфейса ActionListener
        Object src = e.getSource(); // создали объект, переменную src в которую складываем произошедшее событие(нажатие на кнопку)
        if (src == btnStart) { // если нажата кнопка btnStart, то...
            server.start(8189);
        } else if (src == btnStop) { // если нажата кнопка btnStop, то...
            server.stop();
        } else { // иначе исключение
            throw new RuntimeException("Добавить, активировать реализацию нового компонента. " +
                    "Возможно отсутствует действие по событию, нажатию на кнопку."); // при дальнейшей
            // доработке кода, добавлении новой кнопки в визуальном окне, человек ее добавивший, может забыть добавить
            // реализацию действия по нажатию этой, новой кнопки. Это исключение на этот случай.
        }
    }
    //</editor-fold>

    //<editor-fold desc="Переопределение от UncaughtExceptionHandler">
    /* В этом переопределенном методе создали окно с сообщением msg для вывода описания ошибки.
    Не стал комментировать все строки кода, проще cmd + ПКМ и почитать документацию о каждой функции и методе.
    или пересмотри лекцию №4. */
    @Override
    public void uncaughtException(Thread t, Throwable e) { // Переопределение от UncaughtExceptionHandler(ГДЕ произошло, ЧТО произошло)
//        e.printStackTrace();
//        String msg = "Исключение в потоке " + t.getName() + "\n" +
//                " " + e.getClass().getCanonicalName() + ": " + e.getMessage() + "\n" + e.getStackTrace()[0]; // сообщение
//        // о самой ошибке.
//
//        JOptionPane.showMessageDialog(null, msg,
//                "Ошибка", JOptionPane.ERROR_MESSAGE); // создание самого окна об ошибке.
    }
    //</editor-fold>

    @Override
    public void onChatServerMessage(String msg) {
        SwingUtilities.invokeLater(( ) -> {
            log.append(msg + "\n");
            log.setCaretPosition(log.getDocument().getLength());
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() { // создаем новый объект интерфейса Runnable()
            @Override // в Runnable() один метод, который переопределяем
            public void run() { // метод запускает
                new ServerGUI(); // новый объект нашего ServerGUI() который будет создан в новом потоке Runnable()
            }
        });
    }
}
