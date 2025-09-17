package dao;

import exception.DaoException;
import util.ConnectionManager;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class ConnectDao {

    protected Connection connection;

    public ConnectDao() {
        // Получаем подключение из ConnectionManager
        this.connection = ConnectionManager.open();
    }

    protected Connection getConnection() {
        return connection;
    }

    public void close() {
        // Закрываем соединение, если оно открыто
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new DaoException("Failed to close connection", e);
            }
        }
    }
}
