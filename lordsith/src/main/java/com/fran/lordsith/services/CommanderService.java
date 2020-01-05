package com.fran.lordsith.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
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

    @Autowired
    @Lazy
    private MessageSource messageSource;

    Logger log = LoggerFactory.getLogger(CommanderService.class);

    /**
     * Bugs to fix here:
     * 
     * @throws InterruptedException
     */
    public void command() throws InterruptedException {
	if (!loginService.isLogged()) {
	    loginService.login();
	}
	loginService.extractPoints();

	List<String> planetIds = new ArrayList<>();
	firefox.get().findElement(By.id("planetList")).findElements(By.className("smallplanet")).forEach(planet -> planetIds.add(planet.getAttribute("id")));

	for (int i = 0; i < planetIds.size(); i++) {
	    managePlanets(planetIds, i);
	}

	returnToMainPlanet(planetIds);
	manageFleetService.hunting();

	loginService.logout();
	log.info(messageSource.getMessage("commander.done", null, Locale.ENGLISH));
    }

    private void managePlanets(List<String> planetIds, int i) throws InterruptedException {
	firefox.shortLoading();
	firefox.get().findElement(By.id(planetIds.get(i))).click();

	handlerService.scrapFleet(loginService.getPoints());
	if (isMainPlanet(i)) {
	    handlerService.importExport();
	}

	if (loginService.hasPoints()) {
	    manageFleetService.sendExpedition(loginService.getPoints());
	    if (!isMainPlanet(i)) {
		manageFleetService.transportResources();
	    }
	}

	if (isMainPlanet(i)) {
	    manageFleetService.scan();
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

    private void returnToMainPlanet(List<String> planetIds) {
	firefox.get().findElement(By.id(planetIds.get(0))).click();
    }

    private boolean isMainPlanet(int i) {
	return i == 0;
    }

}
