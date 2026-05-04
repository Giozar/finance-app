package com.giozar04.accounts.domain.entities;

import java.io.Serializable;
import java.time.ZonedDateTime;

import com.giozar04.accounts.domain.enums.AccountTypes;

public class Account implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private long userId;
    private Long bankClientId;
    private String name;
    private AccountTypes type;
    private double currentBalance;
    // bank_details fields
    private String accountNumber;
    private String clabe;
    private Boolean canTransferOut = true;
    // credit_details fields
    private Double creditLimit;
    private Integer cutoffDay;
    private Integer paymentDay;
    // savings_details fields
    private Double annualYield;
    private Double yieldCapAmount;
    private String lastYieldCalculation; // DATE as String "yyyy-MM-dd"
    // investment_details fields
    private String instrumentType;   // ej. CETES, BONDDIA
    private Integer termDays;        // plazo en días (nullable para instrumentos sin plazo fijo)
    private Double principalAmount;  // capital invertido
    private Double investmentAnnualYield; // tasa anual (fracción, ej. 0.105 = 10.5%)
    private Integer dayCountBasis;   // 360 o 365
    private String startDate;        // DATE as String "yyyy-MM-dd"
    private String maturityDate;     // DATE as String "yyyy-MM-dd"
    private String investmentStatus; // ACTIVE, MATURED, CANCELLED
    private Boolean autoReinvest;
    private Integer reinvestTermDays;
    private Double reinvestAnnualYield;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public Account() {}

    // Getters y setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public Long getBankClientId() { return bankClientId; }
    public void setBankClientId(Long bankClientId) { this.bankClientId = bankClientId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public AccountTypes getType() { return type; }
    public void setType(AccountTypes type) { this.type = type; }

    public double getCurrentBalance() { return currentBalance; }
    public void setCurrentBalance(double currentBalance) { this.currentBalance = currentBalance; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getClabe() { return clabe; }
    public void setClabe(String clabe) { this.clabe = clabe; }

    public Double getCreditLimit() { return creditLimit; }
    public void setCreditLimit(Double creditLimit) { this.creditLimit = creditLimit; }

    public Integer getCutoffDay() { return cutoffDay; }
    public void setCutoffDay(Integer cutoffDay) { this.cutoffDay = cutoffDay; }

    public Integer getPaymentDay() { return paymentDay; }
    public void setPaymentDay(Integer paymentDay) { this.paymentDay = paymentDay; }

    public Boolean getCanTransferOut() { return canTransferOut; }
    public void setCanTransferOut(Boolean canTransferOut) { this.canTransferOut = canTransferOut; }

    public Double getAnnualYield() { return annualYield; }
    public void setAnnualYield(Double annualYield) { this.annualYield = annualYield; }

    public Double getYieldCapAmount() { return yieldCapAmount; }
    public void setYieldCapAmount(Double yieldCapAmount) { this.yieldCapAmount = yieldCapAmount; }

    public String getLastYieldCalculation() { return lastYieldCalculation; }
    public void setLastYieldCalculation(String lastYieldCalculation) { this.lastYieldCalculation = lastYieldCalculation; }

    // --- investment_details getters/setters ---
    public String getInstrumentType() { return instrumentType; }
    public void setInstrumentType(String instrumentType) { this.instrumentType = instrumentType; }

    public Integer getTermDays() { return termDays; }
    public void setTermDays(Integer termDays) { this.termDays = termDays; }

    public Double getPrincipalAmount() { return principalAmount; }
    public void setPrincipalAmount(Double principalAmount) { this.principalAmount = principalAmount; }

    public Double getInvestmentAnnualYield() { return investmentAnnualYield; }
    public void setInvestmentAnnualYield(Double investmentAnnualYield) { this.investmentAnnualYield = investmentAnnualYield; }

    public Integer getDayCountBasis() { return dayCountBasis; }
    public void setDayCountBasis(Integer dayCountBasis) { this.dayCountBasis = dayCountBasis; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getMaturityDate() { return maturityDate; }
    public void setMaturityDate(String maturityDate) { this.maturityDate = maturityDate; }

    public String getInvestmentStatus() { return investmentStatus; }
    public void setInvestmentStatus(String investmentStatus) { this.investmentStatus = investmentStatus; }

    public Boolean getAutoReinvest() { return autoReinvest; }
    public void setAutoReinvest(Boolean autoReinvest) { this.autoReinvest = autoReinvest; }

    public Integer getReinvestTermDays() { return reinvestTermDays; }
    public void setReinvestTermDays(Integer reinvestTermDays) { this.reinvestTermDays = reinvestTermDays; }

    public Double getReinvestAnnualYield() { return reinvestAnnualYield; }
    public void setReinvestAnnualYield(Double reinvestAnnualYield) { this.reinvestAnnualYield = reinvestAnnualYield; }

    public ZonedDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }

    public ZonedDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return name + (type != null ? " ( " + type.getLabel() + " )" : "");
    }
}
