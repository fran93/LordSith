package com.fran.lordsith.services;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class CommanderService {

	@Autowired @Lazy
	private LoginService loginService;
	
	@Autowired @Lazy
	private TechnologyService technologyService;
	
	@Autowired @Lazy
	private FirefoxClient firefox;
	
	public void command() {
		if(!loginService.isLogged()) {
			loginService.login();
		}
		
		List<String> planetIds = new ArrayList<>();
		firefox.get().findElement(By.id("planetList")).findElements(By.className("smallplanet")).forEach(planet -> planetIds.add(planet.getAttribute("id")));
		
		for(String id: planetIds) {
			firefox.get().findElement(By.id(id)).click();
			technologyService.buildSomething();
			firefox.loading();
		}	
	}
	
}
