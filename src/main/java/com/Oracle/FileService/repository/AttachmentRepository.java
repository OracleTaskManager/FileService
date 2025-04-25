package com.Oracle.FileService.repository;

import com.Oracle.FileService.model.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    @Query("SELECT a FROM Attachment a WHERE a.task_id = ?1")
    List<Attachment> findAllByTaskId(Long task_id);

}
