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

    @Autowired
    @Lazy
    private LoginService loginService;

    @Autowired
    @Lazy
    private BuildingService buildingService;

    @Autowired
    @Lazy
    private ResearchService researchService;

    @Autowired
    @Lazy
    private FleetService manageFleetService;

    @Autowired
    @Lazy
    private HangarService hangarService;

    @Autowired
    @Lazy
    private FirefoxClient firefox;

    @Autowired
    @Lazy
    private DefenseService defenseService;

    @Autowired
    @Lazy
    private HandlerService handlerService;

    Logger log = LoggerFactory.getLogger(CommanderService.class);

    private int exhaustion = 0;

    /**
     * Bugs to fix here:
     * 
     * @throws InterruptedException
     */
    public void command() throws InterruptedException {
	if (exhaustion >= 4) {
	    log.info("Bring me a new team!");
	    firefox.restart();
	    exhaustion = 0;
	} else {
	    exhaustion++;
	}

	if (!loginService.isLogged()) {
	    loginService.login();
	}
	loginService.extractPoints();

	List<String> planetIds = new ArrayList<>();
	firefox.get().findElement(By.id("planetList")).findElements(By.className("smallplanet")).forEach(planet -> planetIds.add(planet.getAttribute("id")));

	for (int i = 0; i < planetIds.size(); i++) {
	    firefox.shortLoading();
	    firefox.get().findElement(By.id(planetIds.get(i))).click();
	    if(loginService.hasPoints()) {
		manageFleetService.sendExpedition(loginService.getPoints());
	    }
	    
	    if (isMainPlanet(i)) {
		manageFleetService.scan();
	    }
	    handlerService.scrapFleet();
	    if (isMainPlanet(i)) {
		handlerService.importExport();
	    }

	    if (buildingService.buildMinesOrFacilities() && researchService.research()) {
		hangarService.buildExpeditionFleet(loginService.getPoints());
		if (isMainPlanet(i)) {
		    hangarService.buildPathfinderFleet(loginService.getPoints());
		    hangarService.buildHuntingFleet();
		}
		defenseService.buildDefenses(i, loginService.getPoints());
	    }
	    firefox.shortLoading();
	}

	returnToMainPlanet(planetIds);
	manageFleetService.hunting();

	log.info("That's all");
    }

    private void returnToMainPlanet(List<String> planetIds) {
	firefox.get().findElement(By.id(planetIds.get(0))).click();
    }

    private boolean isMainPlanet(int i) {
	return i == 0;
    }

    public void maxExhaustion() {
	exhaustion = 10;
    }
}
