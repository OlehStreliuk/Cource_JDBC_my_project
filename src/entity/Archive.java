package entity;

import java.time.LocalDateTime;

public class Archive {

    private Long id;
    private Long grad_id;
    private LocalDateTime archiveDate;

    public Archive() {
    }

    public Archive(Long id, Long grad_id, LocalDateTime archiveDate) {
        this.id = id;
        this.grad_id = grad_id;
        this.archiveDate = archiveDate;
    }

    public Long getId() {
        return id;
    }

    public Long getGrad_id() {
        return grad_id;
    }

    public LocalDateTime getArchiveDate() {
        return archiveDate;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setGrad_id(Long grad_id) {
        this.grad_id = grad_id;
    }

    public void setArchiveDate(LocalDateTime archiveDate) {
        this.archiveDate = archiveDate;
    }
}

