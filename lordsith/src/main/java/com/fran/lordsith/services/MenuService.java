package com.fran.lordsith.services;

import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.fran.lordsith.enums.MenuEnum;

@Service
public class MenuService {

    @Autowired
    @Lazy
    private FirefoxClient firefox;
    
    public void openPage(MenuEnum menu) throws InterruptedException {
	if (!firefox.get().getCurrentUrl().trim().endsWith(menu.getComponent())) {
	    firefox.get().findElements(By.className("menubutton")).get(menu.getId()).click();
	    firefox.shortLoading();
	}
    }
}
