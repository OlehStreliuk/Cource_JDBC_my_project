package dao;

import dto.EnrollmentDto;
import entity.Enrollment;
import exception.DaoException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EnrollmentDaoImpl extends ConnectDao implements DaoEnrollment {

    @Override
    public Long createByStudentCource(Long studentId, Long courseId) {
        String sql = """
                 INSERT INTO enrollments (student_id, course_id, enrollment_date)
                                         VALUES (?, ?, NOW())
                                         ON CONFLICT (student_id, course_id) DO NOTHING
                                         RETURNING id;
                """;
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, studentId);
            statement.setLong(2, courseId);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getLong("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Ошибка при создании записи в таблице enrollments", e);
        }

        return 0L; // Если запись уже существует, возвращаем null

    }

    private static final EnrollmentDaoImpl INSTANCE = new EnrollmentDaoImpl();

    public EnrollmentDaoImpl() {
    }

    public static EnrollmentDaoImpl getInstance() {
        return INSTANCE;
    }

    @Override
    public Enrollment save(Enrollment enrollment) {

        if (enrollment.getId() == null){
            Long id = add(enrollment);
            enrollment.setId(id);
        } else{
            update(enrollment);
        }
        return enrollment;
    }

    @Override
    public Long add(Enrollment enrollment) {
        String sql = "INSERT INTO enrollments (student_id, course_id, enrollment_date) VALUES (?, ?, ?) RETURNING id";
        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {

            // Установка параметров запроса
            statement.setLong(1, enrollment.getStudent_id());
            statement.setLong(2, enrollment.getCourse_id());

            // Преобразование LocalDateTime в Timestamp
            if (enrollment.getDate() != null) {
                statement.setTimestamp(3, Timestamp.valueOf(enrollment.getDate()));
            } else {
                statement.setNull(3, Types.TIMESTAMP); // Если дата отсутствует
            }

            // Выполнение запроса и получение результата
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getLong("id"); // Возвращаем ID новой записи
                } else {
                    throw new RuntimeException("Не удалось получить ID новой записи");
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Ошибка при добавлении записи с ID: " + enrollment.getId(), e);
        }
    }

    @Override
    public Optional<Enrollment> findById(Long id) {
        String sql = "SELECT * FROM enrollments WHERE id = ?";
        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Enrollment enrollment = createEnrollment(resultSet);
                    return Optional.of(enrollment);
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Ошибка подключения: ", e);
        }
        return Optional.empty();
    }

    private Enrollment createEnrollment(ResultSet resultSet) throws SQLException {
        Enrollment enrollment = new Enrollment();
        enrollment.setId(resultSet.getLong("id"));
        enrollment.setStudent_id(resultSet.getLong("student_id"));
        enrollment.setCourse_id(resultSet.getLong("course_id"));

        // Преобразуем Timestamp в LocalDateTime, если дата не null
        Timestamp timestamp = resultSet.getTimestamp("enrollment_date");
        if (timestamp != null) {
            enrollment.setDate(timestamp.toLocalDateTime());
        } else {
            enrollment.setDate(null);
        }
        return enrollment;
    }

    @Override
    public List<Enrollment> findAll() {
        List<Enrollment> enrollments = new ArrayList<>();
        String sql = "SELECT * FROM enrollments";
        try (Statement statement = getConnection().createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                Enrollment enrollment = createEnrollment(resultSet);
                enrollments.add(enrollment);
            }
        } catch (SQLException e) {
            throw new DaoException("Не найдены ", e);
        }
        return enrollments;
    }

    @Override
    public void update(Enrollment enrollment) {
        String sql = "UPDATE enrollments SET student_id = ?, course_id = ?, enrollment_date = ? WHERE id = ?";
        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setLong(1, enrollment.getStudent_id());
            statement.setLong(2, enrollment.getCourse_id());
            if (enrollment.getDate() != null) {
                statement.setTimestamp(3, Timestamp.valueOf(enrollment.getDate())); // Преобразование LocalDateTime в Timestamp
            } else {
                statement.setNull(3, Types.TIMESTAMP); // Устанавливаем NULL, если дата отсутствует
            }
            statement.setLong(4, enrollment.getId());
            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated == 0) {
                throw new RuntimeException("Update failed: No enrollment found with id " + enrollment.getId());
            }
        } catch (SQLException e) {
            throw new DaoException("Error updating enrollment with id " + enrollment.getId(), e);
        }
    }

    @Override
    public List<Enrollment> findByStudentId(Long studentId) {
        List<Enrollment> enrollments = new ArrayList<>();
        String sql = """
            SELECT id,
                   student_id,
                   course_id,
                   enrollment_date
            FROM enrollments
            WHERE student_id = ?
            """;

        try (var statement = getConnection().prepareStatement(sql)) {
            statement.setLong(1, studentId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    enrollments.add(createEnrollment(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Error finding enrollments for student_id " + studentId, e);
        }
        return enrollments;
    }

    @Override
    public List<Enrollment> findByCourseId(Long courseId) {

        List<Enrollment> enrollments = new ArrayList<>();
        String sql = """
                SELECT id,
                   enrollments.student_id,
                   enrollments.course_id,
                   enrollments.enrollment_date
                From enrollments
                WHERE  course_id=?
                """;
        try(var statement = getConnection().prepareStatement(sql)){
            statement.setLong(1, courseId);
            try(ResultSet resultSet = statement.executeQuery()){
                while(resultSet.next()){
                    enrollments.add(createEnrollment(resultSet));
                }
            }
        }catch(SQLException e) {
            throw new DaoException("Ошибка при обновлении записи", e);
        }
        return enrollments;
    }

    @Override
    public boolean delete(Long id) {
        String sql = """
                DELETE  FROM enrollments WHERE id=?""";
        try(var statement = getConnection().prepareStatement(sql)) {
            statement.setLong(1, id);
            return statement.executeUpdate()>0;
        } catch (SQLException e){
            throw new DaoException("Error deleting enrollment with id", e);
        }

    }

}
