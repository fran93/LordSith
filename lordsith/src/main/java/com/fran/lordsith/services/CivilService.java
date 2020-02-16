package com.fran.lordsith.services;

import com.fran.lordsith.enums.MenuEnum;
import com.fran.lordsith.enums.TechnologyEnum;
import com.fran.lordsith.model.Resources;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;

@Service
public class CivilService {

  private static final double MINIMUM_METALL = 1000000;
  private static final double MINIMUM_DEUTERIUM = 500000;
  private static final double MINIMUM_KRISTALL = 500000;
  private static final double MINIMUM_RESOURCES = 1000000;
  private static final double MINIMUM_TRANSPORT = 500;
  private static final int MIN_FLEET_TO_DEPLOY = 50;
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
  @Autowired
  @Lazy
  TechnologyService technologyService;
  @Autowired
  @Lazy
  BuildingService buildingService;
  Logger log = LoggerFactory.getLogger(CivilService.class);

  public void transportResources() throws InterruptedException {
    menuService.openPage(MenuEnum.FLOTTE);

    if (fleetService.isThereAFleet() && fleetService.isFleetAvailable() && fleetService.numberOfShips(TechnologyEnum.KLEINER_TRANSPORTER.getId()) >= MINIMUM_TRANSPORT) {
      Resources amountToTransport = getAmountToTransport();

      if (amountToTransport.getMetall() > 0 || amountToTransport.getKristall() > 0 || amountToTransport.getDeuterium() > 0) {
        transportResources(amountToTransport);
      }
    }
  }

  public void deployFleet() throws InterruptedException {
    menuService.openPage(MenuEnum.FLOTTE);

    if (fleetService.isThereAFleet() && fleetService.isFleetAvailable()) {
      int countKreuzer = fleetService.numberOfShips(TechnologyEnum.KREUZER.getId());
      int countSchlachtKreuzer = fleetService.numberOfShips(TechnologyEnum.SCHLACHTKREUZER.getId());
      int countReaper = fleetService.numberOfShips(TechnologyEnum.REAPER.getId());
      int countZerstorer = fleetService.numberOfShips(TechnologyEnum.ZERSTORER.getId());
      int countPathfinder = fleetService.numberOfShips(TechnologyEnum.PATHFINDER.getId());

      if (countKreuzer > MIN_FLEET_TO_DEPLOY || countSchlachtKreuzer > MIN_FLEET_TO_DEPLOY || countZerstorer > MIN_FLEET_TO_DEPLOY || countPathfinder > MIN_FLEET_TO_DEPLOY
          || countReaper > MIN_FLEET_TO_DEPLOY) {

        selectAllShips(TechnologyEnum.LEICHTER_JAGER.getId());
        selectAllShips(TechnologyEnum.SCHWERER_JAGER.getId());
        selectAllShips(TechnologyEnum.KREUZER.getId());
        selectAllShips(TechnologyEnum.SCHLACHTSCHIFF.getId());
        selectAllShips(TechnologyEnum.SCHLACHTKREUZER.getId());
        selectAllShips(TechnologyEnum.BOMBER.getId());
        selectAllShips(TechnologyEnum.REAPER.getId());
        selectAllShips(TechnologyEnum.SPIONAGESONDE.getId());
        if (countPathfinder > 0) {
          firefox.get().findElement(By.name("explorer")).sendKeys(String.valueOf(countPathfinder - 2));
        }
        if (countZerstorer > 0) {
          firefox.get().findElement(By.name("destroyer")).sendKeys(String.valueOf(countZerstorer - 2));
        }

        if (fleetService.canContinue(FleetService.CONTINUE_TO_FLEET2)) {
          fleetService.weiterWeiter(FleetService.CONTINUE_TO_FLEET2);
          firefox.get().findElement(By.id("shortcuts")).findElements(By.className("glow")).get(0).click();

          Optional<WebElement> dropDown = firefox.get().findElements(By.className("dropdownList")).stream().filter(WebElement::isDisplayed).findFirst();
          dropDown.ifPresent(webElement -> webElement.findElements(By.tagName("li")).get(1).click());
          if (fleetService.canContinue(FleetService.CONTINUE_TO_FLEET3)) {
            fleetService.weiterWeiter(FleetService.CONTINUE_TO_FLEET3);

            firefox.get().findElement(By.id("missionButton4")).click();

            if (fleetService.canContinue(FleetService.SEND_FLEET)) {
              fleetService.weiterWeiter(FleetService.SEND_FLEET);

              log.info(messageSource.getMessage("fleet.deploy", null, Locale.ENGLISH));
            }
          }
        }

      }
    }
  }

  private void selectAllShips(int id) {
    if (fleetService.isStatusOn(id)) {
      Optional<WebElement> element = technologyService.getTechnologyById(id);
      element.ifPresent(WebElement::click);
    }
  }

  private void transportResources(Resources amountToTransport) throws InterruptedException {
    try {
      Optional<WebElement> element = technologyService.getTechnologyById(TechnologyEnum.KLEINER_TRANSPORTER.getId());
      element.ifPresent(webElement -> webElement.findElement(By.className("sprite_small")).click());

      if (fleetService.canContinue(FleetService.CONTINUE_TO_FLEET2)) {
        fleetService.weiterWeiter(FleetService.CONTINUE_TO_FLEET2);
        firefox.get().findElement(By.id("shortcuts")).findElements(By.className("glow")).get(0).click();

        Optional<WebElement> dropDown = firefox.get().findElements(By.className("dropdownList")).stream().filter(WebElement::isDisplayed).findFirst();
        dropDown.ifPresent(webElement -> webElement.findElements(By.tagName("li")).get(1).click());
        if (fleetService.canContinue(FleetService.CONTINUE_TO_FLEET3)) {
          fleetService.weiterWeiter(FleetService.CONTINUE_TO_FLEET3);

          firefox.get().findElement(By.id("missionButton3")).click();

          firefox.get().findElement(By.id("crystal")).sendKeys(String.valueOf(amountToTransport.getKristall()));
          firefox.get().findElement(By.id("deuterium")).sendKeys(String.valueOf(amountToTransport.getDeuterium()));
          firefox.get().findElement(By.id("metal")).sendKeys(String.valueOf(amountToTransport.getMetall()));

          if (fleetService.canContinue(FleetService.SEND_FLEET)) {
            fleetService.weiterWeiter(FleetService.SEND_FLEET);

            log.info(messageSource.getMessage("fleet.transport", null, Locale.ENGLISH));
          }
        }
      }
    } catch (ElementNotInteractableException ex) {
      log.info("transportResources: " + ex.getMessage());
    }
  }

  private Resources getAmountToTransport() {
    Resources resources = buildingService.parseResources();
    Resources amountToTransport = new Resources(0);
    if (resources.getKristall() > MINIMUM_RESOURCES && resources.getKristall() > resources.getMetall() * 2) {
      amountToTransport.setKristall((resources.getKristall() - MINIMUM_KRISTALL));
    }
    if (resources.getDeuterium() > MINIMUM_RESOURCES && resources.getDeuterium() > resources.getKristall() * 2) {
      amountToTransport.setDeuterium((resources.getDeuterium() - MINIMUM_DEUTERIUM));
    }
    if (resources.getMetall() > MINIMUM_RESOURCES && resources.getMetall() > resources.getKristall() * 5) {
      amountToTransport.setMetall((resources.getMetall() - MINIMUM_METALL));
    }
    return amountToTransport;
  }
}
