package com.fran.lordsith;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableEncryptableProperties
@EnableScheduling
@ConfigurationPropertiesScan("com.fran.lordsith.properties")
public class App {
	
	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}
}
