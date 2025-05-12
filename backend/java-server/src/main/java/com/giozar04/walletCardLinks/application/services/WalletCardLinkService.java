package com.giozar04.walletCardLinks.application.services;

import java.util.List;

import com.giozar04.walletCardLinks.domain.entities.WalletCardLink;
import com.giozar04.walletCardLinks.domain.interfaces.WalletCardLinkRepositoryInterface;

public class WalletCardLinkService implements WalletCardLinkRepositoryInterface {

    private final WalletCardLinkRepositoryInterface repository;

    public WalletCardLinkService(WalletCardLinkRepositoryInterface repository) {
        this.repository = repository;
    }

    @Override
    public WalletCardLink createLink(WalletCardLink link) {
        return repository.createLink(link);
    }

    @Override
    public WalletCardLink getLinkById(long id) {
        return repository.getLinkById(id);
    }

    @Override
    public WalletCardLink updateLinkById(long id, WalletCardLink link) {
        return repository.updateLinkById(id, link);
    }

    @Override
    public void deleteLinkById(long id) {
        repository.deleteLinkById(id);
    }

    @Override
    public List<WalletCardLink> getAllLinks() {
        return repository.getAllLinks();
    }

    @Override
    public List<WalletCardLink> getLinksByWalletAccountId(long walletAccountId) {
        return repository.getLinksByWalletAccountId(walletAccountId);
    }
}
