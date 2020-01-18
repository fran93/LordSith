package com.fran.lordsith.services;

import com.fran.lordsith.enums.MenuEnum;
import com.fran.lordsith.enums.TechnologyEnum;
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
public class ExpeditionService {

  @Autowired
  @Lazy
  PlanetService planetService;

  @Autowired
  @Lazy
  MenuService menuService;

  @Autowired
  @Lazy
  FleetService fleetService;

  @Autowired
  @Lazy
  FirefoxClient firefox;

  @Autowired
  @Lazy
  MessageSource messageSource;

  Logger log = LoggerFactory.getLogger(ExpeditionService.class);

  public void sendExpedition() throws InterruptedException {
    if (planetService.hasPoints()) {
      menuService.openPage(MenuEnum.FLOTTE);

      sendExpedition2();
    }
  }

  private void sendExpedition2() throws InterruptedException {
    firefox.loading(2);
    if (isExpeditionAvailable() && fleetService.isStatusOn(TechnologyEnum.GROSSER_TRANSPORTER.getId())
        && fleetService.numberOfShips(TechnologyEnum.GROSSER_TRANSPORTER.getId()) > fleetService.calculateNumberOfCargos(planetService.getPoints()) / 2) {
      firefox.get().findElement(By.name("transporterLarge")).sendKeys(String.valueOf(fleetService.calculateNumberOfCargos(planetService.getPoints())));

      if (fleetService.isStatusOn(TechnologyEnum.PATHFINDER.getId())) {
        firefox.get().findElement(By.name("explorer")).sendKeys("1");
      }

      if (fleetService.isStatusOn(TechnologyEnum.ZERSTORER.getId())) {
        firefox.get().findElement(By.name("destroyer")).sendKeys("1");
      }

      firefox.loading(2);

      if (fleetService.canContinue(FleetService.CONTINUE_TO_FLEET2)) {
        fleetService.weiterWeiter(FleetService.CONTINUE_TO_FLEET2);

        firefox.get().findElement(By.id("position")).sendKeys("16");

        if (fleetService.canContinue(FleetService.CONTINUE_TO_FLEET3)) {
          fleetService.weiterWeiter(FleetService.CONTINUE_TO_FLEET3);

          if (fleetService.canContinue(FleetService.SEND_FLEET)) {
            fleetService.weiterWeiter(FleetService.SEND_FLEET);

            firefox.loading(2);
            log.info(messageSource.getMessage("fleet.expedition", null, Locale.ENGLISH));
          }
        }
      }
    }
  }

  private boolean isExpeditionAvailable() {
    if (menuService.isOnPage(MenuEnum.FLOTTE)) {
      List<WebElement> slotElements = firefox.get().findElement(By.id("slots")).findElements(By.className("fleft"));
      String rawExpe = slotElements.get(1).getText();
      String splitedExpeditions = rawExpe.split(":")[1].trim();

      int currentExpeditions = Integer.parseInt(splitedExpeditions.split("/")[0]);
      int maxExpeditions = Integer.parseInt(splitedExpeditions.split("/")[1]);

      return fleetService.isFleetAvailable() && fleetService.isThereAFleet() && currentExpeditions < maxExpeditions;
    }
    return false;
  }

}
