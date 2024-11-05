package com.patrykdziurkowski.microserviceschat.presentation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EntityScan(basePackages = { "com.patrykdziurkowski.microserviceschat" })
@ComponentScan(basePackages = { "com.patrykdziurkowski.microserviceschat" })
public class AuthApplication {
	public static void main(String[] args) {
		SpringApplication.run(AuthApplication.class, args);
	}
}
