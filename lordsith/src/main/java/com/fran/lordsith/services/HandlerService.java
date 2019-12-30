package com.fran.lordsith.services;

import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.fran.lordsith.enums.MenuEnum;

@Service
public class HandlerService {

    @Autowired
    @Lazy
    private FirefoxClient firefox;

    Logger log = LoggerFactory.getLogger(HandlerService.class);
    
    public void scrapFleet() throws InterruptedException {
	firefox.get().findElements(By.className("menubutton")).get(MenuEnum.HANDLER.getId()).click();
	firefox.shortLoading();
	
	firefox.get().findElement(By.id("js_traderScrap")).click();
	firefox.shortLoading();
	
	firefox.get().findElement(By.id("button204")).click();
	firefox.get().findElement(By.id("button205")).click();
	firefox.get().findElement(By.id("button207")).click();
	firefox.get().findElement(By.id("button211")).click();
	firefox.shortLoading();
	
	if(!firefox.get().findElement(By.id("js_scrapScrapIT")).getAttribute("class").contains("disabled")) {
	    firefox.get().findElement(By.id("js_scrapScrapIT")).click();
	    firefox.shortLoading();
	    
	    log.info("Scraping fleet");
	}
    }
}
