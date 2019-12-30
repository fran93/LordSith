package com.fran.lordsith.services;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.fran.lordsith.enums.MenuEnum;
import com.fran.lordsith.enums.StatusEnum;
import com.fran.lordsith.enums.TechnologyEnum;

@Service
public class FleetService {

    @Autowired
    @Lazy
    private FirefoxClient firefox;

    Logger log = LoggerFactory.getLogger(FleetService.class);

    private static final String LI_DATA_TECHNOLOGY = "//li[@data-technology=";
    private static final int MIN_CARGOS_TO_ATTACK = 10;
    private static final int MAX_SPY_REPORTS = 10;
    private int leftSystem;
    private int rightSystem;

    public int calculateNumberOfCargos(long points) {
	if (points < 100000) {
	    return 300;
	} else if (points < 1000000) {
	    return 700;
	} else if (points < 5000000) {
	    return 1000;
	} else if (points < 25000000) {
	    return 1400;
	} else if (points < 50000000) {
	    return 1800;
	} else if (points < 75000000) {
	    return 2100;
	} else if (points < 100000000) {
	    return 2400;
	} else {
	    return 2900;
	}
    }

    public void sendExpedition(long points) throws InterruptedException {
	firefox.get().findElements(By.className("menubutton")).get(MenuEnum.FLOTTE.getId()).click();
	firefox.shortLoading();

	if (isExpeditionAvailable() && isThereAFleet() && isStatusOn(TechnologyEnum.GROSSER_TRANSPORTER.getId())
		&& numberOfShips(TechnologyEnum.GROSSER_TRANSPORTER.getId()) > calculateNumberOfCargos(points) / 2) {
	    firefox.get().findElement(By.name("transporterLarge"))
		    .sendKeys(String.valueOf(calculateNumberOfCargos(points)));
	    firefox.loading();

	    if (isStatusOn(TechnologyEnum.PATHFINDER.getId())) {
		firefox.get().findElement(By.name("explorer")).sendKeys("1");
		firefox.loading();
	    }

	    if (isStatusOn(TechnologyEnum.ZERSTORER.getId())) {
		firefox.get().findElement(By.name("destroyer")).sendKeys("1");
		firefox.loading();
	    }

	    if (canContinue("continueToFleet2")) {
		weiterWeiter("continueToFleet2");

		firefox.get().findElement(By.id("position")).sendKeys("16");
		firefox.loading();

		if (canContinue("continueToFleet3")) {
		    weiterWeiter("continueToFleet3");

		    if (canContinue("sendFleet")) {
			weiterWeiter("sendFleet");

			log.info("I order to send an expedition!");
		    }
		}
	    }
	}
    }

    public void hunting() throws InterruptedException {
	openMessages();
	processMessages();
    }

    public void scan() throws InterruptedException {
	firefox.get().findElements(By.className("menubutton")).get(MenuEnum.GALAXIE.getId()).click();
	firefox.shortLoading();

	int planetSystem = getCurrentSystem();
	int reportCount = 0;

	while (getGalaxyFreeSlots() > 0 && reportCount < MAX_SPY_REPORTS) {
	    if (!(rightSystem > 0 && planetSystem == getCurrentSystem())) {
		recycle();
		reportCount += spy();
	    }

	    if (rightSystem > 100 && leftSystem > 100) {
		goToSystem(planetSystem);
		rightSystem = 0;
		leftSystem = 0;
	    } else if (rightSystem > 100) {
		goLeft(planetSystem);
	    } else if (leftSystem > 100) {
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

    private void recycle() throws InterruptedException {
	List<WebElement> expeditionSlotBox = firefox.get().findElements(By.className("expeditionDebrisSlotBox"));
	if (!expeditionSlotBox.isEmpty()) {
	    firefox.mouseOver(expeditionSlotBox.get(0).findElement(By.className("js_bday_debris")));
	    firefox.shortLoading();
	    WebElement debris = firefox.get().findElement(By.id("debris16"));
	    String debrisRecyclers = debris.findElement(By.className("debris-recyclers")).getText();
	    if(!debrisRecyclers.trim().isEmpty()) {
    	    	int requiredRecycles = Integer.parseInt(debrisRecyclers.split(":")[1].trim());
    	    	if (requiredRecycles >= 5 && !debris.findElements(By.tagName("a")).isEmpty()) {
    	    	    debris.findElement(By.tagName("a")).click();
    	    	    log.info("I order to recycle that debris field");
    	    	    firefox.shortLoading();
    	    	}
	    }
	}
    }

    private void goRight(int planetSystem) throws InterruptedException {
	rightSystem++;
	goToSystem(planetSystem + rightSystem);
    }

    private void goLeft(int planetSystem) throws InterruptedException {
	leftSystem++;
	goToSystem(planetSystem - leftSystem);
    }

    private void goToSystem(int system) throws InterruptedException {
	if (system > 499) {
	    system -= 499;
	}
	firefox.get().findElement(By.id("system_input")).sendKeys(String.valueOf(system));
	firefox.get().findElement(By.id("system_input")).submit();
	firefox.loading();
    }

    private int getCurrentSystem() {
	return Integer.parseInt(firefox.get().findElement(By.id("system_input")).getAttribute("value"));
    }

    private int spy() throws InterruptedException {
	List<WebElement> inactives = firefox.get().findElements(By.className("inactive_filter"));
	for (WebElement inactive : inactives) {
	    inactive.findElement(By.className("espionage")).click();
	    log.info("I order to spy on system " + getCurrentSystem());
	    firefox.loading();
	}

	return inactives.size();
    }

    private int getGalaxyFreeSlots() {
	String[] slotValue = firefox.get().findElement(By.id("slotValue")).getText().split("/");
	return Integer.parseInt(slotValue[1]) - Integer.parseInt(slotValue[0]);
    }

    private void processMessages() throws InterruptedException {
	List<String> messagesIds = new ArrayList<>();
	firefox.get().findElement(By.id("ui-id-16")).findElements(By.className("msg"))
		.forEach(msg -> messagesIds.add(msg.getAttribute("data-msg-id")));

	for (String id : messagesIds) {
	    WebElement message = firefox.get().findElement(By.xpath("//li[@data-msg-id=" + id + "]"));
	    List<WebElement> rows = message.findElements(By.className("compacting"));
	    if (rows.size() >= 5) {
		int necesaryFleet = getNecesaryFleet(rows);
		String defenses = rows.get(4).findElement(By.className("tooltipRight")).getText().split(":")[1];

		if (defenses.trim().equals("0") && necesaryFleet >= MIN_CARGOS_TO_ATTACK) {
		    message.findElement(By.className("icon_attack")).click();
		    firefox.loading();

		    if (isFleetAvailable()) {
			sendAttack(id, necesaryFleet);
		    }
		} else {
		    message.findElement(By.className("icon_refuse")).click();
		    log.info("I order to discard that objetive!");
		    firefox.loading();
		}
	    }
	}
    }

    private void sendAttack(String id, int necesaryFleet) throws InterruptedException {
	firefox.get().findElement(By.name("transporterSmall")).sendKeys(String.valueOf(necesaryFleet));
	firefox.shortLoading();

	if (canContinue("continueToFleet2")) {
	    weiterWeiter("continueToFleet2");
	    if (canContinue("continueToFleet3")) {
		weiterWeiter("continueToFleet3");
		if (canContinue("sendFleet")) {
		    weiterWeiter("sendFleet");

		    log.info("I order to send an attack!");

		    openMessages();

		    firefox.get().findElement(By.xpath("//li[@data-msg-id=" + id + "]")).findElement(By.className("icon_refuse")).click();
		    firefox.loading();
		}
	    }
	}
    }

    private int getNecesaryFleet(List<WebElement> rows) {
	StringBuilder title = new StringBuilder(rows.get(2).findElement(By.className("tooltipClose")).getAttribute("title"));
	title.delete(0, title.indexOf("am202="));
	title.delete(0, 6);
	title.delete(title.indexOf("\""), title.length());
	return Integer.parseInt(title.toString());
    }

    private void openMessages() throws InterruptedException {
	firefox.get().findElement(By.className("messages")).click();
	firefox.shortLoading();
    }

    private boolean canContinue(String id) {
	return firefox.get().findElement(By.id(id)).getAttribute("class").trim().endsWith("on");
    }

    private void weiterWeiter(String id) throws InterruptedException {
	firefox.jsClick(firefox.get().findElement(By.id(id)));
	firefox.loading();
    }

    private boolean isThereAFleet() {
	return firefox.get().findElements(By.className("icon_warning")).isEmpty();
    }

    private boolean isStatusOn(int id) {
	return firefox.get().findElement(By.xpath(LI_DATA_TECHNOLOGY + id + "]")).getAttribute("data-status").equals(StatusEnum.ON.getValue());
    }

    private int numberOfShips(int id) {
	return Integer.parseInt(firefox.get().findElement(By.xpath(LI_DATA_TECHNOLOGY + id + "]")).findElement(By.className("amount")).getAttribute("data-value"));
    }

    private boolean isExpeditionAvailable() {
	List<WebElement> slotElements = firefox.get().findElement(By.id("slots")).findElements(By.className("fleft"));
	String rawExpe = slotElements.get(1).getText();
	String splitedExpeditions = rawExpe.split(":")[1].trim();

	int currentExpeditions = Integer.parseInt(splitedExpeditions.split("/")[0]);
	int maxExpeditions = Integer.parseInt(splitedExpeditions.split("/")[1]);

	return isFleetAvailable() && currentExpeditions < maxExpeditions;
    }

    private boolean isFleetAvailable() {
	List<WebElement> slotElements = firefox.get().findElement(By.id("slots")).findElements(By.className("fleft"));
	String rawSlots = slotElements.get(0).getText();
	String splitedSlots = rawSlots.split(":")[1].trim();

	int currentSlots = Integer.parseInt(splitedSlots.split("/")[0]);
	int maxSlots = Integer.parseInt(splitedSlots.split("/")[1]);

	return currentSlots < maxSlots;
    }

}
