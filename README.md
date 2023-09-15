# ТРЕКЕР ЗАДАЧ

Данная программа выполняет функцию системы контроля над выполнением поставленных задач.

Этот код является бэкэндом для приложения трекера.

### Функционал программы:

В программе реализовано хранение трех типов задач:

* Task - одиночная задача, не связанная с другими задачами.
* Epic - комплексная задача, включающая в себя подзадачи.
* Subtask - подзадача, включенная в Epic. Одиночная задача, связанная с другими в комплекс.
* Также программа хранит историю обращений к задачам.

<img src="assets/Kanban.png" align="center"></img> 

Все задачи имеют однотипные свойства:

* Название
* Описание
* Уникальный идентификационный номер задачи
* Статус:
    * NEW
    * IN_PROGRESS
    * DONE

Методы для каждого из типа задач(Task/Epic/Subtask):

* Создание.
* Обновление.
* Получение списка всех задач.
* Получение по идентификатору.
* Удаление по идентификатору.
* Удаление всех задач.

Другие функции трекера задач:

* Получить список всех подзадач определённого эпика.
* Вычислить продолжительность задачи.
* Вывести задачи в порядке приоритета.
* Вывести историю обращений к задачам

Принцип управления статусами задач:

* При создании задач устанавливается статус NEW.
* При обновлении задач (Task) или подзадач (Subtask) можно установить статус IN_PROGRESS или DONE.
* Статус комплексной задачи (Epic) не устанавливается вручную, а рассчитывается по статусу вложенных подзадач:
  * NEW - если нет вложенных подзадач или все подзадачи этого "эпика" имеют статус NEW.
  * DONE - если все подзадачи этого "эпика" имеют статус DONE.
  * IN_PROGRESS - в остальных случаях.

### 🛠 Tech & Tools 

* Java 11
* JUnit 5
* Gson
* Альтернативные методы хранения
   * в файле формата *.csv
   * хранение на Key-Value server через связку HttpServer, KV-Server + KV-Client.
* История обращений сохраняется в самодельном LinkedList 

### Инструкция по развёртыванию ▶️

1) Склонируйте репозиторий и перейдите в него
   git@github.com:Salaia/java-kanban.git

2) Запустите проект в выбранной IDE

3) Перейдите по адресу

http://localhost:8080/tasks
4) Можно работать с проектом

### Статус и планы по доработке проекта 

Kanban - финальный проект модуля Java Core курса Java-разработчик от Яндекс.Практикума. На данный момент проект проверен и зачтен ревьюером. Планов по дальнейшему развитию проекта нет.
