package com.fran.lordsith.enums;

import java.util.Arrays;
import java.util.Optional;

import com.fran.lordsith.model.Resources;

public enum TechnologyEnum {

	METALLMINE(1, TypeEnum.PRODUCERS, new Resources(60,15), 1.5),
	KRISTALLMINE(2, TypeEnum.PRODUCERS, new Resources(48,24), 1.6),
	DEUTERIUMSYNTHETISIERER(3, TypeEnum.PRODUCERS, new Resources(225,75), 1.5),
	SOLARKRAFTWERK(4, TypeEnum.ENERGY, new Resources(75,30), 1.5),
	METALLSPEICHER(22, TypeEnum.STORAGE, new Resources(1000),2),
	KRISTALLSPEICHER(23, TypeEnum.STORAGE, new Resources(1000, 500),2),
	DEUTERIUMTANK(24, TypeEnum.STORAGE, new Resources(1000, 1000),2);

	private int id;
	private Resources cost;
	private TypeEnum type;
	private double factorMultiplier;
	
	TechnologyEnum(int id, TypeEnum type, Resources cost, double factorMultiplier) {
		this.id=id;
		this.type=type;
		this.cost=cost;
		this.factorMultiplier=factorMultiplier;
	}
	
	public static TechnologyEnum getById(int id) {
		Optional<TechnologyEnum> optional = Arrays.stream(TechnologyEnum.values()).filter(technology -> technology.getId() == id).findAny();
		return optional.isPresent() ? optional.get() : null;
	}

	public int getId() {
		return id;
	}

	public TypeEnum getType() {
		return type;
	}

	public Resources getCost() {
		return cost;
	}	
	
	public double getFactorMultiplier() {
		return factorMultiplier;
	}
}
