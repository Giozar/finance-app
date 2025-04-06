package com.giozar04.bankClients.domain.interfaces;

import java.util.List;

import com.giozar04.bankClient.domain.entities.BankClient;

public interface BankClientRepositoryInterface {
    BankClient createBankClient(BankClient bankClient);
    BankClient getBankClientById(long id);
    List<BankClient> getBankClientsByUserId(long userId);
    BankClient updateBankClientById(long id, BankClient updated);
    void deleteBankClientById(long id);
    List<BankClient> getAllBankClients();
}
