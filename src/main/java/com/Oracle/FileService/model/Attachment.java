package com.Oracle.FileService.model;

import jakarta.persistence.*;
import org.hibernate.annotations.GenerationTime;

@Entity
@Table(name = "attachments")
public class Attachment {
    @Id
    @Column(
            name = "attachment_id",
            columnDefinition = "NUMBER",
            insertable = false,
            updatable = false
    )
    @org.hibernate.annotations.Generated(GenerationTime.INSERT)
    private Long attachment_id;

    @Column(name = "task_id")
    private Long taskId;
    private String file_url;
    private Long uploaded_by;

    public Attachment() {}

    public Attachment(Long task_id, String file_url, Long uploaded_by) {
        this.taskId = task_id;
        this.file_url = file_url;
        this.uploaded_by = uploaded_by;
    }

    public Long getAttachment_id() {
        return attachment_id;
    }

    public void setAttachment_id(Long attachment_id) {
        this.attachment_id = attachment_id;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getFile_url() {
        return file_url;
    }

    public void setFile_url(String file_url) {
        this.file_url = file_url;
    }

    public Long getUploaded_by() {
        return uploaded_by;
    }

    public void setUploaded_by(Long uploaded_by) {
        this.uploaded_by = uploaded_by;
    }
}
