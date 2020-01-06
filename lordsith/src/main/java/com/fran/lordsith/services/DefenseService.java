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
import com.fran.lordsith.model.Defense;

@Service
public class DefenseService {

    private static final String UPGRADE = "upgrade";
    private static final String LI_DATA_TECHNOLOGY = "//li[@data-technology=";
    private static final int BASE_RAKETENWERFER = 10000;
    private static final int BASE_LEICHTESLASERGESCHUTZ = 10000;
    private static final int BASE_SCHWERESLASERGESCHUTZ = 1000;
    private static final int BASE_IONENGESCHUZ = 1000;
    private static final int BASE_GAUSSKANONE = 500;
    private static final int BASE_PLASMAWERFER = 250;

    @Autowired
    @Lazy
    private FirefoxClient firefox;
    
    @Autowired
    @Lazy
    private PlanetService planetService;

    @Autowired
    @Lazy
    private MessageSource messageSource;

    Logger log = LoggerFactory.getLogger(DefenseService.class);

    public long calculateNumberOfDefense(int baseNumber, long points) {
	if (points < 50000) {
	    return 0;
	} else if (points < 100000) {
	    return (long) (baseNumber * 0.25);
	} else if (points < 500000) {
	    return (long) (baseNumber * 0.50);
	} else if (points < 1000000) {
	    return (long) (baseNumber * 0.75);
	} else {
	    return (points / 1000000) * baseNumber;
	}
    }

    public void buildDefenses(boolean isMainPlanet) throws InterruptedException {
	firefox.get().findElements(By.className("menubutton")).get(MenuEnum.VERTEIDIGUNG.getId()).click();
	firefox.shortLoading();

	int adjust = isMainPlanet? 1 : 10;
	
	List<Defense> defenses = new ArrayList<>();
	defenses.add(new Defense(BASE_RAKETENWERFER, TechnologyEnum.RAKETENWERFER, false));
	defenses.add(new Defense(BASE_LEICHTESLASERGESCHUTZ, TechnologyEnum.LEICHTESLASERGESCHUTZ, false));
	defenses.add(new Defense(BASE_SCHWERESLASERGESCHUTZ, TechnologyEnum.SCHWERESLASERGESCHUTZ, false));
	defenses.add(new Defense(BASE_IONENGESCHUZ, TechnologyEnum.IONENGESCHUZ, false));
	defenses.add(new Defense(BASE_GAUSSKANONE, TechnologyEnum.GAUSSKANONE, false));
	defenses.add(new Defense(BASE_PLASMAWERFER, TechnologyEnum.PLASMAWERFER, false));
	defenses.add(new Defense(1, TechnologyEnum.KLEINE_SCHILDKUPPEL, true));
	defenses.add(new Defense(1, TechnologyEnum.GROSSE_SCHILDKUPPEL, true));
	defenses.add(new Defense(1, TechnologyEnum.ABFANGRAKETE, true));
	
	defenses.forEach(defense -> defense.setAmountToBuild((calculateNumberOfDefense(defense.getBaseAmount(), planetService.getPoints()) / adjust) - getAmount(defense.getTechnology().getId())));
	Optional<Defense> defenseToBuild = defenses.stream().filter(defense -> isStatusOn(defense.getTechnology().getId()) && defense.getAmountToBuild() > 0).findFirst();
	if(defenseToBuild.isPresent()) {
	    if(defenseToBuild.get().isUnique()) {
		build(defenseToBuild.get().getTechnology());
	    } else {
		build(defenseToBuild.get().getTechnology(), defenseToBuild.get().getAmountToBuild());
	    }
	}
    }

    private long getAmount(int id) {
	return Long.parseLong(firefox.get().findElement(By.xpath(LI_DATA_TECHNOLOGY + id + "]")).findElement(By.className("amount")).getAttribute("data-value"));
    }

    private boolean isStatusOn(int id) {
	boolean isInQueue = firefox.get().findElements(By.className("queuePic")).stream().anyMatch(pic -> pic.getAttribute("alt").trim().endsWith("_" + id));

	WebElement defense = firefox.get().findElement(By.xpath(LI_DATA_TECHNOLOGY + id + "]"));
	return defense.getAttribute("data-status").equals(StatusEnum.ON.getValue()) && !isInQueue;
    }

    private void build(TechnologyEnum defense) throws InterruptedException {
	firefox.get().findElement(By.xpath(LI_DATA_TECHNOLOGY + defense.getId() + "]")).click();
	firefox.shortLoading();

	if (!firefox.get().findElements(By.className(UPGRADE)).isEmpty()) {
	    firefox.jsClick(firefox.get().findElement(By.className(UPGRADE)));
	    firefox.shortLoading();
	}

	log.info(messageSource.getMessage("generic.build", new Object[] { 1, defense.name() }, Locale.ENGLISH));
    }

    private void build(TechnologyEnum defense, long quantity) throws InterruptedException {
	firefox.get().findElement(By.xpath(LI_DATA_TECHNOLOGY + defense.getId() + "]")).click();
	firefox.shortLoading();

	firefox.get().findElement(By.id("build_amount")).sendKeys(String.valueOf(quantity));
	firefox.loading();

	if (!firefox.get().findElements(By.className(UPGRADE)).isEmpty()) {
	    firefox.jsClick(firefox.get().findElement(By.className(UPGRADE)));
	    firefox.loading();
	}

	log.info(messageSource.getMessage("generic.build", new Object[] { quantity, defense.name() }, Locale.ENGLISH));
    }
}
