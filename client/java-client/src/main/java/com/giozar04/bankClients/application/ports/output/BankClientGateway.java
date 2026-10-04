package com.giozar04.bankClients.application.ports.output;

import java.util.List;
import com.giozar04.bankClient.domain.entities.BankClient;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;

public interface BankClientGateway {
    BankClient createBankClient(BankClient bankClient) throws ClientOperationException;
    BankClient updateBankClientById(Long id, BankClient bankClient) throws ClientOperationException;
    void deleteBankClientById(Long id) throws ClientOperationException;
    BankClient getBankClientById(Long id) throws ClientOperationException;
    List<BankClient> getAllBankClients() throws ClientOperationException;
}
