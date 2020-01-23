package com.fran.lordsith.services;

import com.fran.lordsith.enums.MenuEnum;
import com.fran.lordsith.enums.StatusEnum;
import com.fran.lordsith.enums.TechnologyEnum;
import com.fran.lordsith.model.Defense;
import org.openqa.selenium.By;
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

  private static final int BASE_RAKETENWERFER = 50000;
  private static final int BASE_LEICHTESLASERGESCHUTZ = 50000;
  private static final int BASE_SCHWERESLASERGESCHUTZ = 1000;
  private static final int BASE_IONENGESCHUZ = 1000;
  private static final int BASE_GAUSSKANONE = 500;
  private static final int BASE_PLASMAWERFER = 250;
  private static final int BASE_ABFANGRAKETE = 1000;

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

    public void buildDefenses(boolean isMainPlanet) throws InterruptedException {
        menuService.openPage(MenuEnum.VERTEIDIGUNG);

        int adjust = isMainPlanet ? 1 : 10;

        List<Defense> defenses = new ArrayList<>();
      defenses.add(new Defense(BASE_RAKETENWERFER, TechnologyEnum.RAKETENWERFER, false));
      defenses.add(new Defense(BASE_LEICHTESLASERGESCHUTZ, TechnologyEnum.LEICHTESLASERGESCHUTZ, false));
      defenses.add(new Defense(BASE_SCHWERESLASERGESCHUTZ, TechnologyEnum.SCHWERESLASERGESCHUTZ, false));
      defenses.add(new Defense(BASE_IONENGESCHUZ, TechnologyEnum.IONENGESCHUZ, false));
      defenses.add(new Defense(BASE_GAUSSKANONE, TechnologyEnum.GAUSSKANONE, false));
      defenses.add(new Defense(BASE_PLASMAWERFER, TechnologyEnum.PLASMAWERFER, false));
      defenses.add(new Defense(BASE_ABFANGRAKETE, TechnologyEnum.ABFANGRAKETE, false));
      defenses.add(new Defense(10, TechnologyEnum.KLEINE_SCHILDKUPPEL, true));
      defenses.add(new Defense(10, TechnologyEnum.GROSSE_SCHILDKUPPEL, true));

      List<String> queue = new ArrayList<>();
      firefox.get().findElements(By.className("queuePic")).forEach(pic -> queue.add(pic.getAttribute("alt").trim()));

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

  private int getAmount(int id) {
    try {
      Optional<WebElement> theShip = technologyService.getTechnologyById(id);
      return theShip.map(webElement -> Integer.parseInt(webElement.findElement(By.className("amount")).getAttribute("data-value"))).orElse(0);
    } catch (TimeoutException ex) {
      log.info("getAmount: " + ex.getMessage());
    }
    return 0;
  }

  private boolean isStatusOn(int id, List<String> queue) {
    boolean isInQueue = queue.stream().anyMatch(pic -> pic.endsWith("_" + id));
    Optional<WebElement> defense = technologyService.getTechnologyById(id);

    return defense.isPresent() && (defense.get().getAttribute("data-status").equals(StatusEnum.ON.getValue()) && !isInQueue);
  }
}
