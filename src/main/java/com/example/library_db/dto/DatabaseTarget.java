package com.example.library_db.dto;

public enum DatabaseTarget {
    BOTH("PostgreSQL + MySQL"),
    POSTGRES("PostgreSQL"),
    MYSQL("MySQL");

    private final String label;

    DatabaseTarget(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public boolean writesPostgres() {
        return this == POSTGRES || this == BOTH;
    }

    public boolean writesMySql() {
        return this == MYSQL || this == BOTH;
    }
}
