package com.fran.lordsith.services;

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
public class DefenseService {
	
	private static final String LI_DATA_TECHNOLOGY = "//li[@data-technology=";
	private static final int BASE_RAKETENWERFER = 1920;
	private static final int BASE_LEICHTESLASERGESCHUTZ = 1920;
	private static final int BASE_SCHWERESLASERGESCHUTZ = 192;
	private static final int BASE_IONENGESCHUZ = 192;
	private static final int BASE_GAUSSKANONE = 96;
	private static final int BASE_PLASMAWERFER = 48;
	
	@Autowired @Lazy
	private FirefoxClient firefox;
	
	Logger log = LoggerFactory.getLogger(DefenseService.class);

	public long calculateNumberOfDefense(int baseNumber, long points) {
		if(points < 50000) {
			return 0;
		} else if(points < 500000) {
			return (long) (baseNumber * 0.25);
		} else if(points < 1000000) {
			return (long) (baseNumber * 0.50);
		} else {
			return (points/1000000) * baseNumber;
		}
	}
	
	public void buildDefenses(int position, long points) throws InterruptedException {	
		firefox.get().findElements(By.className("menubutton")).get(MenuEnum.VERTEIDIGUNG.getId()).click();
		firefox.shortLoading();
		
		int adjust = position == 0 ? 1 : 10;
		long desiredRaketenwerfer = calculateNumberOfDefense(BASE_RAKETENWERFER, points)/adjust;
		long desiredLeichtesLaser = calculateNumberOfDefense(BASE_LEICHTESLASERGESCHUTZ, points)/adjust;
		long desiredSchweresLaser = calculateNumberOfDefense(BASE_SCHWERESLASERGESCHUTZ, points)/adjust;
		long desiredIonenGeschuz = calculateNumberOfDefense(BASE_IONENGESCHUZ, points)/adjust;
		long desiredGaussKanone = calculateNumberOfDefense(BASE_GAUSSKANONE, points)/adjust;
		long desiredPlasmaWerfer = calculateNumberOfDefense(BASE_PLASMAWERFER, points)/adjust;
		
		long amountRaketenwerfer = getAmount(TechnologyEnum.RAKETENWERFER.getId());
		long amountLeichtesLaser  = getAmount(TechnologyEnum.LEICHTESLASERGESCHUTZ.getId());
		long amountSchweresLaser  = getAmount(TechnologyEnum.SCHWERESLASERGESCHUTZ.getId());
		long amountIonenGeschuz= getAmount(TechnologyEnum.IONENGESCHUZ.getId());
		long amountGaussKanone = getAmount(TechnologyEnum.GAUSSKANONE.getId());
		long amountPlasmaWerfer = getAmount(TechnologyEnum.PLASMAWERFER.getId());
		
		if(isStatusOn(TechnologyEnum.RAKETENWERFER.getId()) && desiredRaketenwerfer > amountRaketenwerfer) {
		    build(TechnologyEnum.RAKETENWERFER, desiredRaketenwerfer - amountRaketenwerfer);
		} else if(isStatusOn(TechnologyEnum.LEICHTESLASERGESCHUTZ.getId()) && desiredLeichtesLaser > amountLeichtesLaser) {
		    build(TechnologyEnum.LEICHTESLASERGESCHUTZ, desiredLeichtesLaser - amountLeichtesLaser);
		} else if(isStatusOn(TechnologyEnum.SCHWERESLASERGESCHUTZ.getId()) && desiredSchweresLaser > amountSchweresLaser) {
		    build(TechnologyEnum.SCHWERESLASERGESCHUTZ, desiredSchweresLaser - amountSchweresLaser);
		} else if(isStatusOn(TechnologyEnum.IONENGESCHUZ.getId()) && desiredIonenGeschuz > amountIonenGeschuz) {
		    build(TechnologyEnum.IONENGESCHUZ, desiredIonenGeschuz - amountIonenGeschuz);
		} else if(isStatusOn(TechnologyEnum.GAUSSKANONE.getId()) && desiredGaussKanone > amountGaussKanone) {
		    build(TechnologyEnum.GAUSSKANONE, desiredGaussKanone - amountGaussKanone);
		} else if(isStatusOn(TechnologyEnum.PLASMAWERFER.getId()) && desiredPlasmaWerfer > amountPlasmaWerfer) {
		    build(TechnologyEnum.PLASMAWERFER, desiredPlasmaWerfer - amountPlasmaWerfer);
		} else if(isStatusOn(TechnologyEnum.KLEINE_SCHILDKUPPEL.getId())) {
		    build(TechnologyEnum.KLEINE_SCHILDKUPPEL);
		} else if(isStatusOn(TechnologyEnum.GROSSE_SCHILDKUPPEL.getId())) {
		    build(TechnologyEnum.GROSSE_SCHILDKUPPEL);
		} else if(isStatusOn(TechnologyEnum.ABFANGRAKETE.getId())) {
		    build(TechnologyEnum.ABFANGRAKETE, 5);
		}
		
	}
	
	private long getAmount(int id) {
		return Long.parseLong(firefox.get().findElement(By.xpath(LI_DATA_TECHNOLOGY+id+"]")).findElement(By.className("amount")).getAttribute("data-value"));
	}
	
	private boolean isStatusOn(int id) {
		WebElement defense  = firefox.get().findElement(By.xpath(LI_DATA_TECHNOLOGY+id+"]"));
		return defense.getAttribute("data-status").equals(StatusEnum.ON.getValue()) && !defense.findElements(By.className("targetamount")).isEmpty();
	}
	
	private void build(TechnologyEnum defense) throws InterruptedException {
		firefox.get().findElement(By.xpath(LI_DATA_TECHNOLOGY+defense.getId()+"]")).click();
		firefox.shortLoading();

		firefox.jsClick(firefox.get().findElement(By.className("upgrade")));
		firefox.shortLoading();
		
		log.info("I order to build " + defense.name());
	}
	
	private void build(TechnologyEnum defense, long quantity) throws InterruptedException {
		firefox.get().findElement(By.xpath(LI_DATA_TECHNOLOGY+defense.getId()+"]")).click();
		firefox.shortLoading();
		
		firefox.get().findElement(By.id("build_amount")).sendKeys(String.valueOf(quantity));
		firefox.loading();
		
		firefox.jsClick(firefox.get().findElement(By.className("upgrade")));
		firefox.shortLoading();
		
		log.info("I order to build " + quantity + " " + defense.name());
	}
}
