package com.fran.lordsith.model;

import com.fran.lordsith.enums.TechnologyEnum;

public class Defense {

    private int baseAmount;
    
    private TechnologyEnum technology;
    
    private boolean unique;
    
    private long amountToBuild;

    public Defense(int baseAmount, TechnologyEnum technology, boolean unique) {
	super();
	this.baseAmount = baseAmount;
	this.technology = technology;
	this.unique = unique;
    }

    public int getBaseAmount() {
        return baseAmount;
    }

    public void setBaseAmount(int baseAmount) {
        this.baseAmount = baseAmount;
    }

    public TechnologyEnum getTechnology() {
        return technology;
    }

    public void setTechnology(TechnologyEnum technology) {
        this.technology = technology;
    }

    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    public long getAmountToBuild() {
        return amountToBuild;
    }

    public void setAmountToBuild(long amountToBuild) {
        this.amountToBuild = amountToBuild;
    }

}
