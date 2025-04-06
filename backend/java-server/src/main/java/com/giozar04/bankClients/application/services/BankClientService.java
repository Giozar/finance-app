package com.giozar04.bankClients.application.services;

import java.util.List;

import com.giozar04.bankClient.domain.entities.BankClient;
import com.giozar04.bankClients.domain.interfaces.BankClientRepositoryInterface;

public class BankClientService implements BankClientRepositoryInterface {

    private final BankClientRepositoryInterface bankClientRepository;

    public BankClientService(BankClientRepositoryInterface bankClientRepository) {
        this.bankClientRepository = bankClientRepository;
    }

    @Override
    public BankClient createBankClient(BankClient bankClient) {
        return bankClientRepository.createBankClient(bankClient);
    }

    @Override
    public BankClient getBankClientById(long id) {
        return bankClientRepository.getBankClientById(id);
    }

    @Override
    public BankClient updateBankClientById(long id, BankClient bankClient) {
        return bankClientRepository.updateBankClientById(id, bankClient);
    }

    @Override
    public void deleteBankClientById(long id) {
        bankClientRepository.deleteBankClientById(id);
    }

    @Override
    public List<BankClient> getAllBankClients() {
        return bankClientRepository.getAllBankClients();
    }

    @Override
    public List<BankClient> getBankClientsByUserId(long userId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getBankClientsByUserId'");
    }
}
