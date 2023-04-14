package ru.medwedSa.javaTwe.chat.client;

import ru.medwedSa.javaTwe.network.SocketThread;
import ru.medwedSa.javaTwe.network.SocketThreadListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;

/* В этом классе комментарии добавлены только к тем методам и строчкам кода,
   которые отличны от уже написанных в файлах, классах этого пакета. */
public class Client extends JFrame implements ActionListener, Thread.UncaughtExceptionHandler, SocketThreadListener {
    private static final int WIDTH = 400;
    private static final int HEIGHT = 300;

    private final JTextArea log = new JTextArea(); // Создали область панельки в который будет добавляться весь текст из переписки.

    private final JPanel panelTop = new JPanel(new GridLayout(2,3)); // верхняя панелька для информации, и для полезной, в том числе.
    private final JTextField ftIPAddress = new JTextField("127.0.0.1"); // с каким IP адресом соединяться.
    private final JTextField tfPort = new JTextField("8189"); // с каким портом соединяться.
    private final JCheckBox cbAlwaysOnTop = new JCheckBox("Поверх всех окон"); // галочка, которая будет говорить находится ли наш Client по вверх основных окон или не находится.
    private final JTextField tfLogin = new JTextField("Витя Пупкин"); // текст с логином.
    private final JPasswordField tfPassword = new JPasswordField("123456"); // текст с паролем.
    // PasswordField это тот же TextField, но маскирующий набранный текст в звездочки(*). PasswordField не дает забрать
    // из себя .value , как например может делать TextField. Дело в том, что в PasswordField лежит массив из байтиков и
    // если необходимо забрать информацию из PasswordField - нужно немножко помудрить.
    private final JButton btnLogin = new JButton("Login"); // кнопка логин.

    private final JPanel panelBottom = new JPanel(new BorderLayout()); // верхняя панелька для информации, и для полезной, в том числе.
    private final JButton btnDisconnect = new JButton("Disconnect"); // кнопка разъединения
    private final JTextField tfMessage = new JTextField(); // окно для набора сообщений в переписке
    private final JButton btnSend = new JButton("Send"); // кнопка Send (отправить)
    private final JList<String> userList = new JList<>(); // Создали область панельки в который будет добавляться список пользователей чата.

    private boolean shownIoErrors = false;
    private SocketThread socketThread;


    private Client() { // конструктор клиента.
        Thread.setDefaultUncaughtExceptionHandler(this);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // посередине экрана.
        setSize(WIDTH, HEIGHT);
        setTitle("Чат_Клиент");
        log.setEditable(false); // закрыли возможность писать в панельке log
        log.setLineWrap(true); // перенос отправленного текста сообщения, в окне log, согласно размеру самого окна.
        JScrollPane spLog = new JScrollPane(log); // для области панельки log добавлена возможность скролиться.
        JScrollPane spUser = new JScrollPane(userList); // для области панельки userList добавлена возможность скролиться.
        String[] users = {"user 1", "user 2", "user 3", "user 4", "user 5", "user 6", // тест визуализация пользователей
                "user 7", "user 8", "user 9", "user 10", "user 11", "user 12", "user 13", // больше необходимая для настройки
                "user 14", "user 15_и_у_этого_человека_очень_длинный_ник"}; // размера и проверки скролла окна users
        userList.setListData(users); // добавили тестовый массив пользователей в панельку spUser
        spUser.setPreferredSize(new Dimension(100,0)); // предпочтительный размер панельки spUser
        cbAlwaysOnTop.addActionListener(this); // активировали "галочку" - по вверх всех окон.
        btnSend.addActionListener(this); // слушатель кнопка SEND
        tfMessage.addActionListener(this); // слушатель кнопка ENTER
        btnLogin.addActionListener(this); // слушатель кнопка Login


        panelTop.add(ftIPAddress); // добавили все сообщения, кнопки на верхнюю панель.
        panelTop.add(tfPort); // добавили все сообщения, кнопки на верхнюю панель.
        panelTop.add(cbAlwaysOnTop); // добавили все сообщения, кнопки на верхнюю панель.
        panelTop.add(tfLogin); // добавили все сообщения, кнопки на верхнюю панель.
        panelTop.add(tfPassword); // добавили все сообщения, кнопки на верхнюю панель.
        panelTop.add(btnLogin); // добавили кнопку на верхнюю панель.
        panelBottom.add(btnDisconnect, BorderLayout.WEST); // добавили кнопки на нижнюю панель.
        panelBottom.add(tfMessage, BorderLayout.CENTER); // написание сообщения на нижнюю панель.
        panelBottom.add(btnSend, BorderLayout.EAST); // добавили кнопки на нижнюю панель.

        add(panelTop, BorderLayout.NORTH); // расположение на севере (вверху) панели.
        add(panelBottom, BorderLayout.SOUTH); // расположение на юге (внизу) панели.
        add(spLog, BorderLayout.CENTER); // расположение по центру панели log (через spLog)
        add(spUser, BorderLayout.EAST); // добавили список пользователей

        setVisible(true);
    }
    // У класса клиент своя точка хода, свой main, т.к. клиент ничего не знает о классе ServerGUI
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Client();
            }
        });
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource(); // создали объект, переменную src в которую складываем произошедшее событие(нажатие на кнопку)
        if (src == cbAlwaysOnTop) { // если поставили "галочку", то
            setAlwaysOnTop(cbAlwaysOnTop.isSelected()); // галочка стоит-активна функция "по верх..", если снята, то деактивация функции
        } else if (src == btnSend || src == tfMessage) {
           sendMessage();
        } else if (src == btnLogin) {
            connect();
        } else { // иначе исключение
            throw new RuntimeException("Добавить, активировать реализацию нового компонента. " +
                    "Возможно отсутствует действие по событию, нажатию на кнопку.");
        }
    }

    private void connect() {
        try {
            Socket socket = new Socket(ftIPAddress.getText(), Integer.parseInt(tfPort.getText()));
            socketThread = new SocketThread(this, "Client", socket);
        } catch (IOException e) {
            showException(Thread.currentThread(), e);
        }
    }

    private void sendMessage() { // метод отправки сообщения и записи сообщения в файл .txt
        String msg = tfMessage.getText(); // забрал набранное сообщение
        String userName = tfLogin.getText();// забрал имя пользователя чат_бота
        if ("".equals(msg)) return; // если просто нажата клавиша SEND или ENTER, а сообщение не набрали, то просто дальнейшее ожидание...
//        log.append(text + "\n"); // добавил забранный текст в поле log (я сделал так, а преподаватель перенес это в метод putLog)
        tfMessage.setText(null); // очистили поле tfMessage
        tfMessage.grabFocus(); // вернули "фокусировку" в поле tfMessage
//        tfMessage.requestFocusInWindow(); // вернули "фокусировку" в поле tfMessage (еще способ)
//        putLog(String.format("%s, %s",userName, msg)); // реализация метода putLog
        wrtMsgToLogFile(msg, userName); // реализация метода записи в файл
        socketThread.sendMessage(msg);
    }
    private void wrtMsgToLogFile(String msg, String userName) { // метод записи сообщений в файл .txt
        try (FileWriter out = new FileWriter("/Users/Medwed_SA/Desktop/Education/Java/project_Itellij_IDEA/" +
                "ChatProgram/chat-common/src/ru/medwedSa/javaTwe/chat/common/Log.txt", true)) {
            out.write(userName + ": " + msg + "\n");
            out.flush();
        } catch (IOException e) {
            if (!shownIoErrors) {
                shownIoErrors = true;
                showException(Thread.currentThread(), e); // вывод окна исключений
            }
        }
    }
    private void putLog(String message) { // метод перевода набранного сообщения из поля tfMessage в поле log
        if ("".equals(message)) return;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                log.append(message + "\n");  // добавил забранный текст в поле log
                log.setCaretPosition(log.getDocument().getLength()); // при логировании устанавливается курсор текста в конц документа
            }
        });
    }
    private void showException(Thread t, Throwable e) { // написали отдельный метод для вывода исключений, для использования его в дальнейших блоках кода.
        String msg;
        StackTraceElement[] ste = e.getStackTrace();
        if (ste.length == 0)
            msg = "Пустая трассировка стека";
        else {
            msg = "Исключение в потоке " + t.getName() + "\n" +
                    " " + e.getClass().getCanonicalName() + ": " + e.getMessage() + "\n" + e.getStackTrace()[0 ];
            JOptionPane.showMessageDialog(null, msg,
                    "Ошибка",JOptionPane.ERROR_MESSAGE);
        }
        JOptionPane.showMessageDialog(null, msg,
                "Ошибка",JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) { // обработка исключений
        e.printStackTrace();
        showException(t, e); // вывод окна исключений
    }

    @Override
    public void onSocketStart(SocketThread t, Socket s) {
        putLog("Соединение установлено...");
    }

    @Override
    public void onSocketStop(SocketThread t) {
        putLog("Соединение остановлено.");
    }

    @Override
    public void onSocketReady(SocketThread t, Socket socket) {
        putLog("Обмен потоками ввода вывода с сервером");
    }

    @Override
    public void onReceiveString(SocketThread t, Socket s, String msg) {
        putLog(msg);
    }

    @Override
    public void onSocketException(SocketThread t, Throwable e) {
        showException(t, e);
    }
}