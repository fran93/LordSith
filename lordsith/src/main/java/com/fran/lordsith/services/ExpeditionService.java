package com.fran.lordsith.services;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.fran.lordsith.enums.MenuEnum;
import com.fran.lordsith.enums.TechnologyEnum;

@Service
public class ExpeditionService {

	@Autowired @Lazy
	private FirefoxClient firefox;
	
	Logger logger = LoggerFactory.getLogger(ExpeditionService.class);
	
	public int calculateNumberOfCargos(long points) {
		if(points < 100000) {
			return 42;
		} else if(points < 1000000) {
			return 125; 
		} else if(points < 5000000) {
			return 150; 
		} else if(points < 25000000) {
			return 200; 
		} else if(points < 50000000) {
			return 250; 
		} else if(points < 75000000) {
			return 300; 
		} else if(points < 100000000) {
			return 350; 
		} else {
			return 400;
		}
	}
	
	public void sendExpedition(long points) {
		firefox.get().findElements(By.className("menubutton")).get(MenuEnum.FLOTTE.getId()).click();
		firefox.loading();
				
		if(isExpeditionAvailable() && firefox.get().findElements(By.className("icon_warning")).isEmpty() 
			&& firefox.get().findElement(By.xpath("//li[@data-technology="+TechnologyEnum.GROSSER_TRANSPORTER.getId()+"]")).getAttribute("data-status").equals("on")) {
			firefox.get().findElement(By.name("transporterLarge")).sendKeys(String.valueOf(calculateNumberOfCargos(points)));
			firefox.loading();
			
			if(firefox.get().findElement(By.xpath("//li[@data-technology="+TechnologyEnum.SPIONAGESONDE.getId()+"]")).getAttribute("data-status").equals("on")) {
				firefox.get().findElement(By.name("espionageProbe")).sendKeys("1");
				firefox.loading();
			}
			if(firefox.get().findElement(By.xpath("//li[@data-technology="+TechnologyEnum.PATHFINDER.getId()+"]")).getAttribute("data-status").equals("on")) {
				firefox.get().findElement(By.name("explorer")).sendKeys("1");
				firefox.loading();
			}
			
			if(firefox.get().findElement(By.id("continueToFleet2")).getAttribute("class").trim().endsWith("on")) {
				firefox.get().findElement(By.id("continueToFleet2")).click();
				firefox.loading();
				
				firefox.get().findElement(By.id("position")).sendKeys("16");
				firefox.loading();
				
				if(firefox.get().findElement(By.id("continueToFleet3")).getAttribute("class").trim().endsWith("on")) {
					firefox.get().findElement(By.id("continueToFleet3")).click();
					firefox.loading();
					
					firefox.executeJavascript("$('#missionButton15').click();");
					firefox.loading();
					
					if(firefox.get().findElement(By.id("sendFleet")).getAttribute("class").trim().endsWith("on")) {
						firefox.get().findElement(By.id("sendFleet")).click();
						firefox.loading();
						
						logger.info("I order to send an expedition!");
					}
				}
			}
			

		}
	}
	
	private boolean isExpeditionAvailable() {
		List<WebElement> slotElements = firefox.get().findElement(By.id("slots")).findElements(By.className("fleft"));
		String rawSlots = slotElements.get(0).getText();
		String rawExpe = slotElements.get(1).getText();
		String splitedSlots = rawSlots.split(":")[1].trim();
		String splitedExpeditions = rawExpe.split(":")[1].trim();
		
		int currentSlots = Integer.parseInt(splitedSlots.split("/")[0]);
		int maxSlots = Integer.parseInt(splitedSlots.split("/")[1]);
		int currentExpeditions = Integer.parseInt(splitedExpeditions.split("/")[0]);
		int maxExpeditions = Integer.parseInt(splitedExpeditions.split("/")[1]);
		
		return currentSlots < maxSlots && currentExpeditions < maxExpeditions;
	}
	
}
