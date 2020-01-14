package com.fran.lordsith.services;

import com.fran.lordsith.enums.MenuEnum;
import com.fran.lordsith.enums.StatusEnum;
import com.fran.lordsith.enums.TechnologyEnum;
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
import java.util.Optional;

@Service
public class HangarService {

	@Autowired
	@Lazy
	FirefoxClient firefox;

	@Autowired
	@Lazy
	FleetService expeditionService;

	@Autowired
	@Lazy
	PlanetService planetService;

	@Autowired
	@Lazy
	MessageSource messageSource;

	@Autowired
	@Lazy
	MenuService menuService;

	private static final String LI_DATA_TECHNOLOGY = "//li[@data-technology=";

	Logger log = LoggerFactory.getLogger(HangarService.class);

	public void buildExpeditionFleet() throws InterruptedException {
		menuService.openPage(MenuEnum.SCHIFFSWERFT);

		List<WebElement> ships = getListOfShips();
		int desiredShips = expeditionService.calculateNumberOfCargos(planetService.getPoints()) / 2;
		long amountCargos = getAmount(TechnologyEnum.GROSSER_TRANSPORTER.getId(), ships);
		long amountPath = getAmount(TechnologyEnum.PATHFINDER.getId(), ships);
		long amountZerstorer = getAmount(TechnologyEnum.ZERSTORER.getId(), ships);

		if (isStatusOn(TechnologyEnum.GROSSER_TRANSPORTER.getId()) && amountCargos < desiredShips) {
			build(TechnologyEnum.GROSSER_TRANSPORTER, desiredShips - amountCargos);
		}

		if (isStatusOn(TechnologyEnum.PATHFINDER.getId()) && amountPath == 0) {
			build(TechnologyEnum.PATHFINDER, 1);
		}

		if (isStatusOn(TechnologyEnum.ZERSTORER.getId()) && amountZerstorer == 0) {
			build(TechnologyEnum.ZERSTORER, 1);
		}
	}

    public void buildSolarSatelliteFleet() throws InterruptedException {
		menuService.openPage(MenuEnum.SCHIFFSWERFT);

		if (isStatusOn(TechnologyEnum.SOLARSATELLIT.getId())) {
			build(TechnologyEnum.SOLARSATELLIT, 100);
		}
	}

	public void buildDeathStar(boolean isMainPlanet) throws InterruptedException {
		int desiredShips = isMainPlanet ? 1 : expeditionService.calculateNumberOfCargos(planetService.getPoints()) / 1000000;
		long amountStars = getAmount(TechnologyEnum.TODESSTERN.getId(), getListOfShips());

		if (isStatusOn(TechnologyEnum.TODESSTERN.getId()) && amountStars < desiredShips) {
			build(TechnologyEnum.TODESSTERN, desiredShips - amountStars);
		}
	}

	public void buildHuntingFleet() throws InterruptedException {
		long amountSpionageSonde = getAmount(TechnologyEnum.SPIONAGESONDE.getId(), getListOfShips());
		int desiredShips = 25;

		if (isStatusOn(TechnologyEnum.SPIONAGESONDE.getId()) && amountSpionageSonde < desiredShips) {
			build(TechnologyEnum.SPIONAGESONDE, desiredShips - amountSpionageSonde);
		}

		long amountKleiner = getAmount(TechnologyEnum.KLEINER_TRANSPORTER.getId(), getListOfShips());
		desiredShips = 50;

		if (isStatusOn(TechnologyEnum.KLEINER_TRANSPORTER.getId()) && amountKleiner < desiredShips) {
			build(TechnologyEnum.KLEINER_TRANSPORTER, desiredShips - amountKleiner);
		}
	}

	private int getAmount(int id, List<WebElement> ships) {
		Optional<WebElement> theShip = ships.stream().filter(ship -> ship.getAttribute("data-data-technology").equals(String.valueOf(id))).findFirst();
		return theShip.map(webElement -> Integer.parseInt(webElement.findElement(By.className("amount")).getAttribute("data-value"))).orElse(0);
	}

	private List<WebElement> getListOfShips() {
		return firefox.get().findElements(By.className("technology"));
	}

	private boolean isStatusOn(int id) {
		boolean isInQueue = firefox.get().findElements(By.className("queuePic")).stream().anyMatch(pic -> pic.getAttribute("alt").trim().endsWith("_" + id));

		WebElement defense = firefox.get().findElement(By.xpath(LI_DATA_TECHNOLOGY + id + "]"));
		return defense.getAttribute("data-status").equals(StatusEnum.ON.getValue()) && !isInQueue;
	}

	private void build(TechnologyEnum tech, long quantity) throws InterruptedException {
		firefox.get().findElement(By.xpath(LI_DATA_TECHNOLOGY + tech.getId() + "]")).click();
		firefox.shortLoading();

		firefox.get().findElement(By.id("build_amount")).sendKeys(String.valueOf(quantity));
		firefox.loading();

		if (!firefox.get().findElements(By.className("upgrade")).isEmpty()) {
			firefox.jsClick(firefox.get().findElement(By.className("upgrade")));
			firefox.loading();
		}

		log.info(messageSource.getMessage("generic.build", new Object[]{quantity, tech.name()}, Locale.ENGLISH));
	}

}
