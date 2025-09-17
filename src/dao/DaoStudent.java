package dao;

import entity.Student;
import java.util.List;

public interface DaoStudent extends Dao<Long, Student> {
    List<Student> findByCourseId(Long courseId); // Найти студентов по ID курса
    List<Student> findByName(String name); // Найти студентов по имени

}

