package com.fran.lordsith.services;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

@Service
@Log4j2
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

  public void command() throws Exception {
    if (automaticMode()) {
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
    } else {
      log.info(messageSource.getMessage("commander.waiting", null, Locale.ENGLISH));
    }
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

    if ((buildingService.buildMinesOrFacilities() || planetService.hasGrowEnough()) && isMainPlanet(i)) {
      researchService.research();
    }

    if (planetService.hasGrowEnough()) {
      defenseService.buildDefenses(isMainPlanet(i));
      hangarService.prepareHangar();
      hangarService.buildDeathStar(isMainPlanet(i));
      hangarService.buildExpeditionFleet();
    }
  }

  private void goToMainPlanet() {
    planetService.nextPlanet(0);
  }

  private boolean isMainPlanet(int i) {
    return i == 0;
  }

  private boolean automaticMode() throws IOException {
    Path path = Paths.get("automaticMode");
    boolean automaticMode = true;

    if (Files.exists(path)) {
      automaticMode = Files.readAllLines(path).stream().findFirst().orElse("1").trim().equals("1");
    }

    return automaticMode;
  }

}
