package com.fran.lordsith;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;

@SpringBootApplication
@EnableEncryptableProperties
@Configuration
public class App {
	
	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}
}
