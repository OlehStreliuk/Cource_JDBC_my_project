package dao;

import entity.Course;
import java.util.List;

public interface DaoCourse extends Dao<Long, Course> {
    List<Course> findByTeacherId(Long teacherId); // Найти курсы по ID учителя
    List<Course> findByTitle(String title); // Найти курсы по названию
}
