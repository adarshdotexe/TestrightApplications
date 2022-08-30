package com.amupys.testright2;

import java.util.ArrayList;

public class ProgramModel {
    String name, ifMessage, elseMessage;
    int wavelength, compareCase; // compare = 1 for <,  compare = 2 for =, compare = 3 for >
    boolean printMessage;
    ArrayList<ChartModel> list;

    public ArrayList<ChartModel> getList() {
        return list;
    }

    public void setList(ArrayList<ChartModel> list) {
        this.list = list;
    }

    public ProgramModel(String name, String ifMessage, String elseMessage, int wavelength, int compareCase, boolean printMessage) {
        this.name = name;
        this.ifMessage = ifMessage;
        this.elseMessage = elseMessage;
        this.wavelength = wavelength;
        this.compareCase = compareCase;
        this.printMessage = printMessage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIfMessage() {
        return ifMessage;
    }

    public void setIfMessage(String ifMessage) {
        this.ifMessage = ifMessage;
    }

    public String getElseMessage() {
        return elseMessage;
    }

    public void setElseMessage(String elseMessage) {
        this.elseMessage = elseMessage;
    }

    public int getWavelength() {
        return wavelength;
    }

    public void setWavelength(int wavelength) {
        this.wavelength = wavelength;
    }

    public int getCompareCase() {
        return compareCase;
    }

    public void setCompareCase(int compareCase) {
        this.compareCase = compareCase;
    }

    public boolean isPrintMessage() {
        return printMessage;
    }

    public void setPrintMessage(boolean printMessage) {
        this.printMessage = printMessage;
    }
}

