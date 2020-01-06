package com.fran.lordsith.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.fran.lordsith.enums.MenuEnum;

@Service
public class PlanetService {

    @Autowired
    @Lazy
    private FirefoxClient firefox;

    @Autowired
    @Lazy
    private MessageSource messageSource;

    private long points;

    private int currentFields;
    private int maxFields;

    Logger log = LoggerFactory.getLogger(PlanetService.class);

    public void extractPoints() throws InterruptedException {
	firefox.loading();
	String scoreContentField = firefox.get().findElement(By.id("scoreContentField")).getText();
	if (!scoreContentField.isEmpty()) {
	    points = Long.parseLong(scoreContentField.split(" ")[0].replaceAll("\\.", ""));
	}
    }

    public void nextPlanet(String id) throws InterruptedException {
	if (!firefox.get().getCurrentUrl().contains("component=overview")) {
	    firefox.get().findElements(By.className("menubutton")).get(MenuEnum.UBERSICHT.getId()).click();
	    firefox.shortLoading();
	}
	firefox.get().findElement(By.id(id)).click();
	firefox.loading();
	extractPoints();
	extractFreeFields();

	log.info(messageSource.getMessage("login.points", new Object[] { points }, Locale.ENGLISH));
	log.info(messageSource.getMessage("planet.fields", new Object[] { id, getFreeFields() }, Locale.ENGLISH));

    }

    public List<String> getPlanetList() {
	List<String> planetIds = new ArrayList<>();
	firefox.get().findElement(By.id("planetList")).findElements(By.className("smallplanet")).forEach(planet -> planetIds.add(planet.getAttribute("id")));

	return planetIds;
    }

    public void extractFreeFields() {
	List<WebElement> spans = firefox.get().findElement(By.id("diameterContentField")).findElements(By.tagName("span"));
	if(!spans.isEmpty()) {
	    currentFields = Integer.valueOf(spans.get(0).getText());
	    maxFields = Integer.valueOf(spans.get(1).getText());
	}
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
