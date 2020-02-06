package com.fran.lordsith.services;

import com.fran.lordsith.enums.MenuEnum;
import org.openqa.selenium.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class PlanetService {

  private final static int MIN_DEVELOPMENT = 150;
  @Autowired
  @Lazy
  FirefoxClient firefox;
  @Autowired
  @Lazy
  MessageSource messageSource;
  @Autowired
  @Lazy
  MenuService menuService;
  Logger log = LoggerFactory.getLogger(PlanetService.class);
  private long points;
  private int currentFields;
  private int maxFields;

  public void extractPoints() {
    try {
      firefox.loading(By.id("scoreContentField"));
      String scoreContentField = firefox.get().findElement(By.id("scoreContentField")).getText();
      if (!scoreContentField.isEmpty()) {
        points = Long.parseLong(scoreContentField.split(" ")[0].replaceAll("\\.", ""));
      }
    } catch (NoSuchElementException | TimeoutException | NumberFormatException ex) {
      log.info("extractPoints: " + ex.getMessage());
    }
  }

  public void nextPlanet(int index) {
    try {
      menuService.openPage(MenuEnum.UBERSICHT);
      WebElement planet = getPlanetByIndex(index);
      String name = planet.findElement(By.className("planet-name")).getText();
      planet.click();
      firefox.loading(3);
      extractPoints();
      extractFreeFields();
      log.info(messageSource.getMessage("login.points", new Object[]{points}, Locale.ENGLISH));
      log.info(messageSource.getMessage("planet.fields", new Object[]{name, getFreeFields()}, Locale.ENGLISH));
    } catch (InterruptedException | IndexOutOfBoundsException ex) {
      log.info("nextPlanet: " + ex.getMessage());
    }
  }

  public WebElement getPlanetByIndex(int index) {
    return getPlanetList().get(index);
  }

  public List<WebElement> getPlanetList() {
    menuService.openPage(MenuEnum.UBERSICHT);

    return firefox.get().findElements(By.className("smallplanet"));
  }

  public int countPlanets() {
    return getPlanetList().size();
  }

  public void extractFreeFields() {
    try {
      List<WebElement> spans = firefox.get().findElement(By.id("diameterContentField")).findElements(By.tagName("span"));
      if (!spans.isEmpty()) {
        currentFields = Integer.parseInt(spans.get(0).getText());
        maxFields = Integer.parseInt(spans.get(1).getText());
      }
    } catch (StaleElementReferenceException | NumberFormatException ex) {
      log.info("extractFreeFields: " + ex.getMessage());
    }
  }

  public boolean hasGrowEnough() {
    return currentFields > MIN_DEVELOPMENT;
  }

  public boolean hasPoints() {
    return points > 0;
  }

  public boolean hasFreeFields() {
    return getFreeFields() > 1;
  }

  public int getFreeFields() {
    return maxFields - currentFields;
  }

  public int getCurrentFields() {
    return currentFields;
  }

  public boolean hasFields() {
    return currentFields > 0 && maxFields > 0;
  }

  public long getPoints() {
    return points;
  }
}
