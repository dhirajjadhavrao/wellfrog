package com.wellfrog.model;

import jakarta.persistence.*;

@Entity
@Table(name = "activities")
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    private Long parentId; // null for main activity, populated for sub-activity

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String categoryType; // FINANCE, WORKOUT, SCREEN_TIME, WORK_TIME, CUSTOM

    @Column(nullable = false)
    private String unit; // MINUTES, CURRENCY, COUNT, CHECKBOX

    private String color;
    private String icon;

    @Column(nullable = false)
    private Boolean active = true;

    public Activity() {}

    public Activity(Long userId, Long parentId, String name, String categoryType, String unit, String color, String icon) {
        this.userId = userId;
        this.parentId = parentId;
        this.name = name;
        this.categoryType = categoryType;
        this.unit = unit;
        this.color = color;
        this.icon = icon;
        this.active = true;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategoryType() { return categoryType; }
    public void setCategoryType(String categoryType) { this.categoryType = categoryType; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public Boolean getActive() { return active != null ? active : true; }
    public void setActive(Boolean active) { this.active = active; }
}
