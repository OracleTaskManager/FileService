package com.Oracle.FileService.controller;


import com.Oracle.FileService.data.AttachmentRequest;
import com.Oracle.FileService.model.Attachment;
import com.Oracle.FileService.service.AttachmentService;
import com.Oracle.FileService.service.ObjectStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Base64;

@RestController
@RequestMapping("/attachments")
public class AttachmentController {

    private final ObjectStorageService StorageService;
    private final AttachmentService attachmentService;

    public AttachmentController(ObjectStorageService StorageService, AttachmentService attachmentService) {
        this.StorageService = StorageService;
        this.attachmentService = attachmentService;
    }

    @GetMapping("/test")
    public String testconnection(){
        return StorageService.testConnection();
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestBody AttachmentRequest attachmentRequest) {
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long uploaded_by = Long.parseLong(authentication.getName());

            String filePath = attachmentRequest.file_url();
            File file = new File(filePath);

            if(!file.exists()){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File does not exist");
            }

            String fileName = file.getName();

            FileInputStream fileInputStream = new FileInputStream(file);

            String uploadedParUrl = StorageService.uploadFile(fileInputStream,fileName, file.length());

            Attachment savedAttachment = attachmentService.createAttachment(attachmentRequest.task_id(), uploadedParUrl, uploaded_by);

            return ResponseEntity.ok(savedAttachment);

        } catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error uploading file: " + e.getMessage());
        }
    }
}
