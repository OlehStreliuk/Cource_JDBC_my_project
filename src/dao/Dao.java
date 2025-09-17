package dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface Dao<K, E> {

    boolean delete(K id);

    E save(E obj);

    Long add(E obj) throws SQLException;

    void update(E obj);

    Optional<E> findById(K id);

    List<E> findAll();
}
