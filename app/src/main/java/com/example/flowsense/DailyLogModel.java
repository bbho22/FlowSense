package com.example.flowsense;
import java.util.List;
public class DailyLogModel {
    private String date;
    private String bleeding;
    private List<String> mood;
    private List<String> pain;
    private String sex;
    private String mucus;
    private String temperature;

    // Empty constructor (needed for Firebase if you ever map directly)
    public DailyLogModel() {}

    // Full constructor
    public DailyLogModel(String date, String bleeding, List<String> mood,
                         List<String> pain, String sex, String mucus, String temperature) {
        this.date = date;
        this.bleeding = bleeding;
        this.mood = mood;
        this.pain = pain;
        this.sex = sex;
        this.mucus = mucus;
        this.temperature = temperature;
    }

    // ✅ Getters
    public String getDate() { return date; }
    public String getBleeding() { return bleeding; }
    public List<String> getMood() { return mood; }
    public List<String> getPain() { return pain; }
    public String getSex() { return sex; }
    public String getMucus() { return mucus; }
    public String getTemperature() { return temperature; }

    // ✅ Setters
    public void setDate(String date) { this.date = date; }
    public void setBleeding(String bleeding) { this.bleeding = bleeding; }
    public void setMood(List<String> mood) { this.mood = mood; }
    public void setPain(List<String> pain) { this.pain = pain; }
    public void setSex(String sex) { this.sex = sex; }
    public void setMucus(String mucus) { this.mucus = mucus; }
    public void setTemperature(String temperature) { this.temperature = temperature; }
}
