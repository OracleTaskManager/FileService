package com.Oracle.FileService.controller;


import com.Oracle.FileService.data.AttachmentRequest;
import com.Oracle.FileService.data.AttachmentResponse;
import com.Oracle.FileService.model.Attachment;
import com.Oracle.FileService.service.AttachmentService;
import com.Oracle.FileService.service.ObjectStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

@RestController
@RequestMapping("/attachments")
public class AttachmentController {

    private final ObjectStorageService StorageService;
    private final AttachmentService attachmentService;

    public AttachmentController(ObjectStorageService StorageService, AttachmentService attachmentService) {
        this.StorageService = StorageService;
        this.attachmentService = attachmentService;
    }

    @PostMapping("/testTrigger")
    public ResponseEntity<?> testTrigger(){
        return ResponseEntity.ok("Triggering test connection to Object Storage Service");
    }

    @GetMapping("/test")
    public String testconnection(){
        return StorageService.testConnection();
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("taskId") Long taskId) {

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long uploaded_by = Long.parseLong(authentication.getName());
            System.out.println("Uploading file for user: " + uploaded_by);

            if (file.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("File is empty");
            }

            String fileName = file.getOriginalFilename();
            InputStream fileInputStream = file.getInputStream();
            long fileSize = file.getSize();

            String uploadedParUrl = StorageService.uploadFile(fileInputStream, fileName, fileSize);

            Attachment savedAttachment = attachmentService.createAttachment(taskId, uploadedParUrl, uploaded_by);
            AttachmentResponse attachmentResponse = new AttachmentResponse(
                    savedAttachment.getAttachment_id(),
                    savedAttachment.getFile_url(),
                    savedAttachment.getTaskId(),
                    savedAttachment.getUploaded_by());

            return ResponseEntity.ok(attachmentResponse);

        } catch(Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error uploading file: " + e.getMessage());
        }
    }
    @GetMapping("/")
    @PreAuthorize("hasRole('Manager')")
    public ResponseEntity<?> getAllAttachments() {
        List<Attachment> attachments = attachmentService.getAllAttachments();
        List<AttachmentResponse> attachmentResponses = attachments.stream()
                .map(attachment -> new AttachmentResponse(
                        attachment.getAttachment_id(),
                        attachment.getFile_url(),
                        attachment.getTaskId(),
                        attachment.getUploaded_by()))
                .toList();
        return ResponseEntity.ok(attachmentService.getAllAttachments());
    }

    @GetMapping("/{task_id}")
    public ResponseEntity<?> getAttachmentsByTaskId(@PathVariable Long task_id){
        List<Attachment> attachments = attachmentService.getAttachmentsByTaskId(task_id);
        List<AttachmentResponse> attachmentResponses = attachments.stream()
                .map(attachment -> new AttachmentResponse(
                        attachment.getAttachment_id(),
                        attachment.getFile_url(),
                        attachment.getTaskId(),
                        attachment.getUploaded_by()))
                .toList();
        return ResponseEntity.ok(attachmentResponses);
    }

    @DeleteMapping("/{attachment_id}")
    public ResponseEntity<?> deleteAttachment(@PathVariable Long attachment_id){
        try{
            System.out.println("Deleting attachment with ID: " + attachment_id);
            attachmentService.removeAttachmentById(attachment_id);
            return ResponseEntity.ok("Attachment deleted successfully");
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting attachment: " + e.getMessage());
        }
    }

    @DeleteMapping("/task/{task_id}")
    public ResponseEntity<?> deleteByTaskId(@PathVariable Long task_id){
        try{
            System.out.println("Deleting attachments for task ID: " + task_id);
            attachmentService.removeAllAttachmentsFromTask(task_id);
            return ResponseEntity.ok("Attachments deleted successfully");
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting attachments: " + e.getMessage());
        }
    }

}
