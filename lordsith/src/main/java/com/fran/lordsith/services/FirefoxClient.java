package com.fran.lordsith.services;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxDriverLogLevel;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.interactions.Actions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FirefoxClient {

    @Value("${headless.mode}")
    private boolean headlessMode;

    private WebDriver driver;

    @PostConstruct
    private void init() {
        Logger.getLogger("org.openqa.selenium").setLevel(Level.OFF);
        FirefoxOptions options = new FirefoxOptions();
        options.setHeadless(headlessMode);
        options.setLogLevel(FirefoxDriverLogLevel.FATAL);
        driver = new FirefoxDriver(options);
        if (headlessMode) {
            driver.manage().window().setSize(new Dimension(1920, 1080));
        } else {
            driver.manage().window().maximize();
        }
    }

    public WebDriver get() {
        return driver;
    }

    public void jsClick(WebElement element) {
        JavascriptExecutor executor = (JavascriptExecutor) driver;
        executor.executeScript("arguments[0].click();", element);
    }

    public void mouseOver(WebElement element) {
        Actions mouseHover = new Actions(driver);
        mouseHover.moveToElement(element).click().build().perform();
    }

    public void shortLoading() throws InterruptedException {
        TimeUnit.SECONDS.sleep(1);
    }

    public void loading() throws InterruptedException {
        TimeUnit.SECONDS.sleep(3);
    }

    public void longLoading() throws InterruptedException {
        TimeUnit.SECONDS.sleep(6);
    }

    public void longestLoading() throws InterruptedException {
        TimeUnit.MINUTES.sleep(1);
    }

    public void closeTab() throws InterruptedException {
        String oldtab = driver.getWindowHandle();
        driver.close();
        driver.getWindowHandles().forEach(window -> {
            if (!window.equals(oldtab)) {
                driver.switchTo().window(window);
            }
        });
        longLoading();
    }

    public void restart() {
        init();
    }

    public void quit() {
    	driver.quit();
	}

}
