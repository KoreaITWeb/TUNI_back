package com.project.tuni_back;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude={DataSourceAutoConfiguration.class})
public class TuniBackApplication {

	public static void main(String[] args) {
		SpringApplication.run(TuniBackApplication.class, args);
	}

}
