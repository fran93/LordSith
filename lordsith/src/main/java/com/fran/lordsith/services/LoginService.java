package com.fran.lordsith.services;

import java.util.Locale;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchWindowException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.fran.lordsith.enums.MenuEnum;

@Service
public class LoginService {

    @Value("${ogame.email}")
    private String email;

    @Value("${ogame.password}")
    private String password;

    @Value("${ogame.url}")
    private String url;

    @Autowired
    @Lazy
    private FirefoxClient firefox;

    @Autowired
    @Lazy
    private MessageSource messageSource;

    Logger log = LoggerFactory.getLogger(LoginService.class);

    private int exhaustion;

    public void login() throws InterruptedException {
		handleExhaustion();
		firefox.get().get(url);
        firefox.shortLoading();

        if (firefox.get().findElements(By.className("hub-logo")).isEmpty()) {
            firefox.get().findElement(By.id("loginRegisterTabs")).findElement(By.tagName("span")).click();
            firefox.get().findElement(By.name("email")).sendKeys(email);
            firefox.get().findElement(By.name("password")).sendKeys(password);
            firefox.get().findElement(By.xpath("//button[@type='submit']")).click();
            firefox.shortLoading();
        }

        firefox.get().findElement(By.id("joinGame")).findElement(By.tagName("a")).findElement(By.tagName("button")).click();
        firefox.get().findElement(By.id("accountlist")).findElement(By.tagName("button")).click();
        firefox.closeTab();
    }

	private void handleExhaustion() {
		exhaustion++;
		if(exhaustion >= 10) {
			exhaustion = 0;
			firefox.quit();
			firefox.restart();
			log.info(messageSource.getMessage("commander.new.team", new Object[]{exhaustion}, Locale.ENGLISH));
		}
	}

	public boolean isLogged() {
        try {
            if (firefox.get().getCurrentUrl().contains("page=ingame")) {
                firefox.get().findElements(By.className("menubutton")).get(MenuEnum.UBERSICHT.getId()).click();
            }
        } catch (NoSuchWindowException ex) {
            firefox.restart();
            log.info(messageSource.getMessage("commander.new.team", new Object[]{exhaustion}, Locale.ENGLISH));
        }
        return firefox.get().getCurrentUrl().contains("page=ingame");
    }

    public void logout() throws InterruptedException {
        firefox.get().findElement(By.id("bar")).findElements(By.tagName("li")).get(7).click();
        firefox.shortLoading();
    }

}
