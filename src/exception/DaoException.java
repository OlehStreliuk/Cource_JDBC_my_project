package exception;

public class DaoException extends RuntimeException {
    public DaoException(String message, Throwable throwable) {
        super(throwable);
    }

    public DaoException(String message) {
        super(message);
    }
    public DaoException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Конструктор по умолчанию (без сообщения или причины).
     */
    public DaoException() {
        super();
    }

}