package com.outsera.awards.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;

public record MovieUploadRequest(
        @Schema(type = "string", format = "binary") MultipartFile file) {
}
