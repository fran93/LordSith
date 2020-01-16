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

    @Autowired
    @Lazy
    TechnologyService technologyService;

    private static final String LI_DATA_TECHNOLOGY = "//li[@data-technology=";

    Logger log = LoggerFactory.getLogger(HangarService.class);

    public void buildExpeditionFleet() throws InterruptedException {
        menuService.openPage(MenuEnum.SCHIFFSWERFT);

        int desiredShips = expeditionService.calculateNumberOfCargos(planetService.getPoints()) / 2;
        long amountCargos = getAmount(TechnologyEnum.GROSSER_TRANSPORTER.getId());
		long amountPath = getAmount(TechnologyEnum.PATHFINDER.getId());
		long amountZerstorer = getAmount(TechnologyEnum.ZERSTORER.getId());

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

    public void buildTransportFleet() throws InterruptedException {
        menuService.openPage(MenuEnum.SCHIFFSWERFT);
        int desiredShips = expeditionService.calculateNumberOfCargos(planetService.getPoints()) / 2;
        long amountCargos = getAmount(TechnologyEnum.KLEINER_TRANSPORTER.getId());

        if (isStatusOn(TechnologyEnum.KLEINER_TRANSPORTER.getId()) && amountCargos < desiredShips) {
            build(TechnologyEnum.KLEINER_TRANSPORTER, desiredShips - amountCargos);
        }
    }

    public void buildSolarSatelliteFleet() throws InterruptedException {
        menuService.openPage(MenuEnum.SCHIFFSWERFT);

        if (isStatusOn(TechnologyEnum.SOLARSATELLIT.getId())) {
            build(TechnologyEnum.SOLARSATELLIT, 100);
        }
    }

    public void buildDeathStar(boolean isMainPlanet) throws InterruptedException {
		menuService.openPage(MenuEnum.SCHIFFSWERFT);

		int desiredShips = isMainPlanet ? expeditionService.calculateNumberOfCargos(planetService.getPoints()) / 1000000 : 1;
		long amountStars = getAmount(TechnologyEnum.TODESSTERN.getId());

		if (isStatusOn(TechnologyEnum.TODESSTERN.getId()) && amountStars < desiredShips) {
			build(TechnologyEnum.TODESSTERN, desiredShips - amountStars);
		}
	}

	private int getAmount(int id) {
        Optional<WebElement> theShip = technologyService.getTechnologyById(id);
        return theShip.map(webElement -> Integer.parseInt(webElement.findElement(By.className("amount")).getAttribute("data-value"))).orElse(0);
    }

	private boolean isStatusOn(int id) throws InterruptedException {
        firefox.shortLoading();
        boolean isInQueue = firefox.get().findElements(By.className("queuePic")).stream().anyMatch(pic -> pic.getAttribute("alt").trim().endsWith("_" + id));
        Optional<WebElement> defense = technologyService.getTechnologyById(id);

        return defense.isPresent() && (defense.get().getAttribute("data-status").equals(StatusEnum.ON.getValue()) && !isInQueue);
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
