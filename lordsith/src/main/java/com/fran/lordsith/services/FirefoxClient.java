package com.fran.lordsith.services;

import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.springframework.stereotype.Service;

@Service
public class FirefoxClient {

	private WebDriver driver;
	
	@PostConstruct
	private void init() {
		driver = new FirefoxDriver();
	}

	public WebDriver get() {
		return driver;
	}
	
	public void loading() {
		driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
	}
	
	public void closeTab() {
		String oldtab = driver.getWindowHandle();
		driver.getWindowHandles().forEach(window -> {
			if(!window.equals(oldtab)) {
				driver.switchTo().window(window);
			}
		});
		driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
	}
}
