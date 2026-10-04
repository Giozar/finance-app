package com.giozar04.walletCardLinks.application.ports.output;

import java.util.List;

import com.giozar04.walletCardLinks.domain.entities.WalletCardLink;

public interface WalletCardLinkRepository {
    WalletCardLink createLink(WalletCardLink link);
    WalletCardLink getLinkById(long id);
    WalletCardLink updateLinkById(long id, WalletCardLink link);
    void deleteLinkById(long id);
    List<WalletCardLink> getAllLinks();
    List<WalletCardLink> getLinksByWalletAccountId(long walletAccountId);
}
