package com.giozar04.walletCardLinks.application.usecases;

import java.util.List;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.walletCardLinks.domain.entities.WalletCardLink;
import com.giozar04.walletCardLinks.application.ports.input.WalletCardLinkOperations;
import com.giozar04.walletCardLinks.application.ports.output.WalletCardLinkGateway;

public final class WalletCardLinkUseCase implements WalletCardLinkOperations {
    private final WalletCardLinkGateway gateway;

    public WalletCardLinkUseCase(WalletCardLinkGateway gateway) {
        this.gateway = java.util.Objects.requireNonNull(gateway);
    }

    @Override
    public List<WalletCardLink> getAllByWalletId(Long walletAccountId) throws ClientOperationException {
        return gateway.getAllByWalletId(walletAccountId);
    }

    @Override
    public WalletCardLink createWalletCardLink(WalletCardLink link) throws ClientOperationException {
        return gateway.createWalletCardLink(link);
    }

    @Override
    public void deleteWalletCardLinkById(Long id) throws ClientOperationException {
        gateway.deleteWalletCardLinkById(id);
    }
}
