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
public class PlanetService {

    @Autowired
    @Lazy
    FirefoxClient firefox;

    @Autowired
    @Lazy
    MessageSource messageSource;

    @Autowired
    @Lazy
    MenuService menuService;

    private long points;

    private int currentFields;
    private int maxFields;

    private final static int MIN_DEVELOPMENT = 150;

    Logger log = LoggerFactory.getLogger(PlanetService.class);

    public void extractPoints() throws InterruptedException {
        firefox.loading();
        String scoreContentField = firefox.get().findElement(By.id("scoreContentField")).getText();
        if (!scoreContentField.isEmpty()) {
            points = Long.parseLong(scoreContentField.split(" ")[0].replaceAll("\\.", ""));
        }
    }

    public void nextPlanet(int index) throws InterruptedException {
        menuService.openPage(MenuEnum.UBERSICHT);
        WebElement planet = getPlanetByIndex(index);
        String name = planet.findElement(By.className("planet-name")).getText();
        planet.click();
        firefox.loading();
        extractPoints();
        extractFreeFields();
        log.info(messageSource.getMessage("login.points", new Object[]{points}, Locale.ENGLISH));
        log.info(messageSource.getMessage("planet.fields", new Object[]{name, getFreeFields()}, Locale.ENGLISH));

    }

    public WebElement getPlanetByIndex(int index) throws InterruptedException {
        return getPlanetList().get(index);
    }

    public List<WebElement> getPlanetList() throws InterruptedException {
        menuService.openPage(MenuEnum.UBERSICHT);
        firefox.shortLoading();

        return firefox.get().findElements(By.className("smallplanet"));
    }

    public int countPlanets() throws InterruptedException {
        return getPlanetList().size();
    }

    public void extractFreeFields() {
        List<WebElement> spans = firefox.get().findElement(By.id("diameterContentField")).findElements(By.tagName("span"));
        if (!spans.isEmpty()) {
            currentFields = Integer.valueOf(spans.get(0).getText());
            maxFields = Integer.valueOf(spans.get(1).getText());
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

    public boolean hasFields() {
        return currentFields > 0 && maxFields > 0;
    }

    public long getPoints() {
        return points;
    }
}
