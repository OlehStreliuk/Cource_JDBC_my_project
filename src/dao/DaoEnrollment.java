package dao;

import entity.Enrollment;

import java.util.List;

public interface DaoEnrollment extends Dao<Long, Enrollment>{
    List<Enrollment> findByStudentId(Long studentId); // Найти записи по студенту
    List<Enrollment> findByCourseId(Long courseId);// Найти записи по курсу
    Long createByStudentCource(Long studentId, Long courceId);
}

