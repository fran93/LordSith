package com.fran.lordsith.services;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class CommanderService {

	@Autowired @Lazy
	private LoginService loginService;
	
	@Autowired @Lazy
	private BuildingService buildingService;
	
	@Autowired @Lazy
	private ResearchService researchService;
	
	@Autowired @Lazy
	private ExpeditionService expeditionService;
	
	@Autowired @Lazy
	private HangarService hangarService;
	
	@Autowired @Lazy
	private FirefoxClient firefox;
	
	Logger log = LoggerFactory.getLogger(CommanderService.class);
	
	public void command() throws InterruptedException {
		if(!loginService.isLogged()) {
			loginService.login();
		}
		loginService.extractPoints();
		
		List<String> planetIds = new ArrayList<>();
		firefox.get().findElement(By.id("planetList")).findElements(By.className("smallplanet")).forEach(planet -> planetIds.add(planet.getAttribute("id")));
		
		for(int i=0; i<planetIds.size(); i++) {
			firefox.get().findElement(By.id(planetIds.get(i))).click();	
			expeditionService.sendExpedition(loginService.getPoints());
			
			if(buildingService.buildMinesOrFacilities() && researchService.research()) {
				hangarService.buildExpeditionFleet(loginService.getPoints());
				if(i==0) {
					hangarService.buildPathfinderFleet(loginService.getPoints());
				}
			}
			firefox.shortLoading();
		}
		
		log.info("That's all");
	}
	
}
