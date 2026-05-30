package com.deliverXY.backend.NewCode.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "file.upload")
public record FileUploadProperties(long maxSize) {
}
