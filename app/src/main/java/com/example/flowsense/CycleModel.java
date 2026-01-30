package com.example.flowsense;

public class CycleModel {

    private String startDate;
    private String endDate;
    private String cycleType;
    private int cycleLength;
    private String safeEmailKey; // 👈 add this
    private String cycleId;       // 👈 add this

    public CycleModel() {
        // Needed for Firebase
    }

    public CycleModel(String startDate, String endDate, String cycleType, int cycleLength ,String cycleId, String safeEmailKey) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.cycleType = cycleType;
        this.cycleLength = cycleLength;
        this.cycleId = cycleId;
        this.safeEmailKey = safeEmailKey;
    }

    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public String getCycleType() { return cycleType; }
    public int getCycleLength() { return cycleLength; }
    public String getSafeEmailKey() {  return safeEmailKey;}
    public String getCycleId() {return cycleId;}


    public void setStartDate(String startDate) { this.startDate = startDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public void setCycleType(String cycleType) { this.cycleType = cycleType; }
    public void setCycleLength(int cycleLength) { this.cycleLength = cycleLength; }
    public void setSafeEmailKey(String safeEmailKey) { this.safeEmailKey = safeEmailKey; }
    public void setCycleId(String cycleId) { this.cycleId = cycleId; }
}
