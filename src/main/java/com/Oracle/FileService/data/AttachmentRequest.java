package com.Oracle.FileService.data;

public record AttachmentRequest(
        String file_url,
        Long task_id
) {
}
