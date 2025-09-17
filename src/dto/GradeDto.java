package dto;

import java.time.LocalDateTime;

public record GradeDto(Long id,
                       Long student_id,
                       Long course_id,
                       String grade,
                       LocalDateTime date) {
}
