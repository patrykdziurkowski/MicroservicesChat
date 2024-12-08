package com.patrykdziurkowski.microserviceschat.presentation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

@EntityScan(basePackages = { "com.patrykdziurkowski.microserviceschat" })
@ComponentScan(basePackages = { "com.patrykdziurkowski.microserviceschat" })
@SpringBootApplication
public class ChatApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChatApplication.class, args);
	}

}
