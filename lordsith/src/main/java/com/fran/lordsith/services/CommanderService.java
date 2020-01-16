package com.fran.lordsith.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class CommanderService {

    @Autowired
    @Lazy
    LoginService loginService;

    @Autowired
    @Lazy
    BuildingService buildingService;

    @Autowired
    @Lazy
    ResearchService researchService;

    @Autowired
    @Lazy
    FleetService manageFleetService;

    @Autowired
    @Lazy
    HangarService hangarService;

    @Autowired
    @Lazy
    FirefoxClient firefox;

    @Autowired
    @Lazy
    DefenseService defenseService;

    @Autowired
    @Lazy
    HandlerService handlerService;

    @Autowired
    @Lazy
    PlanetService planetService;

    @Autowired
    @Lazy
    MessageSource messageSource;

    Logger log = LoggerFactory.getLogger(CommanderService.class);

    /**
     * Bugs to fix here:
     *
     */
    public void command() throws InterruptedException {
        if (!loginService.isLogged()) {
            loginService.login();
        }

        for (int i = 0; i < planetService.countPlanets(); i++) {
            try {
                managePlanets(i);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        returnToMainPlanet();
        try {
            manageFleetService.scan();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            manageFleetService.hunting();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        loginService.logout();
        log.info(messageSource.getMessage("commander.done", null, Locale.ENGLISH));
    }

    private void managePlanets(int i) throws InterruptedException {
        planetService.nextPlanet(i);

        handlerService.scrapFleet();
        if (isMainPlanet(i)) {
            handlerService.importExport();
        }

        manageFleetService.sendExpedition();
        if (!isMainPlanet(i)) {
            manageFleetService.transportResources();
            manageFleetService.deployFleet();
        }

        if (buildingService.buildMinesOrFacilities()) {
            researchService.research();
        }

        if (planetService.hasGrowEnough()) {
            defenseService.buildDefenses(isMainPlanet(i));
            hangarService.buildDeathStar(isMainPlanet(i));
            hangarService.buildExpeditionFleet();
            hangarService.buildTransportFleet();
        }
        firefox.shortLoading();
    }

    private void returnToMainPlanet() throws InterruptedException {
        planetService.nextPlanet(0);
    }

    private boolean isMainPlanet(int i) {
        return i == 0;
    }

}
