package com.Oracle.FileService.data;

public record AttachmentResponse(
        Long attachmentId,
        String fileUrl,
        Long taskId,
        Long uploadedBy
) {
}
