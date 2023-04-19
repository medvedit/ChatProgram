package ru.medwedSa.javaTwe.chat.client;
/**
 * Логины в базе данных:
 * medved
 * hhdhdhdds
 * asdasdf
 * fsddfhrq
 * yghtyhrb
 * Пароль 123
 */

import ru.medwedSa.javaTwe.chat.common.Messages;
import ru.medwedSa.javaTwe.network.SocketThread;
import ru.medwedSa.javaTwe.network.SocketThreadListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/* В этом классе комментарии добавлены только к тем методам и строчкам кода,
   которые отличны от уже написанных в файлах, классах этого пакета. */
public class Client extends JFrame implements ActionListener, Thread.UncaughtExceptionHandler, SocketThreadListener {
    private static final int WIDTH = 600;
    private static final int HEIGHT = 300;
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");
    private static final DateFormat DATE_FORMAT_LOG = new SimpleDateFormat("EEE, MMM d, yyyy '@' HH:mm:ss");
    public static final String TITLE = "Чат клиент";

    private final JTextArea log = new JTextArea(); // Создали область панельки в который будет добавляться весь текст из переписки.

    private final JPanel panelTop = new JPanel(new GridLayout(2,3)); // верхняя панелька для информации, и для полезной, в том числе.
    private final JTextField ftIPAddress = new JTextField("127.0.0.1"); // с каким IP адресом соединяться.
    private final JTextField tfPort = new JTextField("8189"); // с каким портом соединяться.
    private final JCheckBox cbAlwaysOnTop = new JCheckBox("На передний план"); // галочка, которая будет говорить находится ли наш Client по вверх основных окон или не находится.
    private final JTextField tfLogin = new JTextField("medved"); // текст с логином.
    private final JPasswordField tfPassword = new JPasswordField("123"); // текст с паролем.
    // PasswordField это тот же TextField, но маскирующий набранный текст в звездочки(*). PasswordField не дает забрать
    // из себя .value , как например может делать TextField. Дело в том, что в PasswordField лежит массив из байтиков и
    // если необходимо забрать информацию из PasswordField - нужно немножко помудрить.
    private final JButton btnLogin = new JButton("Login"); // кнопка логин.

    private final JPanel panelBottom = new JPanel(new BorderLayout()); // верхняя панелька для информации, и для полезной, в том числе.
    private final JButton btnDisconnect = new JButton("<html><b>Disconnect</b><html>"); // кнопка разъединения
    private final JTextField tfMessage = new JTextField(); // окно для набора сообщений в переписке
    private final JButton btnSend = new JButton("<html><b>Send</b><html>"); // кнопка Send (отправить)
    private final JList<String> userList = new JList<>(); // Создали область панельки в который будет добавляться список пользователей чата.

    private boolean shownIoErrors = false;
    private SocketThread socketThread;


    private Client() { // конструктор клиента.
        Thread.setDefaultUncaughtExceptionHandler(this);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // посередине экрана.
        setSize(WIDTH, HEIGHT);
        setTitle(TITLE); // Заголовок, имя окна чата.
        log.setEditable(false); // закрыли возможность писать в панельке log
        log.setLineWrap(true); // перенос отправленного текста сообщения, в окне log, согласно размеру самого окна.
        JScrollPane spLog = new JScrollPane(log); // для области панельки log добавлена возможность скролиться.
        JScrollPane spUser = new JScrollPane(userList); // для области панельки userList добавлена возможность скролиться.
        spUser.setPreferredSize(new Dimension(100,0)); // предпочтительный размер панельки spUser
        cbAlwaysOnTop.addActionListener(this); // активировали "галочку" - по вверх всех окон.
        btnSend.addActionListener(this); // слушатель кнопка SEND
        tfMessage.addActionListener(this); // слушатель кнопка ENTER
        btnLogin.addActionListener(this); // слушатель кнопка Login
        btnDisconnect.addActionListener(this);

        panelBottom.setVisible(false);

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

    //<editor-fold desc="Переопределенный метод для реализации интерфейса ActionListener">
    @Override
    public void actionPerformed(ActionEvent e) { // Переопределенный метод для реализации интерфейса ActionListener
        Object src = e.getSource(); // создали объект, переменную src в которую складываем произошедшее событие(нажатие на кнопку)
        if (src == cbAlwaysOnTop) { // если поставили "галочку", то
            setAlwaysOnTop(cbAlwaysOnTop.isSelected()); // галочка стоит-активна функция "по верх..", если снята, то деактивация функции
        } else if (src == btnSend || src == tfMessage) {
           sendMessage();
        } else if (src == btnLogin) {
            connect();
        } else if (src == btnDisconnect) {
            socketThread.close();
        } else { // иначе исключение
            throw new RuntimeException("Добавить, активировать реализацию нового компонента. " +
                    "Возможно отсутствует действие по событию, нажатию на кнопку.");
        }
    }
    //</editor-fold>

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
        tfMessage.setText(null); // очистили поле tfMessage
        tfMessage.grabFocus(); // вернули "фокусировку" в поле tfMessage
//        wrtMsgToLogFile(msg, userName); // Запуск логирования в файл .txt
        socketThread.sendMessage(Messages.getTypeBCastFromClient(msg));
    }

    private void wrtMsgToLogFile(String msg, String userName) { // метод записи сообщений в файл .txt
        try (FileWriter out = new FileWriter("/Users/Medwed_SA/Desktop/Education/Java/project_Itellij_IDEA/" +
                "ChatProgram/chat-common/src/ru/medwedSa/javaTwe/chat/common/Log.txt", true)) {
            out.write(DATE_FORMAT_LOG.format(new Date()) + ":  " + userName + ": " + msg + "\n");
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
                log.setCaretPosition(log.getDocument().getLength()); // при логирование устанавливается курсор текста в конц документа
            }
        });
    }

    private void showException(Thread t, Throwable e) { // написали отдельный метод для вывода исключений, для использования его в дальнейших блоках кода.
//        String msg;
//        StackTraceElement[] ste = e.getStackTrace();
//        if (ste.length == 0)
//            msg = "Пустая трассировка стека";
//        else {
//            msg = "Исключение в потоке " + t.getName() + "\n" +
//                    " " + e.getClass().getCanonicalName() + ": " + e.getMessage() + "\n" + e.getStackTrace()[0];
//            JOptionPane.showMessageDialog(null, msg,
//                    "Ошибка",JOptionPane.ERROR_MESSAGE);
//        }
//        JOptionPane.showMessageDialog(null, msg,
//                "Ошибка",JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }

    //<editor-fold desc="Переопределение от UncaughtExceptionHandler">
    @Override
    public void uncaughtException(Thread t, Throwable e) { // Обработка исключений. Переопределение от UncaughtExceptionHandler
        e.printStackTrace();
//        showException(t, e); // вывод окна исключений
    }
    //</editor-fold>

    //<editor-fold desc="Переопределенные метода от интерфейса SocketThreadListener">
    @Override
    public void onSocketStart(SocketThread t, Socket s) {
        putLog("Соединение установлено...");
    }

    @Override
    public void onSocketStop(SocketThread t) {
        panelBottom.setVisible(false);
        panelTop.setVisible(true);
        setTitle(TITLE);
        userList.setListData(new String[0]);
    }

    @Override
    public void onSocketReady(SocketThread t, Socket socket) {
        panelBottom.setVisible(true);
        panelTop.setVisible(false);
        String login = tfLogin.getText();
        String pass = new String(tfPassword.getPassword());
        t.sendMessage(Messages.getAuthRequest(login, pass));
    }

    @Override
    public void onReceiveString(SocketThread t, Socket s, String msg) {
        handleMessage(msg);
    }


    @Override
    public void onSocketException(SocketThread t, Throwable e) {
        showException(t, e);
    }
    //</editor-fold>

    void handleMessage(String value) { //Метод форматирования сообщений.
        String[] arr = value.split(Messages.DELIMITER);
        String msgType = arr[0];
        switch (msgType) {
            case Messages.AUTH_ACCEPT:
                setTitle("К " + TITLE + " подключился " + arr[1]);
                break;
            case Messages.AUTH_DENY:
                putLog(value);
                break;
            case Messages.MSG_FORMAT_ERROR:
                putLog(value);
                socketThread.close();
                break;
            case Messages.USER_LIST:
                String users = value.substring(Messages.DELIMITER.length() + Messages.USER_LIST.length());
                String[] usersArr = users.split(Messages.DELIMITER);
                Arrays.sort(usersArr);
                userList.setListData(usersArr);
                break;
            case Messages.SERVER_BROADCAST:
                log.append(DATE_FORMAT.format(Long.parseLong(arr[1])) + " :" + ": "+ arr[2] + ": " + arr[3] + "\n");
                log.setCaretPosition(log.getDocument().getLength());
                break;
            default:
                throw new RuntimeException("Не известный тип сообщения: " + msgType);
        }
    }






    // У класса клиент своя точка входа, свой main, т.к. клиент ничего не знает о классе ServerGUI
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Client();
            }
        });
    }
}
