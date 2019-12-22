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
public class HangarService {
	
	@Autowired @Lazy
	private FirefoxClient firefox;
	
	@Autowired @Lazy
	private ExpeditionService expeditionService;
	
	Logger logger = LoggerFactory.getLogger(HangarService.class);

	public void buildExpeditionFleet(long points) {
		firefox.get().findElements(By.className("menubutton")).get(MenuEnum.SCHIFFSWERFT.getId()).click();
		firefox.loading();
		
		int desiredCargos = expeditionService.calculateNumberOfCargos(points);
		int amountCargos = Integer.parseInt(firefox.get().findElement(By.xpath("//li[@data-technology="+TechnologyEnum.GROSSER_TRANSPORTER.getId()+"]")).findElement(By.className("amount")).getAttribute("data-value"));
		int amountPath = Integer.parseInt(firefox.get().findElement(By.xpath("//li[@data-technology="+TechnologyEnum.PATHFINDER.getId()+"]")).findElement(By.className("amount")).getAttribute("data-value"));
		
		if(amountCargos < desiredCargos) {
			WebElement cargoElement = firefox.get().findElement(By.xpath("//li[@data-technology="+TechnologyEnum.GROSSER_TRANSPORTER.getId()+"]"));
			if(cargoElement.getAttribute("data-status").equals(StatusEnum.ON.getValue())) { 
				cargoElement.click();
				firefox.loading();
				
				int amountToBuild = desiredCargos - amountCargos;
				firefox.get().findElement(By.id("build_amount")).sendKeys(String.valueOf(amountToBuild));
				firefox.loading();

				firefox.get().findElement(By.className("upgrade")).click();
				firefox.loading();
				
				logger.info("I order to build " + amountToBuild + " " + TechnologyEnum.GROSSER_TRANSPORTER.name());
			}
		}
		
		if(amountPath == 0) {
			WebElement pathElement = firefox.get().findElement(By.xpath("//li[@data-technology="+TechnologyEnum.PATHFINDER.getId()+"]"));
			if(pathElement.getAttribute("data-status").equals(StatusEnum.ON.getValue())) { 
				pathElement.click();
				firefox.loading();

				firefox.get().findElement(By.id("build_amount")).sendKeys("1");
				firefox.loading();

				firefox.get().findElement(By.className("upgrade")).click();
				firefox.loading();
			}
		}
		
	}
	
}
