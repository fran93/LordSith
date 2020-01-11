package com.fran.lordsith.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.fran.lordsith.enums.MenuEnum;
import com.fran.lordsith.enums.StatusEnum;
import com.fran.lordsith.enums.TechnologyEnum;
import com.fran.lordsith.model.Resources;

@Service
public class FleetService {

    private static final double MINIMUM_METALL = 1000000;
    private static final double MINIMUM_DEUTERIUM = 500000;
    private static final double MINIMUM_KRISTALL = 500000;
    private static final double MINIMUM_RESOURCES = 1000000;
    private static final double MINIMUM_TRANSPORT = 500;
    private static final String SEND_FLEET = "sendFleet";
    private static final String CONTINUE_TO_FLEET3 = "continueToFleet3";
    private static final String CONTINUE_TO_FLEET2 = "continueToFleet2";
    private static final String SYSTEM_INPUT = "system_input";
    private static final String LI_DATA_TECHNOLOGY = "//li[@data-technology=";
    private static final int MIN_CARGOS_TO_ATTACK = 10;
    private static final int MIN_FLEET_TO_DEPLOY = 100;
    private static final int MAX_SPY_REPORTS = 10;
    private static final int ATTACK_SYSTEM_RANGE = 150;

    @Autowired
    @Lazy
    private FirefoxClient firefox;

    @Autowired
    @Lazy
    private MessageSource messageSource;

    @Autowired
    @Lazy
    private BuildingService buildingService;

    @Autowired
    @Lazy
    private PlanetService planetService;

    @Autowired
    @Lazy
    private MenuService menuService;

    Logger log = LoggerFactory.getLogger(FleetService.class);

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

    public void sendExpedition() throws InterruptedException {
	if (planetService.hasPoints()) {
	    menuService.openPage(MenuEnum.FLOTTE);

	    sendExpedition2();
	}
    }

    private void sendExpedition2() throws InterruptedException {
	if (isExpeditionAvailable() && isThereAFleet() && isStatusOn(TechnologyEnum.GROSSER_TRANSPORTER.getId())
		&& numberOfShips(TechnologyEnum.GROSSER_TRANSPORTER.getId()) > calculateNumberOfCargos(planetService.getPoints()) / 2) {
	    firefox.get().findElement(By.name("transporterLarge")).sendKeys(String.valueOf(calculateNumberOfCargos(planetService.getPoints())));
	    firefox.loading();

	    if (isStatusOn(TechnologyEnum.PATHFINDER.getId())) {
		firefox.get().findElement(By.name("explorer")).sendKeys("1");
		firefox.loading();
	    }

	    if (isStatusOn(TechnologyEnum.ZERSTORER.getId())) {
		firefox.get().findElement(By.name("destroyer")).sendKeys("1");
		firefox.loading();
	    }

	    if (canContinue(CONTINUE_TO_FLEET2)) {
		weiterWeiter(CONTINUE_TO_FLEET2);

		firefox.get().findElement(By.id("position")).sendKeys("16");
		firefox.loading();

		if (canContinue(CONTINUE_TO_FLEET3)) {
		    weiterWeiter(CONTINUE_TO_FLEET3);

		    if (canContinue(SEND_FLEET)) {
			weiterWeiter(SEND_FLEET);

			log.info(messageSource.getMessage("fleet.expedition", null, Locale.ENGLISH));
		    }
		}
	    }
	}
    }

    public void hunting() throws InterruptedException {
	firefox.longestLoading();
	openMessages();
	processMessages();
    }

    public void scan() throws InterruptedException {
	menuService.openPage(MenuEnum.GALAXIE);

	int planetSystem = getCurrentSystem();
	int reportCount = 0;

	while (getGalaxyFreeSlots() > 0 && reportCount < MAX_SPY_REPORTS && isSondeAvailable()) {
	    if (!(rightSystem > 0 && planetSystem == getCurrentSystem())) {
		recycle();
		reportCount += spy();
	    }

	    if (rightSystem > ATTACK_SYSTEM_RANGE && leftSystem > ATTACK_SYSTEM_RANGE) {
		goToSystem(planetSystem);
		rightSystem = 0;
		leftSystem = 0;
	    } else if (rightSystem > ATTACK_SYSTEM_RANGE) {
		goLeft(planetSystem);
	    } else if (leftSystem > ATTACK_SYSTEM_RANGE) {
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
	    if (!debrisRecyclers.trim().isEmpty()) {
		int requiredRecycles = Integer.parseInt(debrisRecyclers.split(":")[1].trim());
		if (requiredRecycles >= 5 && !debris.findElements(By.tagName("a")).isEmpty()) {
		    debris.findElement(By.tagName("a")).click();
		    firefox.shortLoading();

		    log.info(messageSource.getMessage("fleet.recycle", null, Locale.ENGLISH));
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
	firefox.get().findElement(By.id(SYSTEM_INPUT)).sendKeys(String.valueOf(system));
	firefox.get().findElement(By.id(SYSTEM_INPUT)).submit();
	firefox.loading();
    }

    private int getCurrentSystem() {
	return Integer.parseInt(firefox.get().findElement(By.id(SYSTEM_INPUT)).getAttribute("value"));
    }

    private int spy() throws InterruptedException {
	List<WebElement> inactives = firefox.get().findElements(By.className("inactive_filter"));
	for (WebElement inactive : inactives) {
	    inactive.findElement(By.className("espionage")).click();
	    firefox.loading();

	    log.info(messageSource.getMessage("fleet.spy", new Object[] { getCurrentSystem() }, Locale.ENGLISH));
	}

	return inactives.size();
    }

    private int getGalaxyFreeSlots() throws InterruptedException {
	firefox.shortLoading();
	String[] slotValue = firefox.get().findElement(By.id("slotValue")).getText().split("/");
	return Integer.parseInt(slotValue[1]) - Integer.parseInt(slotValue[0]);
    }

    private boolean isSondeAvailable() throws InterruptedException {
	firefox.shortLoading();
	return Integer.parseInt(firefox.get().findElement(By.id("probeValue")).getText().trim()) > 5;
    }

    private void processMessages() throws InterruptedException {
	List<String> messagesIds = new ArrayList<>();
	firefox.get().findElement(By.id("ui-id-16")).findElements(By.className("msg")).forEach(msg -> messagesIds.add(msg.getAttribute("data-msg-id")));

	for (String id : messagesIds) {
	    processMessage(id);
	}
    }

    private void processMessage(String id) throws InterruptedException {
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
		} else {
		    openMessages();
		    firefox.shortLoading();
		}
	    } else {
		if (necesaryFleet >= MIN_CARGOS_TO_ATTACK) {
		    String coordinates = message.findElement(By.className("msg_title")).getText();
		    log.info(messageSource.getMessage("fleet.farm", new Object[] { coordinates, defenses, necesaryFleet }, Locale.ENGLISH));
		} else {
		    log.info(messageSource.getMessage("fleet.discard", null, Locale.ENGLISH));
		}
		message.findElement(By.className("icon_refuse")).click();
		firefox.loading();
	    }
	}
    }

    private void sendAttack(String id, int necesaryFleet) throws InterruptedException {
	firefox.get().findElement(By.name("transporterSmall")).sendKeys(String.valueOf(necesaryFleet));
	firefox.shortLoading();

	if (canContinue(CONTINUE_TO_FLEET2)) {
	    weiterWeiter(CONTINUE_TO_FLEET2);
	    if (canContinue(CONTINUE_TO_FLEET3)) {
		weiterWeiter(CONTINUE_TO_FLEET3);
		if (canContinue(SEND_FLEET)) {
		    weiterWeiter(SEND_FLEET);

		    log.info(messageSource.getMessage("fleet.attack", null, Locale.ENGLISH));

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

    public void transportResources() throws InterruptedException {
	menuService.openPage(MenuEnum.FLOTTE);

	if (isThereAFleet() && isFleetAvailable() && numberOfShips(TechnologyEnum.KLEINER_TRANSPORTER.getId()) >= MINIMUM_TRANSPORT) {
	    Resources amountToTransport = getAmountToTransport();

	    if (amountToTransport.getMetall() > 0 || amountToTransport.getKristall() > 0 || amountToTransport.getDeuterium() > 0) {
		transportResources(amountToTransport);
	    }
	}
    }

    public void deployFleet() throws InterruptedException {
	menuService.openPage(MenuEnum.FLOTTE);
	int countKreuzer = numberOfShips(TechnologyEnum.KREUZER.getId());
	int countSchlachtKreuzer = numberOfShips(TechnologyEnum.SCHLACHTKREUZER.getId());
	int countReaper = numberOfShips(TechnologyEnum.REAPER.getId());
	int countZerstorer = numberOfShips(TechnologyEnum.ZERSTORER.getId());
	int countPathfinder = numberOfShips(TechnologyEnum.PATHFINDER.getId());

	if (countKreuzer > MIN_FLEET_TO_DEPLOY || countSchlachtKreuzer > MIN_FLEET_TO_DEPLOY || countZerstorer > MIN_FLEET_TO_DEPLOY || countPathfinder > MIN_FLEET_TO_DEPLOY
		|| countReaper > MIN_FLEET_TO_DEPLOY) {
	    selectAllShips(TechnologyEnum.KREUZER.getId());
	    selectAllShips(TechnologyEnum.SCHLACHTKREUZER.getId());
	    selectAllShips(TechnologyEnum.REAPER.getId());
	    firefox.get().findElement(By.name("explorer")).sendKeys(String.valueOf(countPathfinder - 2));
	    firefox.get().findElement(By.name("destroyer")).sendKeys(String.valueOf(countZerstorer - 2));

	    if (canContinue(CONTINUE_TO_FLEET2)) {
		weiterWeiter(CONTINUE_TO_FLEET2);
		if (canContinue(CONTINUE_TO_FLEET3)) {
		    weiterWeiter(CONTINUE_TO_FLEET3);

		    firefox.get().findElement(By.id("missionButton4")).click();
		    firefox.shortLoading();

		    if (canContinue(SEND_FLEET)) {
			weiterWeiter(SEND_FLEET);

			log.info(messageSource.getMessage("fleet.deploy", null, Locale.ENGLISH));
		    }
		}
	    }

	}
    }

    private void selectAllShips(int id) {
	if (isStatusOn(id)) {
	    firefox.get().findElement(By.xpath(LI_DATA_TECHNOLOGY + id + "]")).click();
	}
    }

    private void transportResources(Resources amountToTransport) throws InterruptedException {
	firefox.get().findElement(By.xpath(LI_DATA_TECHNOLOGY + TechnologyEnum.KLEINER_TRANSPORTER.getId() + "]")).findElement(By.className("sprite_small")).click();
	firefox.shortLoading();

	if (canContinue(CONTINUE_TO_FLEET2)) {
	    weiterWeiter(CONTINUE_TO_FLEET2);
	    firefox.get().findElement(By.id("shortcuts")).findElements(By.className("glow")).get(0).click();
	    firefox.shortLoading();
	    Optional<WebElement> dropDown = firefox.get().findElements(By.className("dropdownList")).stream().filter(WebElement::isDisplayed).findFirst();
	    if (dropDown.isPresent()) {
		dropDown.get().findElements(By.tagName("li")).get(1).click();
		firefox.shortLoading();
	    }
	    if (canContinue(CONTINUE_TO_FLEET3)) {
		weiterWeiter(CONTINUE_TO_FLEET3);

		firefox.get().findElement(By.id("missionButton3")).click();
		firefox.shortLoading();

		firefox.get().findElement(By.id("crystal")).sendKeys(String.valueOf(amountToTransport.getKristall()));
		firefox.get().findElement(By.id("deuterium")).sendKeys(String.valueOf(amountToTransport.getDeuterium()));
		firefox.get().findElement(By.id("metal")).sendKeys(String.valueOf(amountToTransport.getMetall()));
		firefox.shortLoading();

		if (canContinue(SEND_FLEET)) {
		    weiterWeiter(SEND_FLEET);

		    log.info(messageSource.getMessage("fleet.transport", null, Locale.ENGLISH));
		}
	    }
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
