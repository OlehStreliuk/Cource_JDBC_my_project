package dao;
import java.util.List;

public interface SearchableDao<T, F> {
    List<T> findByCriteria(F obj);
}
