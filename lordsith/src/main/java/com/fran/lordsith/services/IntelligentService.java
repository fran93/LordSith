package com.fran.lordsith.services;

import com.fran.lordsith.enums.MenuEnum;
import org.openqa.selenium.By;
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
  }

  private void recycle() {
    List<WebElement> expeditionSlotBox = firefox.get().findElements(By.className("expeditionDebrisSlotBox"));
    if (!expeditionSlotBox.isEmpty()) {
      firefox.mouseOver(expeditionSlotBox.get(0).findElement(By.className("js_bday_debris")));
      WebElement debris = firefox.get().findElement(By.id("debris16"));
      String debrisRecyclers = debris.findElement(By.className("debris-recyclers")).getText();
      if (!debrisRecyclers.trim().isEmpty()) {
        int requiredRecycles = Integer.parseInt(debrisRecyclers.split(":")[1].trim());
        if (requiredRecycles >= 5 && !debris.findElements(By.tagName("a")).isEmpty()) {
          debris.findElement(By.tagName("a")).click();

          log.info(messageSource.getMessage("fleet.recycle", null, Locale.ENGLISH));
        }
      }
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
    if (system > 499) {
      system -= 499;
    }
    firefox.get().findElement(By.id(SYSTEM_INPUT)).sendKeys(String.valueOf(system));
    firefox.get().findElement(By.id(SYSTEM_INPUT)).submit();
  }

  private int getCurrentSystem() {
    return Integer.parseInt(firefox.get().findElement(By.id(SYSTEM_INPUT)).getAttribute("value"));
  }

  private int spy() throws InterruptedException {
    firefox.loading(1);
    List<WebElement> inactives = firefox.get().findElements(By.className("inactive_filter"));
    for (WebElement inactive : inactives) {
      firefox.loading(1);
      inactive.findElement(By.className("espionage")).click();
    }

    return inactives.size();
  }

  private int getGalaxyFreeSlots() {
    if (menuService.isOnPage(MenuEnum.GALAXIE)) {
      String[] slotValue = firefox.get().findElement(By.id("slotValue")).getText().split("/");
      return Integer.parseInt(slotValue[1]) - Integer.parseInt(slotValue[0]);
    }
    return 0;
  }

  private boolean isSondeAvailable() {
    return Integer.parseInt(firefox.get().findElement(By.id("probeValue")).getText().trim().replaceAll("\\.", "")) > 5;
  }


}
