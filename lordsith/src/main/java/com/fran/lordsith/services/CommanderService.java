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
  HangarService hangarService;

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

  @Autowired
  @Lazy
  FirefoxClient firefoxClient;

  @Autowired
  @Lazy
  ExpeditionService expeditionService;

  @Autowired
  @Lazy
  CivilService civilService;

  @Autowired
  @Lazy
  MilitaryService militaryService;

  @Autowired
  @Lazy
  IntelligentService intelligentService;

  Logger log = LoggerFactory.getLogger(CommanderService.class);

  public void command() throws InterruptedException {
    if (!loginService.isLogged()) {
      loginService.login();
    }

    managePlanets(0);
    for (int i = planetService.countPlanets() - 1; i > 0; i--) {
      managePlanets(i);
    }
    goToMainPlanet();

    militaryService.hunting();
    intelligentService.scan();
    firefoxClient.waiting();
    militaryService.hunting();


    loginService.logout();
    log.info(messageSource.getMessage("commander.done", null, Locale.ENGLISH));
  }

  private void managePlanets(int i) throws InterruptedException {
    loginService.removeBanner();
    planetService.nextPlanet(i);

    handlerService.scrapFleet();
    if (isMainPlanet(i)) {
      handlerService.importExport();
    }

    expeditionService.sendExpedition();
    if (!isMainPlanet(i)) {
      civilService.deployFleet();
      civilService.transportResources();
    }

    if (buildingService.buildMinesOrFacilities() && isMainPlanet(i)) {
      researchService.research();
    }

    if (planetService.hasGrowEnough()) {
      defenseService.buildDefenses(isMainPlanet(i));
      hangarService.prepareHangar();
      hangarService.buildDeathStar(isMainPlanet(i));
      hangarService.buildExpeditionFleet();
    }
  }

  private void goToMainPlanet() throws InterruptedException {
    planetService.nextPlanet(0);
  }

  private boolean isMainPlanet(int i) {
    return i == 0;
  }

}
