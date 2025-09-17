package dao;

import dto.GradeDto;
import entity.Grade;
import exception.DaoException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import java.time.LocalDateTime;

public class GradeDaoImpl extends ConnectDao implements Dao<Long, Grade> {

    private static final GradeDaoImpl INSTANCE = new GradeDaoImpl();

    public static GradeDaoImpl getInstance() {
        return INSTANCE;
    }

    private GradeDaoImpl() {
    }

    @Override
    public Grade save(Grade grade) {

        if (grade.getId() == null) {
            Long id = add(grade);
            grade.setId(id);
        } else {
            update(grade);
        }
        return grade;
    }


    public Long add(GradeDto gradeDto) throws SQLException {
        Grade grade = new Grade(null, gradeDto.student_id(), gradeDto.course_id(), gradeDto.grade(), LocalDateTime.now());
        return add(grade);
    }

    @Override
    public Long add(Grade grade) {
        String sql = """
                INSERT INTO grades(student_id, course_id, grade, date_assigned)
                VALUES (?, ?, ?)
                RETURNING id
                """;
        try (var statement = getConnection().prepareStatement(sql)) {
            statement.setLong(1, grade.getStudent_id());
            statement.setLong(2, grade.getCourse_id());
            if (grade.getDate() != null) {
                statement.setDate(3, Date.valueOf(grade.getDate().toLocalDate()));
            } else {
                statement.setNull(3, Types.DATE);
            }

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getLong("id");
            } else {

                throw new DaoException("Не удалось получить ID новой записи course" + grade.getCourse_id());
            }
        } catch (SQLException e) {
            throw new DaoException("Ошибка при добавлении записи в таблицу grades: " + e.getMessage(), e);

        }
    }

    @Override
    public void update(Grade grade) {
        String sql = """
    
                UPDATE grades
    SET student_id = ?, course_id = ?, grade = ?, date_assigned = ?
    WHERE id = ?
    """;


        try (PreparedStatement preparedStatement = getConnection().prepareStatement(sql)) {
            // Установка параметров запроса
            preparedStatement.setLong(1, grade.getStudent_id());
            preparedStatement.setLong(2, grade.getCourse_id());
            preparedStatement.setString(3, grade.getGrade()); // Предполагается, что grade — число
            preparedStatement.setLong(4, grade.getId());
            if (grade.getDate() != null) {
                preparedStatement.setDate(4, Date.valueOf(grade.getDate().toLocalDate()));
            } else {
                preparedStatement.setNull(4, Types.DATE);
            }
            preparedStatement.setLong(5, grade.getId());


            // Выполнение запроса и проверка количества обновленных строк
            int rowsUpdated = preparedStatement.executeUpdate();

            if (rowsUpdated == 0) {
                // Если ни одна строка не была обновлена
                throw new DaoException("Запись не была обновлена. Возможно, запись с id = " + grade.getId() + " не существует.");
            }
        } catch (SQLException e) {
            // Обработка SQL-исключений
            throw new DaoException("Ошибка при обновлении записи оценки для студента с id = " + grade.getStudent_id(), e);
        }
    }

    @Override
    public Optional<Grade> findById(Long id) {

        String sql = """
                SELECT id, student_id, course_id, grade, date_assigned
                FROM grades
                WHERE id = ?
                """;

        try (var statement = getConnection().prepareStatement( sql)){
            statement.setLong(1, id);

            try(ResultSet resultSet = statement.executeQuery()){
                if(resultSet.next()){
                    Grade grade = createGrade(resultSet);
                    r urn  Optional.of(grade);
                }
            }

        } catch (SQLException e) {
            throw new DaoException(e);
        }
        return Optional.empty();
    }

    private Grade createGrade(ResultSet resultSet) throws SQLException {


            Grade grade = new Grade();
            grade.setId(resultSet.getLong("id"));
            grade.setStudent_id(resultSet.getLong("student_id"));
            grade.setCourse_id(resultSet.getLong("course_id"));
            grade.setGrade(resultSet.getString("grade"));

            Timestamp dateAssigned = resultSet.getTimestamp("date_assigned");
            if (dateAssigned != null) {
                grade.setDate(dateAssigned.toLocalDateTime());
            } else {
                grade.setDate(n
        ull);}

            return grade;
    }


    @Override
    public List<Grade> findAll() {

        List<Grade> grades = new ArrayList<>();

        String sql = """
                SELECT id, student_id, course_id, grade, date_assigned
                FROM grades
                """;
        try(PreparedStatement statement = getConnection().prepareStatement( sql)){
           ResultSet resultSet =  statement.executeQuery();
           while (resultSet.next()){
               Grade grade = createGrade(resultSet);
               grades.add(grade);
           }
        } catch (SQLException e) {
            throw new DaoException(e);
        }
        if (grades.isEmpty()){
            System.out.println("В базе данных отсутствуют оценки.");
        }

        return grades;
    }

    @Override
    public boolean delete(Long id) {

        if (id == null) {
            throw new IllegalArgumentException("Идентификатор для удаления не может быть null");
        }

        String sql = """
                DELETE  FROM grades WHERE id =?
                """;
        try (PreparedStatement statement = getConnection().prepareStatement( sql)){
            statement.setLong(1, id);
            return statement.executeUpdate()>0;

        } catch (SQLException e) {
            throw new DaoException("Ошибка удаления оценки с id=" + id, e);

        }
    }

    public List<GradeDto> findByStudentId(Long studentId) {
        if (studentId == null) {
            throw new IllegalArgumentException("Идентификатор студента не может быть null");
        }

        List<GradeDto> grades = new ArrayList<>();

        String sql =
                """
            SELECT g.id, g.student_id, g.course_id, g.grade, g.date_assigned, c.title AS
                course_n
                   FROM grades g
            JOIN courses c ON g.
                course_id
                       WHERE g.student_id = ?
            """;

        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setLong(1, studentId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    GradeDto gradeDto = createGradeDto(resultSet);

                    grades.add(gradeDto);
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Ошибка при поиске оценок для студента с id=" + studentId, e);
        }

        if (grades.isEmpty()) {
            System.out.println("Для студента с id=" + studentId + " отсутствуют оценки.");
        }

        return grades;
    }

    private GradeDto createGradeDto(ResultSet resultSet) throws SQLException {
        return new GradeDto(
                resultSet.getLong("id"),
                resultSet.getLong("student_id"),
                resultSet.getLong("course_id"),
                resultSet.getString("grade"),
                resultSet.getDate("date_assigned") != null
                        ? resultSet.getDate("date_assigned").toLocalDate().atStartOfDay()
                        : null
        );

    }
}
