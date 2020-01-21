package com.fran.lordsith.services;

import com.fran.lordsith.enums.MenuEnum;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class HandlerService {

  private static final String CLASS = "class";

  @Autowired
  @Lazy
  FirefoxClient firefox;

  @Autowired
  @Lazy
  MessageSource messageSource;

  @Autowired
  @Lazy
  FleetService fleetService;

  @Autowired
  @Lazy
  PlanetService planetService;

  @Autowired
  @Lazy
  MenuService menuService;

  @Autowired
  @Lazy
  LoginService loginService;

  Logger log = LoggerFactory.getLogger(HandlerService.class);

  public void scrapFleet() throws InterruptedException {
    menuService.openPage(MenuEnum.HANDLER);

    firefox.loading(By.id("js_traderScrap"));
    firefox.get().findElement(By.id("js_traderScrap")).click();

    if (firefox.get().getCurrentUrl().contains("page=traderScrap")) {
      if (planetService.hasPoints()) {
        firefox.loading(By.className("forward"));
        firefox.loading(1);
        firefox.get().findElement(By.className("forward")).click();
        firefox.loading(1);
        String rawAmount = firefox.get().findElement(By.id("button203")).findElement(By.className("amount")).getText().replaceAll("\\.", "");
        if (!rawAmount.trim().isEmpty()) {
          long current = Long.parseLong(rawAmount);
          long base = fleetService.calculateNumberOfCargos(planetService.getPoints());
          long desired = current - (base + base / 2);

          if (desired > 0) {
            firefox.get().findElement(By.id("ship_203")).sendKeys(String.valueOf(desired));
          }
        }
      }

      firefox.loading(1);
      if (!firefox.get().findElement(By.id("js_scrapScrapIT")).getAttribute(CLASS).contains("disabled")) {
        firefox.get().findElement(By.id("js_scrapScrapIT")).click();

        firefox.get().findElement(By.className("yes")).click();

        log.info(messageSource.getMessage("handler.scrap", null, Locale.ENGLISH));
      }
    }
  }

  public void importExport() throws InterruptedException {
    firefox.loading(1);
    firefox.loading(By.className("back_to_overview"));
    firefox.get().findElement(By.className("back_to_overview")).click();
    firefox.loading(2);
    firefox.loading(By.id("js_traderImportExport"));
    firefox.get().findElement(By.id("js_traderImportExport")).click();

    try {
      if (!firefox.get().findElement(By.className("got_item_text")).isDisplayed()) {
        firefox.loading(1);
        firefox.get().findElement(By.className("js_sliderMetalMax")).click();
        if (!firefox.get().findElement(By.className("pay")).getAttribute(CLASS).contains("disabled")) {
          firefox.loading(1);
          firefox.get().findElement(By.className("pay")).click();

          firefox.loading(1);
          firefox.get().findElement(By.className("take")).click();
          firefox.loading(1);
        }
      }
    } catch (NoSuchElementException ex) {
      log.info("ImportExport: " + ex.getMessage());
      firefox.quit();
      firefox.restart();
      loginService.login();
    }
  }
}
