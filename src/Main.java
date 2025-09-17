import dao.CourseDaoImpl;
import dao.GradeDaoImpl;
import dao.StudentDaoImpl;
import dao.TeacherDaoImpl;
import dto.CourseDto;
import dto.GradeDto;
import dto.StudentDto;
import dto.TeacherDto;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public class Main {
    public static void main(String[] args) throws SQLException {
        try {
            // Инициализация DAO
            TeacherDaoImpl teacherDao   = TeacherDaoImpl.getINSTANCE();
            StudentDaoImpl studentDao   = StudentDaoImpl.getInstance();
            CourseDaoImpl courseDao     = CourseDaoImpl.getInstance();
            GradeDaoImpl gradeDao       = GradeDaoImpl.getInstance();

            // 1. Добавляем преподавателя
            TeacherDto teacher = new TeacherDto(null, "John", "Doe", "john.doe@example.com");
            Long teacherId = teacherDao.add(teacher);
            System.out.println("Преподаватель добавлен с ID: " + teacherId);

            // 2. Добавляем студента
            StudentDto student = new StudentDto(null, "Jane", "Smith", "jane.smith@example.com");
            Long studentId = studentDao.add(student);
            System.out.println("Студент добавлен с ID: " + studentId);

            // 3. Создаём курс
            CourseDto course = new CourseDto(null, "Java Programming", "title", teacherId);
            Long courseId = courseDao.add(course);
            System.out.println("Курс добавлен с ID: " + courseId);

            // 4. Записываем студента на курс
            courseDao.enrollStudent(courseId, studentId);
            System.out.println("Студент записан на курс");

            // 5. Добавляем оценку студенту за курс
            GradeDto grade = new GradeDto(null, studentId, courseId, "A", LocalDateTime.now());
            Long gradeId = gradeDao.add(grade);
            System.out.println("Оценка добавлена с ID: " + gradeId);

            // 6. Получаем список оценок студента
            List<GradeDto> grades = gradeDao.findByStudentId(studentId);
            System.out.println("Оценки студента:");
            for (GradeDto g : grades) {
                System.out.println("Курс ID: " + g.course_id() + ", Оценка: " + g.grade());
            }

            // 7. Сохраняем оценки студента в архив (файл)
            saveGradesToArchive(student, grades);

        } catch (SQLException e) {
            System.err.println("Ошибка работы с базой данных: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Произошла ошибка: " + e.getMessage());
        }
    }

    /**
     * Метод для сохранения списка оценок студента в текстовый файл.
     *
     * @param student объект студента
     * @param grades  список оценок студента
     */
    private static void saveGradesToArchive(StudentDto student, List<GradeDto> grades) {
        String fileName = "grades_" + (student.id() != null ? student.id() : "unknown") + ".txt";
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("Оценки для студента: " + student.first_name()+ " " + student.last_name() + "\n");
            writer.write("Email: " + student.email() + "\n\n");

            if (grades.isEmpty()) {
                writer.write("Оценки отсутствуют.\n");
            } else {
                for (GradeDto grade : grades) {
                    writer.write("Курс ID: " + grade.course_id() + ", Оценка: " + grade.grade() + "\n");
                }
            }

            System.out.println("Оценки сохранены в архив: " + fileName);
        } catch (IOException e) {
            System.err.println("Ошибка при сохранении оценок в архив: " + e.getMessage());
        }
    }
}
