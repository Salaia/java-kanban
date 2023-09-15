# ТРЕКЕР ЗАДАЧ

Данная программа выполняет функцию системы контроля над выполнением поставленных задач.

Этот код является бэкэндом для приложения трекера.

### Функционал программы:

В программе реализовано хранение трех типов задач:

* Task - одиночная задача, не связанная с другими задачами.
* Epic - комплексная задача, включающая в себя подзадачи.
* Subtask - подзадача, включенная в Epic. Одиночная задача, связанная с другими в комплекс.

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
    * Дополнительно (для задач определенного типа) : Получение списка всех подзадач определённого эпика.
* Удаление по идентификатору.
* Удаление всех задач.

Принцип управления статусами задач:

* При создании задач устанавливается статус NEW.
* При обновлении задач (Task) или подзадач (Subtask) можно установить статус IN_PROGRESS или DONE.
* Статус комплексной задачи (Epic) не устанавливается вручную, а рассчитывается по статусу вложенных подзадач:
  * NEW - если нет вложенных подзадач или все подзадачи этого "эпика" имеют статус NEW.
  * DONE - если все подзадачи этого "эпика" имеют статус DONE.
  * IN_PROGRESS - в остальных случаях.
    
Менеджер сам не выбирает статус для задачи.
Информация о нём приходит менеджеру вместе с информацией о самой задаче.
По этим данным в одних случаях он будет сохранять статус, в других будет рассчитывать.
Для эпиков:

если у эпика нет подзадач или все они имеют статус NEW, то статус должен быть NEW.
если все подзадачи имеют статус DONE, то и эпик считается завершённым — со статусом DONE.
во всех остальных случаях статус должен быть IN_PROGRESS.
Сохранять состояние менеджера задачи в файл

Умеет вычислять продолжительность задач

Выводит задачи в порядке приоритета
