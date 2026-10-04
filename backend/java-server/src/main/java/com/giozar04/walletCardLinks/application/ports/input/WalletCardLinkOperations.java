package com.giozar04.walletCardLinks.application.ports.input;

import java.util.List;

import com.giozar04.walletCardLinks.domain.entities.WalletCardLink;

public interface WalletCardLinkOperations {
    WalletCardLink createLink(WalletCardLink link);
    WalletCardLink getLinkById(long id);
    WalletCardLink updateLinkById(long id, WalletCardLink link);
    void deleteLinkById(long id);
    List<WalletCardLink> getAllLinks();
    List<WalletCardLink> getLinksByWalletAccountId(long walletAccountId);
}
