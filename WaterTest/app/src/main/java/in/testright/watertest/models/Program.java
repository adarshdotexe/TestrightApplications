package in.testright.watertest.models;

import java.util.ArrayList;

public class Program {
    int programID;
    String programName;
    ArrayList<String> programSteps;
    String unit;
    float a;
    float b;
    float c;
    int x;

    public Program(int programID, String programName, String unit, int a, int b, int c, int x) {
        this.programID = programID;
        this.programName = programName;
        this.a = a;
        this.b = b;
        this.c = c;
        this.x = x;

    }


    public int getProgramID() {
        return programID;
    }

    public void setProgramID(int programID) {
        this.programID = programID;
    }

    public void addProgramStep(String Step) {
        programSteps.add(Step);
    }

    public String getProgramStepsString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < programSteps.size(); i++) {
            stringBuilder.append(i + 1).append(") ");
            stringBuilder.append(programSteps.get(i)).append("\n");
        }
        return stringBuilder.toString();
    }

    public ArrayList<String> getProgramSteps() {
        return programSteps;
    }

    public void setProgramSteps(ArrayList<String> programSteps) {
        this.programSteps = programSteps;
    }

    public String getProgramName() {
        return programName;
    }

    public void setProgramName(String programName) {
        this.programName = programName;
    }

    public float getA() {
        return a;
    }

    public void setA(int a) {
        this.a = a;
    }

    public float getB() {
        return b;
    }

    public void setB(int b) {
        this.b = b;
    }

    public float getC() {
        return c;
    }

    public void setC(int c) {
        this.c = c;
    }

    public float getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }
}




