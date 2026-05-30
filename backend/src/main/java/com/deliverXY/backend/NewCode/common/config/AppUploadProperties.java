package com.deliverXY.backend.NewCode.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.uploads")
public record AppUploadProperties(String dir) {
}
