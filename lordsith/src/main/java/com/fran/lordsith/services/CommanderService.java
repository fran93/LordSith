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
	private ManageFleetService expeditionService;
	
	@Autowired @Lazy
	private HangarService hangarService;
	
	@Autowired @Lazy
	private FirefoxClient firefox;
	
	@Autowired @Lazy
	private DefenseService defenseService;
	
	Logger log = LoggerFactory.getLogger(CommanderService.class);
	
	private int exhaustion = 0;
	
	/**
	 * Bugs to fix here:
	 * @throws InterruptedException
	 */
	public void command() throws InterruptedException {
		if(exhaustion >= 4) {
			log.info("Bring me a new team!");
			firefox.restart();
			exhaustion=0;
		} else {
			exhaustion++;
		}
		
		if(!loginService.isLogged()) {
			loginService.login();
		}
		loginService.extractPoints();
		
		List<String> planetIds = new ArrayList<>();
		firefox.get().findElement(By.id("planetList")).findElements(By.className("smallplanet")).forEach(planet -> planetIds.add(planet.getAttribute("id")));
		
		for(int i=0; i<planetIds.size(); i++) {
			firefox.get().findElement(By.id(planetIds.get(i))).click();	
			expeditionService.sendExpedition(loginService.getPoints());
			if(isMainPlanet(i)) {
				expeditionService.hunting();
			}
			
			if(buildingService.buildMinesOrFacilities() && researchService.research()) {
				hangarService.buildExpeditionFleet(loginService.getPoints());
				if(isMainPlanet(i)) {
					hangarService.buildPathfinderFleet(loginService.getPoints());
				}
				defenseService.buildDefenses(i, loginService.getPoints());
			}
			firefox.shortLoading();
		}
		
		returnToMainPlanet(planetIds);	
		expeditionService.hunting();
		
		log.info("That's all");
	}

	private void returnToMainPlanet(List<String> planetIds) {
		firefox.get().findElement(By.id(planetIds.get(0))).click();
	}
	
	private boolean isMainPlanet(int i) {
		return i==0;
	}
	
	public void maxExhaustion() {
		exhaustion = 10;
	}
}
