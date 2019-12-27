package com.fran.lordsith.services;

import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.fran.lordsith.enums.MenuEnum;
import com.fran.lordsith.enums.StatusEnum;
import com.fran.lordsith.enums.TechnologyEnum;

@Service
public class HangarService {
	
	@Autowired @Lazy
	private FirefoxClient firefox;
	
	@Autowired @Lazy
	private ManageFleetService expeditionService;
	
	private static final String LI_DATA_TECHNOLOGY = "//li[@data-technology=";
	
	Logger log = LoggerFactory.getLogger(HangarService.class);

	public void buildExpeditionFleet(long points) throws InterruptedException {
		firefox.get().findElements(By.className("menubutton")).get(MenuEnum.SCHIFFSWERFT.getId()).click();
		firefox.shortLoading();
		
		int desiredShips = expeditionService.calculateNumberOfCargos(points)/2;
		long amountCargos = getAmount(TechnologyEnum.GROSSER_TRANSPORTER.getId());
		long amountPath = getAmount(TechnologyEnum.PATHFINDER.getId());
		long amountZerstorer = getAmount(TechnologyEnum.ZERSTORER.getId());
		
		if(isStatusOn(TechnologyEnum.GROSSER_TRANSPORTER.getId()) && amountCargos < desiredShips) {
			build(TechnologyEnum.GROSSER_TRANSPORTER, desiredShips - amountCargos);
		}
		
		if(isStatusOn(TechnologyEnum.PATHFINDER.getId()) && amountPath == 0) {
			build(TechnologyEnum.PATHFINDER, 1);
		}
		
		if(isStatusOn(TechnologyEnum.ZERSTORER.getId()) && amountZerstorer == 0) {
			build(TechnologyEnum.ZERSTORER, 1);
		}
	}
	
	public void buildPathfinderFleet(long points) throws InterruptedException {
		int desiredShips = expeditionService.calculateNumberOfCargos(points)/10;
		long amountPath = getAmount(TechnologyEnum.PATHFINDER.getId());
		
		if(isStatusOn(TechnologyEnum.PATHFINDER.getId()) && amountPath < desiredShips) {
			build(TechnologyEnum.PATHFINDER, desiredShips - amountPath);
		}
	}
	
	private long getAmount(int id) {
		return Long.parseLong(firefox.get().findElement(By.xpath(LI_DATA_TECHNOLOGY+id+"]")).findElement(By.className("amount")).getAttribute("data-value"));
	}
	
	private boolean isStatusOn(int id) {
		return firefox.get().findElement(By.xpath(LI_DATA_TECHNOLOGY+id+"]")).getAttribute("data-status").equals(StatusEnum.ON.getValue());
	}
	
	private void build(TechnologyEnum tech, long quantity) throws InterruptedException {
		firefox.get().findElement(By.xpath(LI_DATA_TECHNOLOGY+tech.getId()+"]")).click();
		firefox.shortLoading();
		
		firefox.get().findElement(By.id("build_amount")).sendKeys(String.valueOf(quantity));
		firefox.loading();
		
		firefox.get().findElement(By.className("upgrade")).click();
		firefox.shortLoading();
		
		log.info("I order to build " + quantity + " " + tech.name());
	}
	
}
