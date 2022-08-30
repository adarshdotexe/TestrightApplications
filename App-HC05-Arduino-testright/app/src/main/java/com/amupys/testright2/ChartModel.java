package com.amupys.testright2;

public class ChartModel {
    private int concentration;
    private float abs;

    public ChartModel(int concentration, float abs) {
        this.concentration = concentration;
        this.abs = abs;
    }

    public int getConcentration() {
        return concentration;
    }

    public void setConcentration(int concentration) {
        this.concentration = concentration;
    }

    public float getAbs() {
        return abs;
    }

    public void setAbs(float abs) {
        this.abs = abs;
    }
}
