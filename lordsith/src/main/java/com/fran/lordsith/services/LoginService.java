package com.fran.lordsith.services;

import com.fran.lordsith.enums.MenuEnum;
import com.fran.lordsith.properties.OgameProperties;
import org.openqa.selenium.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class LoginService {

  public static final int MAX_EXHAUSTION = 5;

  @Autowired
  OgameProperties ogameProperties;

  @Autowired
  @Lazy
  FirefoxClient firefox;

  @Autowired
  @Lazy
  MessageSource messageSource;

  Logger log = LoggerFactory.getLogger(LoginService.class);

  private int exhaustion;

  public void login() throws InterruptedException {
    handleExhaustion();
    firefox.get().get(ogameProperties.getUrl());

    removeBanner();

    if (firefox.get().findElements(By.className("hub-logo")).isEmpty()) {
      firefox.get().findElement(By.id("loginRegisterTabs")).findElement(By.tagName("span")).click();
      firefox.get().findElement(By.name("email")).sendKeys(ogameProperties.getEmail());
      firefox.get().findElement(By.name("password")).sendKeys(ogameProperties.getPassword());
      firefox.get().findElement(By.xpath("//button[@type='submit']")).click();
    }

    firefox.get().findElement(By.id("joinGame")).findElement(By.tagName("a")).findElement(By.tagName("button")).click();
    firefox.get().findElement(By.id("accountlist")).findElement(By.tagName("button")).click();
    firefox.loading(1);
    firefox.closeTab();
  }

  public void removeBanner() {
    if (!firefox.get().findElements(By.className("openX_int_closeButton")).isEmpty()) {
      firefox.get().findElement(By.className("openX_int_closeButton")).findElement(By.tagName("a")).click();
    }
  }

  private void handleExhaustion() {
    exhaustion++;
    if (exhaustion >= MAX_EXHAUSTION) {
      firefox.quit();
      firefox.restart();
      log.info(messageSource.getMessage("commander.new.team", new Object[]{exhaustion}, Locale.ENGLISH));
      exhaustion = 0;
    }
  }

  public boolean isLogged() {
    try {
      if (firefox.get().getCurrentUrl().contains("page=ingame")) {
        firefox.get().findElements(By.className("menubutton")).get(MenuEnum.UBERSICHT.getId()).click();
      }
    } catch (NoSuchWindowException | NoSuchSessionException | IndexOutOfBoundsException ex) {
      firefox.restart();
      log.info(messageSource.getMessage("commander.new.team", new Object[]{exhaustion}, Locale.ENGLISH));
    }
    return firefox.get().getCurrentUrl().contains("page=ingame");
  }

  public void logout() {
    try {
      firefox.loading(By.id("bar"));
      firefox.get().findElement(By.id("bar")).findElements(By.tagName("li")).get(7).click();
    } catch (StaleElementReferenceException | NoSuchElementException | TimeoutException ex) {
      log.info("logout:" + ex.getMessage());
    }
  }

}
