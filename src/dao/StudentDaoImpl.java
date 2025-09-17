package dao;

import dto.StudentDto;
import entity.Student;
import exception.DaoException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StudentDaoImpl extends ConnectDao implements DaoStudent {


    private static final StudentDaoImpl INSTANCE = new StudentDaoImpl();

    public static StudentDaoImpl getInstance() {
            return INSTANCE;
    }

    @Override
    public boolean delete(Long id) {
        String sql = "DELETE FROM students WHERE id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Не удалено " + id, e);
        }
    }


    @Override
    public Student save(Student student) {
        // Проверяем, существует ли студент с таким же именем и фамилией
        Student existingStudent = findByFullName(student);

        if (existingStudent != null) {
            // Если студент уже существует, возвращаем найденного студента
            return existingStudent;
        }

        // Если студент не существует, добавляем его в базу данных
        if (student.getId() == null) {
            Long id = add(student); // Метод add возвращает сгенерированный ID
            student.setId(id);      // Устанавливаем ID для объекта student
        } else {
            update(student);        // Если ID уже есть, обновляем запись
        }
        return student;
    }

    @Override
    public Long add(Student student) {
        String sql = "INSERT INTO students (first_name, last_name, email) VALUES (?, ?, ?) RETURNING id";
        try (PreparedStatement preparedStatement = getConnection().prepareStatement(sql)) {
            preparedStatement.setString(1, student.getFirst_name());
            preparedStatement.setString(2, student.getLast_name());
            preparedStatement.setString(3, student.getEmail());

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getLong("id"); // Извлекаем сгенерированный ID
                } else {
                    throw new RuntimeException("Failed to add student: no ID returned from database.");
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Error while adding a student to the database", e);
        }
    }

    public Long add(StudentDto studentDto) {
        Student student = new Student(null, studentDto.first_name(), studentDto.last_name(), studentDto.email());
        return add(student);
    }



    @Override
    public void update(Student student) {
        String sql = "UPDATE students SET first_name = ?, last_name = ?, email = ? WHERE id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, student.getFirst_name());
            stmt.setString(2, student.getLast_name());
            stmt.setString(3, student.getEmail());
            stmt.setLong(4, student.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Error while updating a student", e);
        }
    }

    @Override
    public Optional<Student> findById(Long id) {
        String sql = "SELECT * FROM students WHERE id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Student student = new Student(
                        rs.getLong("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email")
                );
                return Optional.of(student);
            }
        } catch (SQLException e) {
            throw new DaoException("Error while updating a student", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Student> findAll() {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM students";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                students.add(new Student(
                        rs.getLong("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email")
                ));
            }
        } catch (SQLException e) {
            throw new DaoException("Error while updating a student", e);
        }
        return students;
    }

    @Override
    public List<Student> findByCourseId(Long courseId) {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT s.* FROM students s " +
                "JOIN enrollments e ON s.id = e.student_id " +
                "WHERE e.course_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setLong(1, courseId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                students.add(new Student(
                        rs.getLong("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email")
                ));
            }
        } catch (SQLException e) {
            throw new DaoException("Error while findByCourseId a student", e);
        }
        return students;
    }


    @Override
    public List<Student> findByName(String name) {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM students WHERE students.last_name LIKE ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, "%" + name + "%");
            try (ResultSet rs = stmt.executeQuery()) { // Оборачиваем ResultSet в try-with-resources
                while (rs.next()) {
                    students.add(new Student(
                            rs.getLong("id"),
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getString("email")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Error while finding students by name", e); // Бросаем пользовательское исключение
        }
        return students;
    }


    public Student findByFullName(Student student) {
        String sql = "SELECT id, first_name, last_name FROM students WHERE first_name = ? AND last_name = ?";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, student.getFirst_name());
            stmt.setString(2, student.getLast_name());

            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    // Создаем объект Student из результата запроса
                    return student; // Возвращаем найденного студента
                }
            }
        } catch (SQLException e) {

            throw new DaoException("Ошибка при поиске студента", e);
        }
        return null; // Если студент не найден, возвращаем null
    }

}
