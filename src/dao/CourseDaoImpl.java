package dao;

import dto.CourseDto;
import entity.Course;
import entity.Enrollment;
import entity.Student;
import exception.DaoException;
import util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

public class CourseDaoImpl extends ConnectDao implements Dao<Long, Course>, SearchableDao<Course, CourseDto> {


    private static final CourseDaoImpl INSTANCE = new CourseDaoImpl();

    public static  CourseDaoImpl getInstance(){
        return INSTANCE;
    }
    private CourseDaoImpl() {
    }

    @Override
    public boolean delete(Long id) {
        String sql = """
                DELETE FROM courses 
                WHERE id = ?
                """;
        try (var statement = getConnection().prepareStatement(sql)) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0; // Возвращает true, если запись удалена
        } catch (SQLException e) {
            throw new DaoException("Ошибка удаления курса с id=" + id, e);
        }
    }


    @Override
    public Course save(Course course) {
        if (course.getId() == null) {
            Long id = add(course);
            course.setId(id);
        } else {
            update(course);
        }
        return course;
    }

    public Long add(CourseDto courseDto) {

        Course course = new Course(courseDto.id(), courseDto.title(), courseDto.description(), courseDto.teacherId());
        return add(course);
    }

    @Override
    public Long add(Course course) {
        String sql = """
                 INSERT INTO courses (title, description, teacher_id)
                 VALUES (?, ?, ?)
                 RETURNING id
                """;
        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setString(1, course.getTitle());
            statement.setString(2, course.getDescription());
            statement.setLong(3, course.getTeacher_id());

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getLong("id");
            } else {
                throw new RuntimeException("Не удалось получить ID новой записи");
            }
        } catch (SQLException e) {
            throw new DaoException("Ошибка подключения", e);
        }
    }

    @Override
    public void update(Course course) {
        String sql = """
                UPDATE courses 
                SET title = ?, description = ?, teacher_id = ? 
                WHERE id = ?
                """;
        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
                        statement.setString(1, course.getTitle());
                        statement.setString(2, course.getDescription());
                        statement.setLong(3, course.getTeacher_id());
                        statement.setLong(4, course.getId());
                    int rowsUpdated = statement.executeUpdate();
                    if (rowsUpdated == 0) {
                            throw new DaoException("Курс с id=" + course.getId() + " не найден");
                    }
        } catch (SQLException e) {
            throw new DaoException("Ошибка при обновлении курса", e);
        }
    }

    @Override
    public Optional<Course> findById(Long id) {
        String sql = """
                    SELECT id,
                           title,
                           description,
                           teacher_id
                    FROM  courses
                    WHERE id=?
                    """;
        try(PreparedStatement statement = getConnection().prepareStatement(sql)){
            statement.setLong(1, id);
            try(ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Course course = createCourse(resultSet);
                    return Optional.of(course);
                }
            }
        } catch (SQLException e){
            throw new DaoException("Ошибка подключения при поиске по id", e);
        }
        return Optional.empty();
    }

    private static Course createCourse(ResultSet resultSet) throws SQLException {
        Course course = new Course();
        course.setId(resultSet.getLong("id"));
        course.setTitle(resultSet.getString("title"));

        // Проверяем, может ли описание быть null
        String description = resultSet.getString("description");
        course.setDescription(Optional.ofNullable(resultSet.getString("description")).orElse(""));
        // Устанавливаем пустую строку, если значение null

        course.setTeacher_id(resultSet.getLong("teacher_id"));
        return course;
    }

    @Override
    public List<Course> findAll() {
        final List<Course> courses = new ArrayList<>();
        String sql = """
            SELECT id,
                   title,
                   description,
                   teacher_id
            FROM courses
            """;

        try (PreparedStatement statement = getConnection().prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Course course = createCourse(resultSet); // Создаём объект Course из текущей строки результата
                courses.add(course); // Добавляем курс в список
            }

        } catch (SQLException e) {
            throw new DaoException("Ошибка при выполнении SQL-запроса: " + sql, e);
        }

        if (courses.isEmpty()) {
            System.out.println("В базе данных отсутствуют курсы.");
        }

        return courses; // Возвращаем список курсов
    }

    public void enrollStudent(Long courseId, Long studentId) {
        EnrollmentDaoImpl enrollmentDao = EnrollmentDaoImpl.getInstance();

        // Проверяем, существует ли студент и курс
        Optional<Student> student = StudentDaoImpl.getInstance().findById(studentId);
        Optional<Course> course = CourseDaoImpl.getInstance().findById(courseId);

        if (student.isPresent() && course.isPresent()) {
            Enrollment enrollment = new Enrollment();
            enrollment.setCourse_id(courseId);
            enrollment.setStudent_id(studentId);

            enrollmentDao.save(enrollment); // Сохраняем запись о зачислении
            System.out.println("Студент успешно зачислен на курс.");
        } else {
            System.out.println("Ошибка: студент или курс не найдены.");
        }
    }

    @Override
    public List<Course> findByCriteria(CourseDto criteria) {
        List<Course> enrollments = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM courses WHERE 1=1");

        if (criteria.id() != null) {
            sql.append(" AND id = ").append(criteria.id());
        }
        if (criteria.title() != null) {
            sql.append(" AND title = ").append(criteria.id());
        }
        if (criteria.description() != null) {
            sql.append(" AND description = ").append(criteria.id());
        }
        sql.append(" AND description = '").append(criteria.teacherId()).append("'");


    try (Connection connection = getConnection();
         Statement statement = connection.createStatement();
         ResultSet resultSet = statement.executeQuery(sql.toString())) {

        while (resultSet.next()) {
            enrollments.add(createCourse(resultSet));
        }
    } catch (SQLException e) {
        throw new DaoException("Error finding enrollments by criteria", e);
    }

    return enrollments;
    }
}
