package com.playrole.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class UploadConfig implements WebMvcConfigurer {
	
	@Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
		//añadimos los rutas de recursos para poder subir y acceder a ellas
        registry.addResourceHandler("/uploads/**")
        .addResourceLocations("file:" + System.getProperty("user.dir") + "/uploads/");
    }
}
