package com.fran.lordsith.services;

import com.fran.lordsith.enums.MenuEnum;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MenuService {

    @Autowired
    @Lazy
    FirefoxClient firefox;

    Logger log = LoggerFactory.getLogger(MenuService.class);

    public void openPage(MenuEnum menu) {
      try {
        if (!isOnPage(menu)) {
          firefox.loading(By.className("menubutton"));
          List<WebElement> buttons = firefox.get().findElements(By.className("menubutton"));
          if (!buttons.isEmpty()) {
            buttons.get(menu.getId()).click();
          }
        }
      } catch (StaleElementReferenceException | TimeoutException ex) {
        log.info("openPage", ex);
      }
    }

    public boolean isOnPage(MenuEnum menu) {
        return firefox.get().getCurrentUrl().trim().contains(menu.getComponent());
    }
}
