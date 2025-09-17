package entity;

public class Course {
    Long id;
    String title;
    String description;
    Long teacher_id;

    public Course() {
    }

    public Course(Long id, String title, String description, Long teacher_id) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.teacher_id = teacher_id;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Long getTeacher_id() {
        return teacher_id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTeacher_id(Long teacher_id) {
        this.teacher_id = teacher_id;
    }

    @Override
    public String toString() {
        return "Course{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", teacher_id=" + teacher_id +
                '}';
    }
}
