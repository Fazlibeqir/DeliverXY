package com.deliverXY.backend;

import com.deliverXY.backend.NewCode.common.config.AppUploadProperties;
import com.deliverXY.backend.NewCode.common.config.AppSecurityProperties;
import com.deliverXY.backend.NewCode.common.config.FileUploadProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({FileUploadProperties.class, AppUploadProperties.class, AppSecurityProperties.class})
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

}
