package com.Oracle.FileService.data;

public record AttachmentRequest(
        String fileUrl,
        Long taskId
) {
}
