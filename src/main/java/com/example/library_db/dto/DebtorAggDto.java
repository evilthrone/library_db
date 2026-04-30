package com.example.library_db.dto;

import java.math.BigDecimal;

public class DebtorAggDto {
    private String fullName;
    private Integer finesCount;
    private BigDecimal totalDebt;

    public DebtorAggDto() {
    }

    public DebtorAggDto(String fullName, Integer finesCount, BigDecimal totalDebt) {
        this.fullName = fullName;
        this.finesCount = finesCount;
        this.totalDebt = totalDebt;
    }

    public String getFullName() {
        return fullName;
    }

    public Integer getFinesCount() {
        return finesCount;
    }

    public BigDecimal getTotalDebt() {
        return totalDebt;
    }
}