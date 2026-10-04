package com.giozar04.walletCardLinks.application.usecases;

import java.util.List;

import com.giozar04.walletCardLinks.domain.entities.WalletCardLink;
import com.giozar04.walletCardLinks.application.ports.output.WalletCardLinkRepository;
import com.giozar04.walletCardLinks.application.ports.input.WalletCardLinkOperations;

public class WalletCardLinkUseCase implements WalletCardLinkOperations {

    private final WalletCardLinkRepository repository;

    public WalletCardLinkUseCase(WalletCardLinkRepository repository) {
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
