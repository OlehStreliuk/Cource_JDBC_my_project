package dto;

import java.time.LocalDateTime;

public record EnrollmentDto(Long id,
                            Long student_id,
                            long course_id,
                            LocalDateTime enrollment_date) {
}
