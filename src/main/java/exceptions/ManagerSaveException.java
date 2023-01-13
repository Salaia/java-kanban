package exceptions;

/*
Эту часть ТЗ не поняла вообще:

Исключения вида IOException нужно отлавливать внутри метода save и
кидать собственное непроверяемое исключение ManagerSaveException.
Благодаря этому можно не менять сигнатуру методов интерфейса менеджера.
 */

public class ManagerSaveException extends Exception{
    public ManagerSaveException(String message) {
        super(message);
    }

    public ManagerSaveException(){
        super();
    }
}
