package com.example.demo.model;

public class Program {
    private int programId;
    private String programName; // e.g., "BCA", "BBA", "BPH", "BHM"

    // Constructor for creating a new Program (ID handled by DB)
    public Program(String programName) {
        this(0, programName); // programId 0 for new
    }

    // Full constructor for retrieving Program from the database
    public Program(int programId, String programName) {
        this.programId = programId;
        this.programName = programName;
    }

    // --- Getters ---
    public int getProgramId() {
        return programId;
    }

    public String getProgramName() {
        return programName;
    }

    // --- Setters ---
    public void setProgramId(int programId) {
        this.programId = programId;
    }

    public void setProgramName(String programName) {
        this.programName = programName;
    }

    @Override
    public String toString() {
        return "Program{" +
                "programId=" + programId +
                ", programName='" + programName + '\'' +
                '}';
    }
}
