package com.fran.lordsith.services;

import com.fran.lordsith.enums.TechnologyEnum;
import com.fran.lordsith.model.Resources;
import com.fran.lordsith.model.Technology;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class TechnologyService {

    @Autowired
    @Lazy
    FirefoxClient firefox;

    @Autowired
    @Lazy
    MessageSource messageSource;

    Logger log = LoggerFactory.getLogger(TechnologyService.class);

    public void calculateCost(Technology techno) {
        TechnologyEnum technoEnum = TechnologyEnum.getById(techno.getId());
        techno.setCost(new Resources(technoEnum.getCost().getMetall(), technoEnum.getCost().getKristall(), technoEnum.getCost().getDeuterium(), technoEnum.getCost().getEnergie()));
        calculateCost(techno, 0, technoEnum.getFactorMultiplier());
        double totalCost = techno.getCost().getMetall() * 1;
        totalCost += techno.getCost().getKristall() * 2;
        totalCost += techno.getCost().getDeuterium() * 3;
        totalCost += techno.getCost().getEnergie() * 4;
        techno.setTotalCost(totalCost);
    }

    private void calculateCost(Technology techno, int currentLevel, double factorMultiplier) {
        if (currentLevel < techno.getLevel()) {
            techno.getCost().setMetall(techno.getCost().getMetall() * factorMultiplier);
            techno.getCost().setKristall(techno.getCost().getKristall() * factorMultiplier);
            techno.getCost().setDeuterium(techno.getCost().getDeuterium() * factorMultiplier);
            techno.getCost().setEnergie(techno.getCost().getEnergie() * factorMultiplier);
            currentLevel++;
            calculateCost(techno, currentLevel, factorMultiplier);
        }
    }

    public Optional<WebElement> getTechnologyById(int id) {
      try {
        firefox.loading(By.className("technology"));
        List<WebElement> technologies = firefox.get().findElements(By.className("technology"));
        return technologies.stream().filter(tech -> tech.getAttribute("data-technology").equals(String.valueOf(id))).findFirst();
      } catch (StaleElementReferenceException ex) {
        log.info("getTechnologyById", ex.getMessage());
      }

      return Optional.empty();
    }

    public void build(TechnologyEnum defense) throws InterruptedException {
        Optional<WebElement> ship = getTechnologyById(defense.getId());
        if (ship.isPresent()) {
            ship.get().click();
            firefox.loading(1);
            firefox.loading(By.className("upgrade"));
            if (!firefox.get().findElements(By.className("upgrade")).isEmpty()) {
                firefox.jsClick(firefox.get().findElement(By.className("upgrade")));
            }

            log.info(messageSource.getMessage("generic.build", new Object[]{1, defense.name()}, Locale.ENGLISH));
        }
    }

    public void build(TechnologyEnum tech, long quantity) throws InterruptedException {
        try {
            Optional<WebElement> ship = getTechnologyById(tech.getId());
            if (ship.isPresent()) {
                ship.get().click();
                firefox.loading(1);
                firefox.loading(By.id("build_amount"));
                firefox.get().findElement(By.id("build_amount")).sendKeys(String.valueOf(quantity));

                firefox.loading(1);
                firefox.loading(By.className("upgrade"));
                if (!firefox.get().findElements(By.className("upgrade")).isEmpty()) {
                    firefox.jsClick(firefox.get().findElement(By.className("upgrade")));
                }

                log.info(messageSource.getMessage("generic.build", new Object[]{quantity, tech.name()}, Locale.ENGLISH));
            }
        } catch (TimeoutException ex) {
            log.info("build", ex.getMessage());
        }
    }
}
