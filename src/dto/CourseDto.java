package dto;

public record CourseDto(Long id,
                        String title,
                        String description,
                        Long teacherId) {
    @Override
    public Long id() {
        return id;
    }

    @Override
    public String title() {
        return title;
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public Long teacherId() {
        return teacherId;
    }
}
