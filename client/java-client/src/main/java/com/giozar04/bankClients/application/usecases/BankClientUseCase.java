package com.giozar04.bankClients.application.usecases;

import java.util.List;
import com.giozar04.bankClient.domain.entities.BankClient;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.bankClients.application.ports.input.BankClientOperations;
import com.giozar04.bankClients.application.ports.output.BankClientGateway;

public final class BankClientUseCase implements BankClientOperations {
    private final BankClientGateway gateway;

    public BankClientUseCase(BankClientGateway gateway) {
        this.gateway = java.util.Objects.requireNonNull(gateway);
    }

    @Override
    public BankClient createBankClient(BankClient bankClient) throws ClientOperationException {
        return gateway.createBankClient(bankClient);
    }

    @Override
    public BankClient updateBankClientById(Long id, BankClient bankClient) throws ClientOperationException {
        return gateway.updateBankClientById(id, bankClient);
    }

    @Override
    public void deleteBankClientById(Long id) throws ClientOperationException {
        gateway.deleteBankClientById(id);
    }

    @Override
    public BankClient getBankClientById(Long id) throws ClientOperationException {
        return gateway.getBankClientById(id);
    }

    @Override
    public List<BankClient> getAllBankClients() throws ClientOperationException {
        return gateway.getAllBankClients();
    }
}
