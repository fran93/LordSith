package com.fran.lordsith.services;

import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.fran.lordsith.enums.MenuEnum;
import com.fran.lordsith.enums.PagesEnum;

@Service
public class LoginService {

	@Value("${ogame.email}")
	private String email;
	
	@Value("${ogame.password}")
	private String password;
	
	@Autowired @Lazy
	private FirefoxClient firefox;
	
	private long points;
	
	public void login() throws InterruptedException {
		firefox.get().get(PagesEnum.LOBBY.getUrl());
		firefox.shortLoading();

		if(firefox.get().findElements(By.className("hub-logo")).isEmpty()) {
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

	public void extractPoints() throws InterruptedException {
		firefox.get().findElement(By.id("bar")).findElements(By.tagName("li")).get(1).findElement(By.tagName("a")).click();
		firefox.shortLoading();
		points = Long.parseLong(firefox.get().findElements(By.className("score")).get(1).getText().replaceAll("\\.", ""));
	}
	
	public boolean isLogged() {
		if(firefox.get().getCurrentUrl().contains("page=ingame")) { 
			firefox.get().findElements(By.className("menubutton")).get(MenuEnum.UBERSICHT.getId()).click();
		}
		return firefox.get().getCurrentUrl().contains("page=ingame");
	}
	
	public void logout() {
		firefox.get().quit();
	}

	public long getPoints() {
		return points;
	}

	public void setPoints(long points) {
		this.points = points;
	}
}
