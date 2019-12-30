package com.fran.lordsith.enums;

public enum MenuEnum {

	UBERSICHT(0),
	VERSORGUNG(1),
	ANLAGEN(2),
	HANDLER(4),
	FORSCHUNG(5),
	SCHIFFSWERFT(6),
	VERTEIDIGUNG(7),
	FLOTTE(8),
	GALAXIE(9);
	
	private int id;
	
	MenuEnum(int id){
		this.id = id;
	}

	public int getId() {
		return id;
	}
}
