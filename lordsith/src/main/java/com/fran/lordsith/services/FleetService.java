package com.fran.lordsith.services;

import com.fran.lordsith.enums.MenuEnum;
import com.fran.lordsith.enums.StatusEnum;
import com.fran.lordsith.properties.ExpeditionProperties;
import org.openqa.selenium.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FleetService {


  public static final String SEND_FLEET = "sendFleet";
  public static final String CONTINUE_TO_FLEET3 = "continueToFleet3";
  public static final String CONTINUE_TO_FLEET2 = "continueToFleet2";
  public static final int EXPEDITION_LEVEL_0 = 100000;
  public static final int EXPEDITION_LEVEL_1 = 1000000;
  public static final int EXPEDITION_LEVEL_2 = 5000000;
  public static final int EXPEDITION_LEVEL_3 = 25000000;
  public static final int EXPEDITION_LEVEL_4 = 50000000;
  public static final int EXPEDITION_LEVEL_5 = 75000000;
  public static final int EXPEDITION_LEVEL_6 = 100000000;

  @Autowired
  @Lazy
  FirefoxClient firefox;

  @Autowired
  @Lazy
  MenuService menuService;

  @Autowired
  ExpeditionProperties expeditionProperties;

  @Autowired
  @Lazy
  TechnologyService technologyService;

  Logger log = LoggerFactory.getLogger(FleetService.class);

  public int calculateNumberOfCargos(long points) {
    if (points < EXPEDITION_LEVEL_0) {
      return expeditionProperties.getLevel0();
    } else if (points < EXPEDITION_LEVEL_1) {
      return expeditionProperties.getLevel1();
    } else if (points < EXPEDITION_LEVEL_2) {
      return expeditionProperties.getLevel2();
    } else if (points < EXPEDITION_LEVEL_3) {
      return expeditionProperties.getLevel3();
    } else if (points < EXPEDITION_LEVEL_4) {
      return expeditionProperties.getLevel4();
    } else if (points < EXPEDITION_LEVEL_5) {
      return expeditionProperties.getLevel5();
    } else if (points < EXPEDITION_LEVEL_6) {
      return expeditionProperties.getLevel6();
    } else {
      return expeditionProperties.getLevel7();
    }
  }

  public boolean canContinue(String id) {
    return firefox.get().findElement(By.id(id)).getAttribute("class").trim().endsWith("on");
  }

  public void weiterWeiter(String id) throws InterruptedException {
    firefox.jsClick(firefox.get().findElement(By.id(id)));
    firefox.loading(1);
  }

  public boolean isThereAFleet() {
    return firefox.get().findElements(By.className("icon_warning")).isEmpty();
  }

  public boolean isStatusOn(int id) {
    Optional<WebElement> defense = technologyService.getTechnologyById(id);
    return defense.isPresent() && (defense.get().getAttribute("data-status").equals(StatusEnum.ON.getValue()));
  }

  public int numberOfShips(int id) {
    try {
      Optional<WebElement> theShip = technologyService.getTechnologyById(id);
      return theShip.map(webElement -> Integer.parseInt(webElement.findElement(By.className("amount")).getAttribute("data-value"))).orElse(0);
    } catch (StaleElementReferenceException ex) {
      log.info("NumberOfShips: ", ex.getMessage());
    }

    return 0;
  }

  public boolean isFleetAvailable() {
    if (menuService.isOnPage(MenuEnum.FLOTTE)) {
      try {
        firefox.loading(By.id("slots"));
        List<WebElement> slotElements = firefox.get().findElement(By.id("slots")).findElements(By.className("fleft"));
        String rawSlots = slotElements.get(0).getText();
        String[] rawSlotSplit = rawSlots.split(":");
        if (rawSlotSplit.length > 1) {
          String splitedSlots = rawSlotSplit[1].trim();

          int currentSlots = Integer.parseInt(splitedSlots.split("/")[0]);
          int maxSlots = Integer.parseInt(splitedSlots.split("/")[1]);

          return currentSlots < maxSlots;
        }
      } catch (NoSuchElementException | TimeoutException ex) {
        log.info("isFleetAvailable: " + ex.getMessage());
      }
    }
    return false;
  }

}
