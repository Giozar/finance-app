package com.giozar04.walletCardLinks.domain.interfaces;

import java.util.List;

import com.giozar04.walletCardLinks.domain.entities.WalletCardLink;

public interface WalletCardLinkRepositoryInterface {
    WalletCardLink createLink(WalletCardLink link);
    WalletCardLink getLinkById(long id);
    WalletCardLink updateLinkById(long id, WalletCardLink link);
    void deleteLinkById(long id);
    List<WalletCardLink> getAllLinks();
    List<WalletCardLink> getLinksByWalletAccountId(long walletAccountId);
}
