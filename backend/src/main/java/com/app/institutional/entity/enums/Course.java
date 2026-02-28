package com.app.institutional.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Course {
    // Engineering Courses
    EXTC_ENGINEERING("E&TC ENGINEERING"),
    COMPUTER_ENGINEERING("COMPUTER ENGINEERING"),
    AIDS_ENGINEERING("AI&DS ENGINEERING"),
    CIVIL_ENGINEERING("CIVIL ENGINEERING"),
    MECHANICAL_ENGINEERING("MECHANICAL ENGINEERING"),
    ELECTRICAL_ENGINEERING("ELECTRICAL ENGINEERING"),

    // Pharmacy Courses
    PHARMACY("PHARMACY"),
    DIPLOMA("DIPLOMA"),

    // Management Course
    MANAGEMENT_OF_BUSINESS_ANALYTICS("MANAGEMENT OF BUSINESS ANALYTICS");

    private final String displayName;

    Course(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static Course fromString(String value) {
        if (value == null)
            return null;
        for (Course course : Course.values()) {
            if (course.displayName.equalsIgnoreCase(value.trim())) {
                return course;
            }
        }
        throw new IllegalArgumentException("Unknown Course: " + value);
    }
}
