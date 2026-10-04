package com.giozar04.walletCardLinks.application.ports.output;

import java.util.List;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;
import com.giozar04.walletCardLinks.domain.entities.WalletCardLink;

public interface WalletCardLinkGateway {
    List<WalletCardLink> getAllByWalletId(Long walletAccountId) throws ClientOperationException;
    WalletCardLink createWalletCardLink(WalletCardLink link) throws ClientOperationException;
    void deleteWalletCardLinkById(Long id) throws ClientOperationException;
}
