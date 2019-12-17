package com.fran.lordsith.services;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.fran.lordsith.enums.StatusEnum;
import com.fran.lordsith.enums.TechnologyEnum;
import com.fran.lordsith.model.Resources;
import com.fran.lordsith.model.Technology;

@Service
public class TechnologyService {

	@Autowired @Lazy
	private FirefoxClient firefox;
	
	public void buildSomething() {
		ArrayList<Technology> mines = new ArrayList<>();
		ArrayList<Technology> powerPlants = new ArrayList<>();
		ArrayList<Technology> storages = new ArrayList<>();
		AtomicBoolean building = new AtomicBoolean(false);
		
		firefox.get().findElements(By.className("menubutton")).get(1).click();
		firefox.loading();
		
		double energy = Double.parseDouble(firefox.get().findElement(By.id("resources_energy")).getAttribute("data-raw"));
		double metallStorage = processStorage(firefox.get().findElement(By.id("metal_box")).getAttribute("title"));
		double kristallStorage = processStorage(firefox.get().findElement(By.id("crystal_box")).getAttribute("title"));
		double deuteriumStorage = processStorage(firefox.get().findElement(By.id("deuterium_box")).getAttribute("title"));
		Resources storage = new Resources(metallStorage, kristallStorage, deuteriumStorage, energy); 
		
		firefox.get().findElements(By.className("technology")).forEach(technology -> {
			int id = Integer.parseInt(technology.getAttribute("data-technology"));
			String status = technology.getAttribute("data-status");
			if(status.equals(StatusEnum.ACTIVE.getValue()))
				building.set(true);

			if(id <= TechnologyEnum.DEUTERIUMSYNTHETISIERER.getId()) {
				parseTechnology(mines, technology, id, status);
			} else if( id == TechnologyEnum.SOLARKRAFTWERK.getId()) {
				parseTechnology(powerPlants, technology, id, status);
			} else if (id == TechnologyEnum.FUSIONKRAFTWERK.getId() && !status.equals(StatusEnum.OFF.getValue())) {
				parseTechnology(powerPlants, technology, id, status);
			} else if(id >= TechnologyEnum.METALLSPEICHER.getId() && id <= TechnologyEnum.DEUTERIUMTANK.getId()) {
				parseTechnology(storages, technology, id, status);
			}
		});
			
		chooseWhatToBuild(mines, powerPlants, storages, building, energy, storage);
	}

	private void chooseWhatToBuild(ArrayList<Technology> mines, ArrayList<Technology> powerPlants,
			ArrayList<Technology> storages, AtomicBoolean building, double energy, Resources storage) {
		if(!building.get()) {
			if(energy < 0) {
				powerPlants.sort(Comparator.comparingDouble(Technology::getTotalCost));
				upTechnology(powerPlants.get(0), storage, storages);
			} else {
				mines.sort(Comparator.comparingDouble(Technology::getTotalCost));			
				upTechnology(mines.get(0), storage, storages);
			}
		}
	}

	private void parseTechnology(ArrayList<Technology> mines, WebElement technology, int id, String status) {
		int level = Integer.parseInt(technology.findElement(By.className("level")).getAttribute("data-value"));	
		Technology techno =  new Technology(id, level, status);
		calculateCost(techno);
		mines.add(techno);
	}
	
	private void upTechnology(Technology tech, Resources storage, ArrayList<Technology> storages) {
		if(tech.getCost().getMetall() > storage.getMetall()) {
			Optional<Technology> optional = storages.stream().filter(ss -> ss.getId() == TechnologyEnum.METALLSPEICHER.getId()).findFirst();
			if(optional.isPresent()) 
				tech = optional.get();
		} else if(tech.getCost().getKristall() > storage.getKristall()) {
			Optional<Technology> optional = storages.stream().filter(ss -> ss.getId() == TechnologyEnum.KRISTALLSPEICHER.getId()).findFirst();
			if(optional.isPresent()) 
				tech = optional.get();
		} else if(tech.getCost().getDeuterium() > storage.getDeuterium()) {
			Optional<Technology> optional = storages.stream().filter(ss -> ss.getId() == TechnologyEnum.DEUTERIUMTANK.getId()).findFirst();
			if(optional.isPresent()) 
				tech = optional.get();
		}
		
		if(tech.getStatus().equals(StatusEnum.ON.getValue())) {
			firefox.get().findElement(By.xpath("//li[@data-technology="+tech.getId()+"]")).findElement(By.tagName("button")).click();
			firefox.loading();
		}
	}
	
	private void calculateCost(Technology techno) {
		TechnologyEnum technoEnum = TechnologyEnum.getById(techno.getId());
		techno.setCost( new Resources(technoEnum.getCost().getMetall(), technoEnum.getCost().getKristall(), technoEnum.getCost().getDeuterium(), technoEnum.getCost().getEnergie()));
		calculateCost(techno, 0, technoEnum.getFactorMultiplier());
		double totalCost = techno.getCost().getMetall() * 1;
		totalCost += techno.getCost().getKristall() * 2;
		totalCost += techno.getCost().getDeuterium() * 3;
		totalCost += techno.getCost().getEnergie() * 4;
		techno.setTotalCost(totalCost);
	}
	
	private void calculateCost(Technology techno, int currentLevel, double factorMultiplier) {
		if(currentLevel < techno.getLevel()) {
			techno.getCost().setMetall(techno.getCost().getMetall() * factorMultiplier);
			techno.getCost().setKristall(techno.getCost().getKristall() * factorMultiplier);
			techno.getCost().setDeuterium(techno.getCost().getDeuterium() * factorMultiplier);
			techno.getCost().setEnergie(techno.getCost().getEnergie() * factorMultiplier);
			currentLevel++;
			calculateCost(techno, currentLevel, factorMultiplier);
		}
	}

	private double processStorage(String rawData) {
		StringBuilder sb = new StringBuilder(rawData);
		sb.delete(0, sb.indexOf("</tr>"));
		sb.delete(0, sb.indexOf("class"));
		sb.delete(0, sb.indexOf(">")+1);
		sb.delete(sb.indexOf("<"), sb.length());
		return Double.parseDouble(sb.toString().replaceAll("\\.", ""));
	}
	
	
}
