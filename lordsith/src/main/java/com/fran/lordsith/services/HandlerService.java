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
	firefox.loading();

	clickScrap("button204");
	clickScrap("button205");
	clickScrap("button207");
	clickScrap("button211");
	firefox.shortLoading();

	if (!firefox.get().findElement(By.id("js_scrapScrapIT")).getAttribute("class").contains("disabled")) {
	    firefox.get().findElement(By.id("js_scrapScrapIT")).click();
	    firefox.loading();

	    firefox.get().findElement(By.className("yes")).click();
	    firefox.shortLoading();

	    log.info("Scraping fleet");
	}
    }

    private void clickScrap(String id) {
	if (firefox.get().findElement(By.id(id)).getAttribute("class").endsWith("on")) {
	    firefox.get().findElement(By.id(id)).click();
	}
    }

    public void importExport() throws InterruptedException {
	firefox.get().findElement(By.className("back_to_overview")).click();
	firefox.loading();
	firefox.get().findElement(By.id("js_traderImportExport")).click();
	firefox.shortLoading();
	
	if (firefox.get().findElements(By.className("got_item_text")).isEmpty()) {
	    firefox.get().findElement(By.className("js_sliderMetalMax")).click();
	    firefox.shortLoading();
	    if (!firefox.get().findElement(By.className("pay")).getAttribute("class").contains("disabled")) {
		firefox.get().findElement(By.className("pay")).click();
		firefox.shortLoading();
		
		firefox.get().findElement(By.className("take")).click();
		firefox.shortLoading();
	    }
	}
    }
}
