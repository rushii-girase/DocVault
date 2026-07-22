package com.app.institutional.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CollegeName {
    ENGINEERING("KCTs Late G.N Sapkal College Of Engineering"),
    PHARMACY_COLLEGE("KCTs R. G. Sapkal College Of Pharmacy"),
    PHARMACY_INSTITUTION("KCTs R. G. Sapkal Intitution Of Pharmacy"),
    MANAGEMENT("KCTs K. R. Sapkal College Of Management Studies");

    private final String displayName;

    CollegeName(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static CollegeName fromString(String value) {
        if (value == null)
            return null;
        for (CollegeName college : CollegeName.values()) {
            if (college.displayName.equalsIgnoreCase(value.trim())) {
                return college;
            }
        }
        throw new IllegalArgumentException("Unknown College Name: " + value);
    }
}
