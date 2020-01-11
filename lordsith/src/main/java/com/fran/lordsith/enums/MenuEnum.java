package com.fran.lordsith.enums;

public enum MenuEnum {

    UBERSICHT(0, "overview"), 
    VERSORGUNG(1, "supplies"), 
    ANLAGEN(2, "facilities"), 
    HANDLER(4, "traderOverview"), 
    FORSCHUNG(5, "research"), 
    SCHIFFSWERFT(6, "shipyard"), 
    VERTEIDIGUNG(7, "defenses"), 
    FLOTTE(8, "fleetdispatch"), 
    GALAXIE(9, "galaxy");

    private int id;
    private String component;

    MenuEnum(int id, String component) {
	this.id = id;
	this.component=component;
    }

    public int getId() {
	return id;
    }

    public String getComponent() {
        return component;
    }
    
}
