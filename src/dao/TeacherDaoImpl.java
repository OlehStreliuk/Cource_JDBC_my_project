package dao;

import dto.TeacherDto;
import entity.Teacher;
import exception.DaoException;
import util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TeacherDaoImpl extends ConnectDao implements Dao<Long, Teacher>, SearchableDao<Teacher, TeacherDto> {
    @Override
    public List<Teacher> findByCriteria(TeacherDto obj) {
        return List.of();
    }

//    @Override
//    public Long add(Teacher obj) throws SQLException {
//        return 0;
//    }

    private static final TeacherDaoImpl INSTANCE = new TeacherDaoImpl();

    private static final String DELETE_SQL = """ 
        DELETE FROM teachers
        WHERE id=?
        """;
    private static final String SAVE_SQL = """
            INSERT INTO teachers(first_name, last_name, email) VALUES (?,?,?)
            """;
    private static final String UPDATE_SQL = """
            UPDATE teachers
            SET first_name = ?,
                last_name = ?,
                email = ?
            WHERE id = ?
            """;
    private static final String FIND_ALL_SQL = """
            SELECT  teachers.id,
                    teachers.first_name,
                    teachers.last_name,
                    teachers.email
            FROM teachers
            """;
    private static final String FIND_BY_ID_SQL = FIND_ALL_SQL + """
            WHERE teachers.id = ?
            """;
    private static final String ADD_SQL = """
            INSERT INTO teachers (id, first_name, last_name, email) 
            VALUES (?, ?, ?, ?)""";


    public TeacherDaoImpl() {
    }

    public static TeacherDaoImpl getINSTANCE() {
        return INSTANCE;
    }

    @Override
    public boolean delete(Long id) {
        try(var connaction = ConnectionManager.open();
            var preparedStatement = connaction.prepareStatement(DELETE_SQL)){
                preparedStatement.setLong(1, id);
                return preparedStatement.executeUpdate()>0;
        } catch (SQLException e){
            throw new DaoException("Удален teacher " + id, e);
        }
    }

    @Override
    public Teacher save(Teacher teacher) {
        try(var connaction = ConnectionManager.open();
            var preparedStatement = connaction.prepareStatement(SAVE_SQL, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, teacher.getFirst_name());
            preparedStatement.setString(2, teacher.getLast_name());
            preparedStatement.setString(3, teacher.getEmail());

            preparedStatement.executeUpdate();

            var generatedKeys = preparedStatement.getGeneratedKeys();
            if(generatedKeys.next()){
                teacher.setId(generatedKeys.getLong("id"));
            }
            return  teacher;

        } catch (SQLException e){
            throw new DaoException("save", e);
        }
    }

    @Override
    public Long add(Teacher teacher) {
        Teacher student = new Teacher(null, teacher.getFirst_name(), teacher.getLast_name(), teacher.getEmail());
        return add(student);
    }


    public Long add(TeacherDto teacherDto) {

        long id = 0L;

        try (var preparedStatement = getConnection().prepareStatement(ADD_SQL, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, teacherDto.first_name());
            preparedStatement.setString(2, teacherDto.last_name());
            preparedStatement.setString(3, teacherDto.email());

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected == 0) {
                throw new DaoException("Не удалось добавить учителя: " + teacherDto);
            }

            try (var generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    id = generatedKeys.getLong(1);

                } else {
                    throw new DaoException("Не удалось получить ID для учителя: " + teacherDto);
                }
            }

            return id;

        } catch (SQLException e) {
            throw new DaoException("Ошибка при добавлении учителя: " + teacherDto, e);
        }
    }


    @Override
    public void update(Teacher teacher) {
        try (Connection connection = ConnectionManager.open();
        PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_SQL)){

            preparedStatement.setString(1, teacher.getFirst_name());
            preparedStatement.setString(2, teacher.getLast_name());
            preparedStatement.setString(3, teacher.getEmail());

            preparedStatement.executeUpdate();


        } catch(SQLException e){
            throw new DaoException("Ошибка при обновлении записи" + teacher.getId(), e);
        }
    }

    @Override
    public Optional<Teacher> findById(Long id) {
        try(Connection connection = ConnectionManager.open();
            PreparedStatement preparedStatement = connection.prepareStatement(FIND_BY_ID_SQL)){

            preparedStatement.setLong(1, id);

            try(ResultSet resultSet = preparedStatement.executeQuery()){
                if(resultSet.next()){
                    Teacher teacher = createTeacher(resultSet);
                    return  Optional.of(teacher);
                }
            } catch(SQLException e){
                throw new DaoException("ind by id", e);
            }

        } catch(SQLException e){
            throw new DaoException("find by id ", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Teacher> findAll() {
        List<Teacher> teachers = new ArrayList<>();
        try (Connection connection = ConnectionManager.open();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(FIND_ALL_SQL)) {
            while (resultSet.next()) {
                Teacher teacher = createTeacher(resultSet);
                teachers.add(teacher);
            }
        }catch(SQLException e){
        throw new DaoException("find all", e);}
      return teachers;
    }

    private static Teacher createTeacher(ResultSet resultSet) throws SQLException {
        Teacher teacher = new Teacher();
        teacher.setId(resultSet.getLong("id"));
        teacher.setFirst_name(resultSet.getString("first_name"));
        teacher.setLast_name(resultSet.getString("last_name"));
        teacher.setEmail(resultSet.getString("email"));
        return teacher;
    }
}
