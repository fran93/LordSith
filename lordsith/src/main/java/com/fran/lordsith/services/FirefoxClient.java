package com.fran.lordsith.services;

import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxDriverLogLevel;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class FirefoxClient {

    @Value("${headless.mode}")
    boolean headlessMode;

    private WebDriver driver;

    @PostConstruct
    private void init() {
      System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE, "/dev/null");
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
      driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
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

  public void loading(By element) {
    WebDriverWait wait = new WebDriverWait(driver, 5);
    wait.until(ExpectedConditions.visibilityOfElementLocated(element));
  }

  public void loading(int seconds) throws InterruptedException {
    TimeUnit.SECONDS.sleep(seconds);
  }

  public void waiting() throws InterruptedException {
    TimeUnit.SECONDS.sleep(30);
  }

  public void closeTab() {
    String oldtab = driver.getWindowHandle();
    driver.close();
    driver.getWindowHandles().forEach(window -> {
      if (!window.equals(oldtab)) {
        driver.switchTo().window(window);
      }
    });
  }

    public void restart() {
        init();
    }

    public void quit() {
    	driver.quit();
	}

}
