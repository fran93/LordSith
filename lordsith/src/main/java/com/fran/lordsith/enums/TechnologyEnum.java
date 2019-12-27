package com.fran.lordsith.enums;

import java.util.Arrays;
import java.util.Optional;

import com.fran.lordsith.model.Resources;

public enum TechnologyEnum {

	METALLMINE(1, TypeEnum.PRODUCERS, new Resources(60,15), 1.5),
	KRISTALLMINE(2, TypeEnum.PRODUCERS, new Resources(48,24), 1.6),
	DEUTERIUMSYNTHETISIERER(3, TypeEnum.PRODUCERS, new Resources(225,75), 1.5),
	SOLARKRAFTWERK(4, TypeEnum.ENERGY, new Resources(75,30), 1.5),
	FUSIONKRAFTWERK(12, TypeEnum.ENERGY, new Resources(900,360,180), 1.8),
	ROBOTERFABRIK(14, TypeEnum.FACILITIES, new Resources(400,120,200), 2),
	NANITENFABRIK(15, TypeEnum.FACILITIES, new Resources(1000000,500000,100000), 2),
	RAUMSCHIFFSWERFT(21, TypeEnum.FACILITIES, new Resources(400,200,100), 2),
	METALLSPEICHER(22, TypeEnum.STORAGE, new Resources(1000),2),
	KRISTALLSPEICHER(23, TypeEnum.STORAGE, new Resources(1000, 500),2),
	DEUTERIUMTANK(24, TypeEnum.STORAGE, new Resources(1000, 1000),2),
	FORSCHUNGSLABOR(31, TypeEnum.FACILITIES, new Resources(200,400,200), 2),
	TERRAFORMER(33, TypeEnum.FACILITIES, new Resources(0,50000,100000, 1000), 2),
	RAKETENSILO(44, TypeEnum.FACILITIES, new Resources(20000,20000,1000), 2),
	SPIONAGETECHNIK(106, TypeEnum.TECHNOLOGY, new Resources(200,1000,200),2),
	COMPUTERTECHNIK(108, TypeEnum.TECHNOLOGY, new Resources(0,400,600),2),
	WAFFENTECHNIK(109, TypeEnum.TECHNOLOGY, new Resources(800,200),2),
	SCHILDTECHNIK(110, TypeEnum.TECHNOLOGY, new Resources(200,600),2),
	RAUMSCHIFFPANZERUNG(111, TypeEnum.TECHNOLOGY, new Resources(1000),2),
	ENERGIETECHNIK(113, TypeEnum.TECHNOLOGY, new Resources(0,800,400),2),
	HYPERRAUMTECHNIK(114, TypeEnum.TECHNOLOGY, new Resources(0,4000,2000),2),
	VERBRENNUNGSTRIEBWERK(115, TypeEnum.TECHNOLOGY, new Resources(400,0,600),2),
	IMPULSTRIEBWERK(117, TypeEnum.TECHNOLOGY, new Resources(2000,4000,600),2),
	HYPERRAUMANTRIEB(118, TypeEnum.TECHNOLOGY, new Resources(10000,20000,6000),2),
	LASERTECHNIK(120, TypeEnum.TECHNOLOGY, new Resources(200,100),2),
	IONENTECHNIK(121, TypeEnum.TECHNOLOGY, new Resources(1000,300,100),2),
	PLASMATECHNIK(122, TypeEnum.TECHNOLOGY, new Resources(2000,4000,1000),2),
	INTERGALAKTISCHES_FORSCHUNGSNETZWERK(123, TypeEnum.TECHNOLOGY, new Resources(240000,400000,160000),2),
	ASTROPHYSIK(124, TypeEnum.TECHNOLOGY, new Resources(4000,8000,4000),2),
	GROSSER_TRANSPORTER(203, TypeEnum.SPACESHIPS, new Resources(6000,6000),1),
	SPIONAGESONDE(210, TypeEnum.SPACESHIPS, new Resources(6000,6000),1),
	ZERSTORER(213, TypeEnum.SPACESHIPS, new Resources(60000,5000,15000),1),
	PATHFINDER(219, TypeEnum.SPACESHIPS, new Resources(8000,15000,8000),1),
	RAKETENWERFER(401, TypeEnum.DEFENSE, new Resources(2000), 1),
	LEICHTESLASERGESCHUTZ(402, TypeEnum.DEFENSE, new Resources(1500, 500), 1),
	SCHWERESLASERGESCHUTZ(403, TypeEnum.DEFENSE, new Resources(6000, 2000), 1),
	GAUSSKANONE(404, TypeEnum.DEFENSE, new Resources(20000, 15000,2000), 1),
	IONENGESCHUZ(405, TypeEnum.DEFENSE, new Resources(5000,3000),1),
	PLASMAWERFER(406, TypeEnum.DEFENSE, new Resources(50000,50000,30000), 1);

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
