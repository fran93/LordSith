package com.fran.lordsith.services;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicBoolean;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.fran.lordsith.enums.MenuEnum;
import com.fran.lordsith.enums.StatusEnum;
import com.fran.lordsith.enums.TechnologyEnum;
import com.fran.lordsith.model.Technology;
import com.fran.lordsith.utilities.TechnologyUtils;

@Service
public class ResearchService {

	@Autowired @Lazy
	private FirefoxClient firefox;
	
	
	Logger logger = LoggerFactory.getLogger(ResearchService.class);
	
	public boolean research() {
		ArrayList<Technology> researchs = new ArrayList<>();
		AtomicBoolean researching = new AtomicBoolean(false);

		firefox.get().findElements(By.className("menubutton")).get(MenuEnum.FORSCHUNG.getId()).click();
		firefox.loading();
		
		firefox.get().findElements(By.className("technology")).forEach(technology -> {
			int id = Integer.parseInt(technology.getAttribute("data-technology"));
			String status = technology.getAttribute("data-status");
			if(status.equals(StatusEnum.ACTIVE.getValue()))
				researching.set(true);

			if(!status.equals(StatusEnum.OFF.getValue()) && id >= TechnologyEnum.SPIONAGETECHNIK.getId() && id <= TechnologyEnum.INTERGALAKTISCHES_FORSCHUNGSNETZWERK.getId()) {
				parseTechnology(researchs, technology, id, status);
			}
		});
			
		chooseWhatToBuild(researchs, researching);
		
		return researching.get();
	}
	
	private void parseTechnology(ArrayList<Technology> mines, WebElement technology, int id, String status) {
		int level = Integer.parseInt(technology.findElement(By.className("level")).getAttribute("data-value"));	
		Technology techno =  new Technology(id, level, status);
		TechnologyUtils.calculateCost(techno);
		mines.add(techno);
	}
		
	private void chooseWhatToBuild(ArrayList<Technology> researchs, AtomicBoolean researching) {
		if(!researching.get() && !researchs.isEmpty()) {
			researchs.sort(Comparator.comparingDouble(Technology::getTotalCost));			
			upTechnology(researchs.get(0), researching);
		}
	}
	
	private void upTechnology(Technology tech, AtomicBoolean researching) {			
		logger.info("I order to work on >>>>> " + TechnologyEnum.getById(tech.getId()).name() + " at current level >>>>> " + tech.getLevel());
		
		if(tech.getStatus().equals(StatusEnum.ON.getValue())) {
			firefox.get().findElement(By.xpath("//li[@data-technology="+tech.getId()+"]")).findElement(By.tagName("button")).click();
			firefox.loading();
			researching.set(true);
		}
	}

}
