package com.giozar04.walletCardLinks.domain.models;

import java.util.List;
import java.util.Objects;

import com.giozar04.databases.domain.interfaces.DatabaseConnectionInterface;
import com.giozar04.logging.CustomLogger;
import com.giozar04.walletCardLinks.domain.entities.WalletCardLink;
import com.giozar04.walletCardLinks.domain.interfaces.WalletCardLinkRepositoryInterface;

public abstract class WalletCardLinkRepositoryAbstract implements WalletCardLinkRepositoryInterface {

    protected final DatabaseConnectionInterface databaseConnection;
    protected final CustomLogger logger = CustomLogger.getInstance();

    protected WalletCardLinkRepositoryAbstract(DatabaseConnectionInterface databaseConnection) {
        this.databaseConnection = Objects.requireNonNull(databaseConnection, "La conexión a base de datos no puede ser nula");
    }

    protected void validateLink(WalletCardLink link) {
        Objects.requireNonNull(link, "El enlace wallet-tarjeta no puede ser nulo");

        if (link.getWalletAccountId() <= 0) {
            throw new IllegalArgumentException("El ID de la cuenta wallet debe ser mayor que cero");
        }

        if (link.getCardId() <= 0) {
            throw new IllegalArgumentException("El ID de la tarjeta debe ser mayor que cero");
        }
    }

    protected void validateId(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID debe ser mayor que cero");
        }
    }

    @Override
    public abstract WalletCardLink createLink(WalletCardLink link);

    @Override
    public abstract WalletCardLink getLinkById(long id);

    @Override
    public abstract WalletCardLink updateLinkById(long id, WalletCardLink link);

    @Override
    public abstract void deleteLinkById(long id);

    @Override
    public abstract List<WalletCardLink> getAllLinks();

    @Override
    public abstract List<WalletCardLink> getLinksByWalletAccountId(long walletAccountId);
}
