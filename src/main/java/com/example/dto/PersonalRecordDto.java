package com.example.dto;

import java.time.LocalDate;

public class PersonalRecordDto {

    private String exerciseName;
    private String muscleGroup;
    private String equipment;
    private Double bestWeightKg;
    private Integer bestReps;
    private Integer estimatedOneRm;
    private LocalDate achievedDate;
    private Long sessionId;
    private String sessionName;
    private int totalSessionsLogged;
    private int totalSetsLogged;

    public String getExerciseName() { return exerciseName; }
    public void setExerciseName(String exerciseName) { this.exerciseName = exerciseName; }

    public String getMuscleGroup() { return muscleGroup; }
    public void setMuscleGroup(String muscleGroup) { this.muscleGroup = muscleGroup; }

    public String getEquipment() { return equipment; }
    public void setEquipment(String equipment) { this.equipment = equipment; }

    public Double getBestWeightKg() { return bestWeightKg; }
    public void setBestWeightKg(Double bestWeightKg) { this.bestWeightKg = bestWeightKg; }

    public Integer getBestReps() { return bestReps; }
    public void setBestReps(Integer bestReps) { this.bestReps = bestReps; }

    public Integer getEstimatedOneRm() { return estimatedOneRm; }
    public void setEstimatedOneRm(Integer estimatedOneRm) { this.estimatedOneRm = estimatedOneRm; }

    public LocalDate getAchievedDate() { return achievedDate; }
    public void setAchievedDate(LocalDate achievedDate) { this.achievedDate = achievedDate; }

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public String getSessionName() { return sessionName; }
    public void setSessionName(String sessionName) { this.sessionName = sessionName; }

    public int getTotalSessionsLogged() { return totalSessionsLogged; }
    public void setTotalSessionsLogged(int totalSessionsLogged) { this.totalSessionsLogged = totalSessionsLogged; }

    public int getTotalSetsLogged() { return totalSetsLogged; }
    public void setTotalSetsLogged(int totalSetsLogged) { this.totalSetsLogged = totalSetsLogged; }
}
