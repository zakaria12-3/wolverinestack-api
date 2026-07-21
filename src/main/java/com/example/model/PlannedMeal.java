package com.example.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "planned_meals")
public class PlannedMeal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private User member;

    @Column(nullable = false)
    private LocalDate planDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MealType mealType;

    @Column(nullable = false, length = 500)
    private String foodName;

    @Column(length = 1000)
    private String description;

    private Double quantity;
    private String unit;

    private Integer estimatedCalories;
    private Double estimatedProtein;
    private Double estimatedCarbs;
    private Double estimatedFat;

    @Column(nullable = false)
    private Boolean consumed = false;

    private Integer orderIndex;
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getMember() { return member; }
    public void setMember(User member) { this.member = member; }

    public LocalDate getPlanDate() { return planDate; }
    public void setPlanDate(LocalDate planDate) { this.planDate = planDate; }

    public MealType getMealType() { return mealType; }
    public void setMealType(MealType mealType) { this.mealType = mealType; }

    public String getFoodName() { return foodName; }
    public void setFoodName(String foodName) { this.foodName = foodName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public Integer getEstimatedCalories() { return estimatedCalories; }
    public void setEstimatedCalories(Integer estimatedCalories) { this.estimatedCalories = estimatedCalories; }

    public Double getEstimatedProtein() { return estimatedProtein; }
    public void setEstimatedProtein(Double estimatedProtein) { this.estimatedProtein = estimatedProtein; }

    public Double getEstimatedCarbs() { return estimatedCarbs; }
    public void setEstimatedCarbs(Double estimatedCarbs) { this.estimatedCarbs = estimatedCarbs; }

    public Double getEstimatedFat() { return estimatedFat; }
    public void setEstimatedFat(Double estimatedFat) { this.estimatedFat = estimatedFat; }

    public Boolean getConsumed() { return consumed; }
    public void setConsumed(Boolean consumed) { this.consumed = consumed; }

    public Integer getOrderIndex() { return orderIndex; }
    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
