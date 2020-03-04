package com.fran.lordsith.services;

import com.fran.lordsith.enums.MenuEnum;
import com.fran.lordsith.enums.StatusEnum;
import com.fran.lordsith.enums.TechnologyEnum;
import com.fran.lordsith.model.Defense;
import com.fran.lordsith.properties.DefenseProperties;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DefenseService {

  @Autowired
  DefenseProperties defenseProperties;

  @Autowired
  @Lazy
  FirefoxClient firefox;

  @Autowired
  @Lazy
  PlanetService planetService;

  @Autowired
  @Lazy
  MenuService menuService;

  @Autowired
  @Lazy
  TechnologyService technologyService;

  Logger log = LoggerFactory.getLogger(DefenseService.class);

  public long calculateNumberOfDefense(int baseNumber, long points) {
    if (points < 50000) {
      return 0;
    } else if (points < 100000) {
      return (long) (baseNumber * 0.25);
    } else if (points < 500000) {
      return (long) (baseNumber * 0.50);
    } else if (points < 1000000) {
      return (long) (baseNumber * 0.75);
    } else {
      return (points / 1000000) * baseNumber;
    }
  }

  public int calculateAdjust(boolean isMainPlanet) {
    if (isMainPlanet) {
      return 1;
    }
    if (planetService.getCurrentFields() < 160) {
      return 60;
    }
    if (planetService.getCurrentFields() < 170) {
      return 30;
    }
    if (planetService.getCurrentFields() < 180) {
      return 25;
    }
    if (planetService.getCurrentFields() < 190) {
      return 20;
    }
    if (planetService.getCurrentFields() < 200) {
      return 15;
    } else {
      return 10;
    }
  }

  public void buildDefenses(boolean isMainPlanet) throws InterruptedException {
    menuService.openPage(MenuEnum.VERTEIDIGUNG);

    try {
      int adjust = calculateAdjust(isMainPlanet);

      List<Defense> defenses = new ArrayList<>();
      defenses.add(new Defense(defenseProperties.getRaketenwerfer(), TechnologyEnum.RAKETENWERFER, false));
      defenses.add(new Defense(defenseProperties.getLeichteslasergeschutz(), TechnologyEnum.LEICHTESLASERGESCHUTZ, false));
      defenses.add(new Defense(defenseProperties.getSchwereslasergeschutz(), TechnologyEnum.SCHWERESLASERGESCHUTZ, false));
      defenses.add(new Defense(defenseProperties.getIonengeschuz(), TechnologyEnum.IONENGESCHUZ, false));
      defenses.add(new Defense(defenseProperties.getGausskanone(), TechnologyEnum.GAUSSKANONE, false));
      defenses.add(new Defense(defenseProperties.getPlasmawerfer(), TechnologyEnum.PLASMAWERFER, false));
      defenses.add(new Defense(10, TechnologyEnum.KLEINE_SCHILDKUPPEL, true));
      defenses.add(new Defense(10, TechnologyEnum.GROSSE_SCHILDKUPPEL, true));

      List<String> queue = new ArrayList<>();
      firefox.get().findElements(By.className("queuePic")).forEach(pic -> queue.add(pic.getAttribute("alt").trim()));

      if (isStatusOn(TechnologyEnum.ABFANGRAKETE.getId(), queue)) {
        technologyService.build(TechnologyEnum.ABFANGRAKETE, defenseProperties.getAbfangrakete());
      } else {
        defenses.forEach(defense -> defense.setAmountToBuild((calculateNumberOfDefense(defense.getBaseAmount(), planetService.getPoints()) / adjust) - getAmount(defense.getTechnology().getId())));
        Optional<Defense> defenseToBuild = defenses.stream().filter(defense -> isStatusOn(defense.getTechnology().getId(), queue) && defense.getAmountToBuild() > 0).findFirst();
        if (defenseToBuild.isPresent()) {
          if (defenseToBuild.get().isUnique()) {
            technologyService.build(defenseToBuild.get().getTechnology());
          } else {
            technologyService.build(defenseToBuild.get().getTechnology(), defenseToBuild.get().getAmountToBuild());
          }
        }
      }
    } catch (TimeoutException | StaleElementReferenceException ex) {
      log.info("buildDefenses: " + ex.getMessage());
    }
  }

  private int getAmount(int id) {
    Optional<WebElement> theShip = technologyService.getTechnologyById(id);
    return theShip.map(webElement -> Integer.parseInt(webElement.findElement(By.className("amount")).getAttribute("data-value"))).orElse(0);
  }

  private boolean isStatusOn(int id, List<String> queue) {
    boolean isInQueue = queue.stream().anyMatch(pic -> pic.endsWith("_" + id));
    Optional<WebElement> defense = technologyService.getTechnologyById(id);

    return defense.isPresent() && (defense.get().getAttribute("data-status").equals(StatusEnum.ON.getValue()) && !isInQueue);
  }
}
