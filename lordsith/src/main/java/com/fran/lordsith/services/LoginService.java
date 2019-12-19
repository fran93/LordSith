package com.fran.lordsith.services;

import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.fran.lordsith.enums.PagesEnum;

@Service
public class LoginService {

	@Value("${ogame.email}")
	private String email;
	
	@Value("${ogame.password}")
	private String password;
	
	@Autowired @Lazy
	private FirefoxClient firefox;
	
	public void login() {
		firefox.get().get(PagesEnum.LOBBY.getUrl());
		firefox.get().findElement(By.id("loginRegisterTabs")).findElement(By.tagName("span")).click();
		firefox.get().findElement(By.name("email")).sendKeys(email);
		firefox.get().findElement(By.name("password")).sendKeys(password);
		firefox.get().findElement(By.xpath("//button[@type='submit']")).click();
		firefox.loading();
		
		firefox.get().findElement(By.id("joinGame")).findElement(By.tagName("a")).findElement(By.tagName("button")).click();
		firefox.get().findElement(By.id("accountlist")).findElement(By.tagName("button")).click();
		firefox.closeTab();
	}
	
	public boolean isLogged() {
		return firefox.get().getCurrentUrl().contains("page=ingame");
	}
	
	public void logout() {
		firefox.get().quit();
	}
}
