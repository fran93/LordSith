package com.fran.lordsith.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fran.lordsith.services.CommanderService;
import com.fran.lordsith.services.LoginService;

@RestController
public class Home {
	
	@Autowired @Lazy
	private CommanderService commanderService;
	
	@Autowired @Lazy
	private LoginService loginService;
	
	Logger logger = LoggerFactory.getLogger(Home.class);

	@GetMapping("/")
	public ResponseEntity<String> main() {
		logger.info("Procesing petition");
		commanderService.command();
		return ResponseEntity.ok("Ok");
	}

	@GetMapping("shutdown")
	public void shutdown() {
		loginService.logout();
		System.exit(0);
	}
}
