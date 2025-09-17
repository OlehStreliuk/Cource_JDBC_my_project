package entity;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Enrollment {

    private  Long id;
    private  Long student_id;
    private  Long course_id;
    private LocalDateTime date;

    public Enrollment() {
    }

    public Enrollment(Long id, Long student_id, Long course_id, LocalDateTime date) {
        this.id = id;
        this.student_id = student_id;
        this.course_id = course_id;
        this.date = date;
    }

    public Long getId() {
        return id;
    }

    public Long getStudent_id() {
        return student_id;
    }

    public Long getCourse_id() {
        return course_id;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setStudent_id(Long student_id) {
        this.student_id = student_id;
    }

    public void setCourse_id(Long course_id) {
        this.course_id = course_id;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }
}
