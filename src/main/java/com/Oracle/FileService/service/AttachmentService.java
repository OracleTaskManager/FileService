package com.Oracle.FileService.service;

import com.Oracle.FileService.data.AttachmentRequest;
import com.Oracle.FileService.model.Attachment;
import com.Oracle.FileService.repository.AttachmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AttachmentService {

    @Autowired
    private AttachmentRepository attachmentRepository;

    public Attachment createAttachment(Long task_id, String file_url, Long uploaded_by){
        Attachment attachment = new Attachment(task_id, file_url, uploaded_by);
        return attachmentRepository.save(attachment);
    }

}
