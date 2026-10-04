package com.giozar04.bankClients.application.usecases;

import java.util.List;

import com.giozar04.bankClient.domain.entities.BankClient;
import com.giozar04.bankClients.application.ports.output.BankClientRepository;
import com.giozar04.bankClients.application.ports.input.BankClientOperations;

public class BankClientUseCase implements BankClientOperations {

    private final BankClientRepository bankClientRepository;

    public BankClientUseCase(BankClientRepository bankClientRepository) {
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
        return bankClientRepository.getBankClientsByUserId(userId);
    }
}
