### Перенёс проект в отдельный модуль, в котором все классы разбил на дополнительные модули:
+ Разбили работу по модулям. Реализовали по сети на клиентской и серверной стороне 
(пока просто как эхо_бот). Соединение, запуск происходит после коннекта запуска ServerGui -> 
старт, Client -> Login.
> 18e9ed4b medvedit on 14.04.2023 at 16:34
***
### Изменение вывода логов:
+ Добавил возможность для вывода логов, при запуске и дальнейшей активации кнопок Start и Stop,
из консоли в поле окна "Сервер запуска чата". Изменение отрисовки окна "Сервер запуска чата".
> d87c30b0 medvedit on 16.04.2023 at 18:32
***
### Реализация массовой рассылки:
+ Добавил возможность массовой рассылки набранных сообщений из окна чата на всех запущенных
клиентов, от любого запущенного клиента.
> 94dc795c medvedit on 16.04.2023 at 20:30
***
### Реализация БД в проекте:
+ БД в проекте реализована для "примитивного" хранения в ней login, password, nickname. Те в нее
заранее внесены эти данные, что дает возможность подключаться, авторизоваться в чате только тем 
пользователям чьи данные есть в БД. У каждой БД есть свои особенности, свои интерфейс, свои функции,
свой язык взаимодействия, и для того, что бы "подружить" БД и JAVA придумали некую "прослойку" которая называется 
jdbc - драйвер, библиотека для работы с БД. У jdbc есть свои классы, и эти классы умеют общаться с БД. Скачал jdbc 
[тут](https://mvnrepository.com/artifact/org.xerial/sqlite-jdbc/3.36.0.3) и cmd+C -> cmd+V ее в модуль chat-server, тк
именно из него будет вестись работа с БД. Далее добавил зависимость к модулю chat-server от "вставленного" в модуль
скаченного файла jdbc. *Подробное описание JAVA2 урок №7 1час 56мин*. Для создания файла БД использовал SQLiteStudio,
который скачал [тут](https://sqlitestudio.pl). *Видеоряд по созданию файла БД JAVA2 урок №7 2час 01мин* Файл созданный 
в SQLiteStudio создается в корне модуля chat-server. Далее постараюсь максимально добавлять комментарии к самому коду,
функциям....
> 2fe2e16a medvedit on 18.04.2023 at 21:38
***
### Много всего интересного:
+ В ChatServer в методе onServerStop добавил возможность отключать всех клиентов по факту остановки сервера.
+ В Client в методе onReceiveString - просто логировал. Сейчас по факту получения строки нужно отформатировать 
строку, разобрать и уложить ее в лог, а возможно даже реализовать запись в файл журнала сообщений. По-этому создаем
дополнительный метод для разбора handleMessage.
+ Добавлено оповещение подключенным к чату клиентам об отключении, при выходе кого то из клиентов и чата.
+ Работа со списком пользователей. Вывод пользователей в список userList после авторизации и удаление из него
после выхода из чата.
+ Добавлены сообщения протокола в классе Messages.
+ ...
> a759f206 medvedit on 19.04.2023 at 23:13
***