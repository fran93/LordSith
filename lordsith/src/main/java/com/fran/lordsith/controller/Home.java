package com.fran.lordsith.controller;

import java.util.Date;

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

	@GetMapping("/")
	public ResponseEntity<String> main() {
		commanderService.command();
		return ResponseEntity.ok("Ok " + new Date());
	}

	@GetMapping("shutdown")
	public void shutdown() {
		loginService.logout();
		System.exit(0);
	}
}
