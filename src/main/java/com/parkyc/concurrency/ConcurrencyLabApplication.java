package com.parkyc.concurrency;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class ConcurrencyLabApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConcurrencyLabApplication.class, args);
	}

}
