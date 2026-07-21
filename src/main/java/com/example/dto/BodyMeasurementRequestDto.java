package com.example.dto;

import java.time.LocalDate;

public class BodyMeasurementRequestDto {
    private LocalDate measuredAt;
    private Double weightKg;
    private Double bodyFatPercent;
    private Double waistCm;
    private Double hipsCm;
    private Double chestCm;
    private Double leftArmCm;
    private Double rightArmCm;
    private Double leftThighCm;
    private Double rightThighCm;
    private String notes;

    public LocalDate getMeasuredAt() { return measuredAt; }
    public void setMeasuredAt(LocalDate measuredAt) { this.measuredAt = measuredAt; }

    public Double getWeightKg() { return weightKg; }
    public void setWeightKg(Double weightKg) { this.weightKg = weightKg; }

    public Double getBodyFatPercent() { return bodyFatPercent; }
    public void setBodyFatPercent(Double bodyFatPercent) { this.bodyFatPercent = bodyFatPercent; }

    public Double getWaistCm() { return waistCm; }
    public void setWaistCm(Double waistCm) { this.waistCm = waistCm; }

    public Double getHipsCm() { return hipsCm; }
    public void setHipsCm(Double hipsCm) { this.hipsCm = hipsCm; }

    public Double getChestCm() { return chestCm; }
    public void setChestCm(Double chestCm) { this.chestCm = chestCm; }

    public Double getLeftArmCm() { return leftArmCm; }
    public void setLeftArmCm(Double leftArmCm) { this.leftArmCm = leftArmCm; }

    public Double getRightArmCm() { return rightArmCm; }
    public void setRightArmCm(Double rightArmCm) { this.rightArmCm = rightArmCm; }

    public Double getLeftThighCm() { return leftThighCm; }
    public void setLeftThighCm(Double leftThighCm) { this.leftThighCm = leftThighCm; }

    public Double getRightThighCm() { return rightThighCm; }
    public void setRightThighCm(Double rightThighCm) { this.rightThighCm = rightThighCm; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
