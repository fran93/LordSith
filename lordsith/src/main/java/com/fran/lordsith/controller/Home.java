package com.fran.lordsith.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fran.lordsith.services.LoginService;
import com.fran.lordsith.services.TechnologyService;

@RestController
public class Home {
	
	@Autowired @Lazy
	private LoginService loginService;
	
	@Autowired @Lazy
	private TechnologyService technologyService;

	@GetMapping("/")
	public ResponseEntity<String> main() {
		loginService.login();
		technologyService.buildSomething();
		return ResponseEntity.ok("Ok");
	}
	
	@GetMapping("/build")
	public ResponseEntity<String> build (){
		technologyService.buildSomething();
		return ResponseEntity.ok("Ok");
	}
	
	@GetMapping("shutdown")
	public void shutdown() {
		loginService.logout();
		System.exit(0);
	}
}
