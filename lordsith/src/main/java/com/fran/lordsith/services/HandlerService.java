package com.fran.lordsith.services;

import com.fran.lordsith.enums.MenuEnum;
import org.openqa.selenium.By;
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

    Logger log = LoggerFactory.getLogger(HandlerService.class);

    public void scrapFleet() throws InterruptedException {
        menuService.openPage(MenuEnum.HANDLER);

        firefox.get().findElement(By.id("js_traderScrap")).click();
        firefox.loading();

        if (firefox.get().getCurrentUrl().contains("page=traderScrap")) {
            clickScrap("button204");
            clickScrap("button205");
            clickScrap("button207");
            clickScrap("button211");
            firefox.shortLoading();

            if (planetService.hasPoints()) {
                firefox.get().findElement(By.className("forward")).click();
                firefox.shortLoading();
                String rawAmount = firefox.get().findElement(By.id("button203")).findElement(By.className("amount")).getText().replaceAll("\\.", "");
                if (!rawAmount.trim().isEmpty()) {
                    long current = Long.parseLong(rawAmount);
                    long base = fleetService.calculateNumberOfCargos(planetService.getPoints());
                    long desired = current - (base + base / 2);

                    if (desired > 0) {
                        firefox.get().findElement(By.id("ship_203")).sendKeys(String.valueOf(desired));
                        firefox.shortLoading();
                    }
                }
            }

            if (!firefox.get().findElement(By.id("js_scrapScrapIT")).getAttribute(CLASS).contains("disabled")) {
                firefox.get().findElement(By.id("js_scrapScrapIT")).click();
                firefox.loading();

                firefox.get().findElement(By.className("yes")).click();
                firefox.shortLoading();

                log.info(messageSource.getMessage("handler.scrap", null, Locale.ENGLISH));
            }
        }
    }

    private void clickScrap(String id) {
        if (firefox.get().findElement(By.id(id)).getAttribute(CLASS).endsWith("on")) {
            firefox.get().findElement(By.id(id)).click();
        }
    }

    public void importExport() throws InterruptedException {
        firefox.shortLoading();
        if (!firefox.get().findElements(By.className("back_to_overview")).isEmpty()) {
            firefox.get().findElement(By.className("back_to_overview")).click();
            firefox.loading();
            firefox.get().findElement(By.id("js_traderImportExport")).click();
            firefox.loading();

            if (!firefox.get().findElement(By.className("got_item_text")).isDisplayed()) {
                firefox.get().findElement(By.className("js_sliderMetalMax")).click();
                firefox.shortLoading();
                if (!firefox.get().findElement(By.className("pay")).getAttribute(CLASS).contains("disabled")) {
                    firefox.get().findElement(By.className("pay")).click();
                    firefox.shortLoading();

                    firefox.get().findElement(By.className("take")).click();
                    firefox.shortLoading();
                }
            }
        }
    }
}
