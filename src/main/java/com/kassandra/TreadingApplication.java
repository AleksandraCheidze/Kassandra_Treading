package com.kassandra;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.kassandra")
public class TreadingApplication {

	public static void main(String[] args) {
		SpringApplication.run(TreadingApplication.class, args);

	}

}
