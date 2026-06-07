package com.giozar04.accounts.application.utils;

import java.util.HashMap;
import java.util.Map;

import com.giozar04.accounts.domain.entities.Account;
import com.giozar04.accounts.domain.enums.AccountTypes;
import com.giozar04.shared.utils.SharedUtils;

public class AccountUtils {

    public static Map<String, Object> accountToMap(Account account) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", account.getId());
        map.put("userId", account.getUserId());
        map.put("bankClientId", account.getBankClientId());
        map.put("name", account.getName());
        map.put("type", account.getType() != null ? account.getType().getValue() : null);
        map.put("currentBalance", account.getCurrentBalance());
        map.put("accountNumber", account.getAccountNumber());
        map.put("clabe", account.getClabe());
        map.put("canTransferOut", account.getCanTransferOut());
        map.put("creditLimit", account.getCreditLimit());
        map.put("cutoffDay", account.getCutoffDay());
        map.put("paymentDay", account.getPaymentDay());
        map.put("annualYield", account.getAnnualYield());
        map.put("yieldCapAmount", account.getYieldCapAmount());
        map.put("lastYieldCalculation", account.getLastYieldCalculation());
        // investment_details
        map.put("instrumentType", account.getInstrumentType());
        map.put("termDays", account.getTermDays());
        map.put("principalAmount", account.getPrincipalAmount());
        map.put("investmentAnnualYield", account.getInvestmentAnnualYield());
        map.put("dayCountBasis", account.getDayCountBasis());
        map.put("startDate", account.getStartDate());
        map.put("maturityDate", account.getMaturityDate());
        map.put("investmentStatus", account.getInvestmentStatus());
        map.put("autoReinvest", account.getAutoReinvest());
        map.put("reinvestTermDays", account.getReinvestTermDays());
        map.put("reinvestAnnualYield", account.getReinvestAnnualYield());

        if (account.getCreatedAt() != null) {
            map.put("createdAt", account.getCreatedAt().format(SharedUtils.getFormatter()));
        }
        if (account.getUpdatedAt() != null) {
            map.put("updatedAt", account.getUpdatedAt().format(SharedUtils.getFormatter()));
        }

        return map;
    }

    public static Account mapToAccount(Map<String, Object> map) {
        Account account = new Account();

        account.setId(SharedUtils.parseLong(map.get("id")));
        account.setUserId(SharedUtils.parseLong(map.get("userId")));
        account.setBankClientId(SharedUtils.parseNullableLong(map.get("bankClientId")));
        account.setName((String) map.getOrDefault("name", ""));
        
        String typeStr = (String) map.get("type");
        if (typeStr != null && !typeStr.isEmpty() && !"null".equals(typeStr)) {
            account.setType(AccountTypes.fromValue(typeStr));
        }
        
        account.setCurrentBalance(SharedUtils.parseDouble(map.get("currentBalance")));
        Object accNum = map.get("accountNumber");
        if (accNum != null && !"null".equals(accNum.toString())) account.setAccountNumber(accNum.toString());
        
        Object clabe = map.get("clabe");
        if (clabe != null && !"null".equals(clabe.toString())) account.setClabe(clabe.toString());
        
        account.setCreditLimit(SharedUtils.parseNullableDouble(map.get("creditLimit")));
        account.setCutoffDay(SharedUtils.parseNullableInt(map.get("cutoffDay")));
        account.setPaymentDay(SharedUtils.parseNullableInt(map.get("paymentDay")));
        
        Object canTransferOutObj = map.get("canTransferOut");
        if (canTransferOutObj != null) {
            account.setCanTransferOut(Boolean.parseBoolean(canTransferOutObj.toString()));
        } else {
            account.setCanTransferOut(true);
        }

        account.setAnnualYield(SharedUtils.parseNullableDouble(map.get("annualYield")));
        account.setYieldCapAmount(SharedUtils.parseNullableDouble(map.get("yieldCapAmount")));
        Object lyc = map.get("lastYieldCalculation");
        if (lyc != null && !"null".equals(lyc.toString())) account.setLastYieldCalculation(lyc.toString());

        // investment_details
        Object instrType = map.get("instrumentType");
        if (instrType != null && !"null".equals(instrType.toString())) account.setInstrumentType(instrType.toString());
        account.setTermDays(SharedUtils.parseNullableInt(map.get("termDays")));
        account.setPrincipalAmount(SharedUtils.parseNullableDouble(map.get("principalAmount")));
        account.setInvestmentAnnualYield(SharedUtils.parseNullableDouble(map.get("investmentAnnualYield")));
        account.setDayCountBasis(SharedUtils.parseNullableInt(map.get("dayCountBasis")));
        Object sd = map.get("startDate");
        if (sd != null && !"null".equals(sd.toString())) account.setStartDate(sd.toString());
        Object md = map.get("maturityDate");
        if (md != null && !"null".equals(md.toString())) account.setMaturityDate(md.toString());
        Object invStatus = map.get("investmentStatus");
        if (invStatus != null && !"null".equals(invStatus.toString())) account.setInvestmentStatus(invStatus.toString());
        Object ar = map.get("autoReinvest");
        if (ar != null && !"null".equals(ar.toString())) account.setAutoReinvest(Boolean.parseBoolean(ar.toString()));
        account.setReinvestTermDays(SharedUtils.parseNullableInt(map.get("reinvestTermDays")));
        account.setReinvestAnnualYield(SharedUtils.parseNullableDouble(map.get("reinvestAnnualYield")));

        account.setCreatedAt(SharedUtils.parseZonedDateTime(map.get("createdAt")));
        account.setUpdatedAt(SharedUtils.parseZonedDateTime(map.get("updatedAt")));

        return account;
    }
}
