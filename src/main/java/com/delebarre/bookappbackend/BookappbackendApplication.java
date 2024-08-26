package com.delebarre.bookappbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

// @SpringBootApplication
// public class BookappbackendApplication {

// 	public static void main(String[] args) {
// 		SpringApplication.run(BookappbackendApplication.class, args);
// 	}

// 	@Bean
// 	public RestTemplate restTemplate(RestTemplateBuilder builder) {
// 		return builder
// 				.setConnectTimeout(Duration.ofSeconds(10))
// 				.setReadTimeout(Duration.ofSeconds(10))
// 				.build();
// 	}
	
// }

@SpringBootApplication
@ComponentScan(basePackages = "com.delebarre.bookappbackend")
public class BookappbackendApplication {
	public static void main(String[] args) {
		SpringApplication.run(BookappbackendApplication.class, args);
	}
}