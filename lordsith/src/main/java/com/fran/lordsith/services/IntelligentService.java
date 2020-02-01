package com.fran.lordsith.services;

import com.fran.lordsith.enums.MenuEnum;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class IntelligentService {

  private static final int MAX_SPY_REPORTS = 10;
  private static final int ATTACK_SYSTEM_RANGE = 150;
  private static final String SYSTEM_INPUT = "system_input";
  public static final int MIN_REQUIRED_RECYCLES = 5;
  @Autowired
  @Lazy
  MenuService menuService;
  @Autowired
  @Lazy
  FirefoxClient firefox;
  @Autowired
  @Lazy
  MessageSource messageSource;
  Logger log = LoggerFactory.getLogger(IntelligentService.class);
  private int leftSystem;
  private int rightSystem;

  public void scan() throws InterruptedException {
    menuService.openPage(MenuEnum.GALAXIE);

    try {
      int planetSystem = getCurrentSystem();
      int reportCount = 0;

      while (getGalaxyFreeSlots() > 0 && reportCount < MAX_SPY_REPORTS && isSondeAvailable()) {
        if (!(rightSystem > 0 && planetSystem == getCurrentSystem())) {
          log.info(messageSource.getMessage("fleet.spy", new Object[]{getCurrentSystem()}, Locale.ENGLISH));
          recycle();
          reportCount += spy();
        }

        if (rightSystem > ATTACK_SYSTEM_RANGE && leftSystem > ATTACK_SYSTEM_RANGE) {
          goToSystem(planetSystem);
          rightSystem = 0;
          leftSystem = 0;
        } else if (rightSystem > ATTACK_SYSTEM_RANGE) {
          goLeft(planetSystem);
        } else if (leftSystem > ATTACK_SYSTEM_RANGE) {
          goRight(planetSystem);
        } else if (rightSystem == 0) {
          goRight(planetSystem);
        } else if (leftSystem >= rightSystem) {
          goRight(planetSystem);
        } else {
          goLeft(planetSystem);
        }
      }
    } catch (NoSuchElementException ex) {
      log.info("scan: " + ex.getMessage());
    }
  }

  private void recycle() {
    try {
      firefox.loading(2);
      List<WebElement> expeditionSlotBox = firefox.get().findElements(By.className("expeditionDebrisSlotBox"));
      if (!expeditionSlotBox.isEmpty()) {
        firefox.loading(1);
        firefox.mouseOver(expeditionSlotBox.get(0).findElement(By.className("js_bday_debris")));
        WebElement debris = firefox.get().findElement(By.id("debris16"));
        String debrisRecyclers = debris.findElement(By.className("debris-recyclers")).getText();
        if (!debrisRecyclers.trim().isEmpty()) {
          int requiredRecycles = Integer.parseInt(debrisRecyclers.split(":")[1].trim());
          if (requiredRecycles >= MIN_REQUIRED_RECYCLES && !debris.findElements(By.tagName("a")).isEmpty()) {
            debris.findElement(By.tagName("a")).click();

            log.info(messageSource.getMessage("fleet.recycle", null, Locale.ENGLISH));
          }
        }
      }
    } catch (StaleElementReferenceException | InterruptedException ex) {
      log.info("recycle: " + ex.getMessage());
    }
  }

  private void goRight(int planetSystem) {
    rightSystem++;
    goToSystem(planetSystem + rightSystem);
  }

  private void goLeft(int planetSystem) {
    leftSystem++;
    goToSystem(planetSystem - leftSystem);
  }

  private void goToSystem(int system) {
    try {
      if (system > 499) {
        system -= 499;
      }
      firefox.loading(By.id(SYSTEM_INPUT));
      firefox.get().findElement(By.id(SYSTEM_INPUT)).sendKeys(String.valueOf(system));
      firefox.get().findElement(By.id(SYSTEM_INPUT)).submit();
    } catch (NoSuchElementException ex) {
      log.info("goToSystem: " + ex.getMessage());
    }
  }

  private int getCurrentSystem() {
    try {
      return Integer.parseInt(firefox.get().findElement(By.id(SYSTEM_INPUT)).getAttribute("value"));
    } catch (StaleElementReferenceException ex) {
      log.info("getCurrentSystem: " + ex.getMessage());
    }

    return 0;
  }

  private int spy() throws InterruptedException {
    firefox.loading(1);
    List<WebElement> inactives = firefox.get().findElements(By.className("inactive_filter"));
    for (WebElement inactive : inactives) {
      firefox.loading(1);
      try {
        inactive.findElement(By.className("espionage")).click();
      } catch (StaleElementReferenceException ex) {
        log.info("Spy: " + ex.getMessage());
      }
    }

    return inactives.size();
  }

  private int getGalaxyFreeSlots() {
    if (menuService.isOnPage(MenuEnum.GALAXIE)) {
      try {
        String[] slotValue = firefox.get().findElement(By.id("slotValue")).getText().split("/");
        return Integer.parseInt(slotValue[1]) - Integer.parseInt(slotValue[0]);
      } catch (StaleElementReferenceException | NoSuchElementException ex) {
        log.info("getGalaxyFreeSlots: " + ex.getMessage());
      }
    }
    return 0;
  }

  private boolean isSondeAvailable() {
    try {
      return Integer.parseInt(firefox.get().findElement(By.id("probeValue")).getText().trim().replaceAll("\\.", "")) > 5;
    } catch (StaleElementReferenceException ex) {
      log.info("isSondeAvailable: " + ex.getMessage());
    }
    return false;
  }


}
