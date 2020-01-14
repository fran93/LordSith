package com.fran.lordsith.services;

import com.fran.lordsith.enums.MenuEnum;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MenuService {

    @Autowired
    @Lazy
    private FirefoxClient firefox;

    public void openPage(MenuEnum menu) throws InterruptedException {
        if (!isOnPage(menu)) {
            List<WebElement> buttons = firefox.get().findElements(By.className("menubutton"));
            if(!buttons.isEmpty()) {
                buttons.get(menu.getId()).click();
                firefox.shortLoading();
            }
        }
    }

    public boolean isOnPage(MenuEnum menu) {
        return firefox.get().getCurrentUrl().trim().contains(menu.getComponent());
    }
}
