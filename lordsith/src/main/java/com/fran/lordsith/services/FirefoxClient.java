package com.fran.lordsith.services;

import javax.annotation.PostConstruct;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FirefoxClient {

	@Value("${headless.mode}")
	private boolean headlessMode;

	private WebDriver driver;
	
	@PostConstruct
	private void init() {
		FirefoxOptions options = new FirefoxOptions();
		options.setHeadless(headlessMode);
		driver = new FirefoxDriver(options);
	}

	public WebDriver get() {
		return driver;
	}
	
	public void executeJavascript(String script) {
		JavascriptExecutor js =  (JavascriptExecutor)driver;
		js.executeScript(script, new Object[0]);
	}
	
	public void shortLoading() throws InterruptedException {
		Thread.sleep(1000);
	}
	
	public void loading() throws InterruptedException {
		Thread.sleep(3000);
	}
	
	public void longLoading() throws InterruptedException {
		Thread.sleep(6000);
	}
	
	public void closeTab() throws InterruptedException {
		String oldtab = driver.getWindowHandle();
		driver.close();
		driver.getWindowHandles().forEach(window -> {
			if(!window.equals(oldtab)) {
				driver.switchTo().window(window);
			}
		});
		longLoading();
	}
	
	public void restart() {
		driver.close();
		init();
	}


}
