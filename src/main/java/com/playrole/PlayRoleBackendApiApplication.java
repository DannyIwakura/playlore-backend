package com.playrole;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class PlayRoleBackendApiApplication {

	public static void main(String[] args) {
		// Genera el hash ANTES de arrancar Spring
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println(encoder.encode("password"));
		SpringApplication.run(PlayRoleBackendApiApplication.class, args);
	}

}
