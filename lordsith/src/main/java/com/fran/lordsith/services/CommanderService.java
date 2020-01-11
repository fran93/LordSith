package com.fran.lordsith.services;

import java.util.List;
import java.util.Locale;

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
    private PlanetService planetService;

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

	List<String> planetIds = planetService.getPlanetList();

	for (int i = 0; i < planetIds.size(); i++) {
	    managePlanets(planetIds, i);
	}

	returnToMainPlanet(planetIds);
	manageFleetService.scan();
	manageFleetService.hunting();

	loginService.logout();
	log.info(messageSource.getMessage("commander.done", null, Locale.ENGLISH));
    }

    private void managePlanets(List<String> planetIds, int i) throws InterruptedException {
	planetService.nextPlanet(planetIds.get(i));

	handlerService.scrapFleet();
	if (isMainPlanet(i)) {
	    handlerService.importExport();
	}

	manageFleetService.sendExpedition();
	if (!isMainPlanet(i)) {
	    manageFleetService.transportResources();
	    manageFleetService.deployFleet();
	}

	if (buildingService.buildMinesOrFacilities() && researchService.research()) {
	    hangarService.buildExpeditionFleet();
	    if (isMainPlanet(i)) {
		hangarService.buildPathfinderFleet();
		hangarService.buildHuntingFleet();
	    }
	    defenseService.buildDefenses(isMainPlanet(i));
	}
	firefox.shortLoading();
    }

    private void returnToMainPlanet(List<String> planetIds) throws InterruptedException {
	planetService.nextPlanet(planetIds.get(0));
    }

    private boolean isMainPlanet(int i) {
	return i == 0;
    }

}
