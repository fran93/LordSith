package com.fran.lordsith.utilities;

import com.fran.lordsith.enums.TechnologyEnum;
import com.fran.lordsith.model.Resources;
import com.fran.lordsith.model.Technology;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.Optional;

public class TechnologyUtils {

    private TechnologyUtils() {

    }

    public static void calculateCost(Technology techno) {
		TechnologyEnum technoEnum = TechnologyEnum.getById(techno.getId());
		techno.setCost(new Resources(technoEnum.getCost().getMetall(), technoEnum.getCost().getKristall(), technoEnum.getCost().getDeuterium(), technoEnum.getCost().getEnergie()));
		calculateCost(techno, 0, technoEnum.getFactorMultiplier());
		double totalCost = techno.getCost().getMetall() * 1;
		totalCost += techno.getCost().getKristall() * 2;
		totalCost += techno.getCost().getDeuterium() * 3;
		totalCost += techno.getCost().getEnergie() * 4;
		techno.setTotalCost(totalCost);
	}

	private static void calculateCost(Technology techno, int currentLevel, double factorMultiplier) {
		if (currentLevel < techno.getLevel()) {
			techno.getCost().setMetall(techno.getCost().getMetall() * factorMultiplier);
			techno.getCost().setKristall(techno.getCost().getKristall() * factorMultiplier);
			techno.getCost().setDeuterium(techno.getCost().getDeuterium() * factorMultiplier);
			techno.getCost().setEnergie(techno.getCost().getEnergie() * factorMultiplier);
			currentLevel++;
			calculateCost(techno, currentLevel, factorMultiplier);
		}
	}

	public static Optional<WebElement> getTechnologyById(int id, List<WebElement> ships) {
		return ships.stream().filter(ship -> ship.getAttribute("data-technology").equals(String.valueOf(id))).findFirst();
	}
}
