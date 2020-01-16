package com.fran.lordsith.services;

import com.fran.lordsith.enums.MenuEnum;
import com.fran.lordsith.enums.StatusEnum;
import com.fran.lordsith.enums.TechnologyEnum;
import com.fran.lordsith.model.Technology;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class ResearchService {

    @Autowired
    @Lazy
    FirefoxClient firefox;

    @Autowired
    @Lazy
    MessageSource messageSource;

    @Autowired
    @Lazy
    MenuService menuService;

    @Autowired
    @Lazy
    TechnologyService technologyService;


    Logger log = LoggerFactory.getLogger(ResearchService.class);

    public boolean research() throws InterruptedException {
        ArrayList<Technology> researchs = new ArrayList<>();
        AtomicBoolean researching = new AtomicBoolean(false);

        menuService.openPage(MenuEnum.FORSCHUNG);

        firefox.get().findElements(By.className("technology")).forEach(technology -> {
            int id = Integer.parseInt(technology.getAttribute("data-technology"));
            String status = technology.getAttribute("data-status");
            if (status.equals(StatusEnum.ACTIVE.getValue())) {
                researching.set(true);
            }

            if (!status.equals(StatusEnum.OFF.getValue()) && id >= TechnologyEnum.SPIONAGETECHNIK.getId() && id <= TechnologyEnum.INTERGALAKTISCHES_FORSCHUNGSNETZWERK.getId()) {
                parseTechnology(researchs, technology, id, status);
            }
        });

        chooseWhatToBuild(researchs, researching);

        return researching.get();
    }

    private void parseTechnology(ArrayList<Technology> mines, WebElement technology, int id, String status) {
        int level = Integer.parseInt(technology.findElement(By.className("level")).getAttribute("data-value"));
        Technology techno = new Technology(id, level, status);
        technologyService.calculateCost(techno);
        mines.add(techno);
    }

    private void chooseWhatToBuild(ArrayList<Technology> researchs, AtomicBoolean researching) throws InterruptedException {
        if (!researching.get() && !researchs.isEmpty()) {
            researchs.sort(Comparator.comparingDouble(Technology::getTotalCost));
            upTechnology(researchs.get(0), researching);
        }
    }

    private void upTechnology(Technology tech, AtomicBoolean researching) throws InterruptedException {
        log.info(messageSource.getMessage("research.update", new Object[]{TechnologyEnum.getById(tech.getId()).name(), tech.getLevel()}, Locale.ENGLISH));

        if (tech.getStatus().equals(StatusEnum.ON.getValue())) {
            firefox.get().findElement(By.xpath("//li[@data-technology=" + tech.getId() + "]")).findElement(By.tagName("button")).click();
            firefox.shortLoading();
            researching.set(true);
        }
    }

}
