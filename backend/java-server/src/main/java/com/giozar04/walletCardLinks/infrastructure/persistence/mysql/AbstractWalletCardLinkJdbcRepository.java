package com.giozar04.walletCardLinks.infrastructure.persistence.mysql;

import java.util.List;
import java.util.Objects;

import com.giozar04.databases.infrastructure.persistence.mysql.DatabaseConnectionInterface;
import com.giozar04.logging.infrastructure.ConsoleLogger;
import com.giozar04.walletCardLinks.domain.entities.WalletCardLink;
import com.giozar04.walletCardLinks.application.ports.output.WalletCardLinkRepository;
import com.giozar04.walletCardLinks.domain.policies.WalletCardLinkPolicy;

public abstract class AbstractWalletCardLinkJdbcRepository implements WalletCardLinkRepository {

    protected final DatabaseConnectionInterface databaseConnection;
    protected final ConsoleLogger logger = ConsoleLogger.getInstance();

    protected AbstractWalletCardLinkJdbcRepository(DatabaseConnectionInterface databaseConnection) {
        this.databaseConnection = Objects.requireNonNull(databaseConnection, "La conexión a base de datos no puede ser nula");
    }

    protected void validateLink(WalletCardLink link) {
        WalletCardLinkPolicy.validateLink(link);
    }

    protected void validateId(long id) {
        WalletCardLinkPolicy.validateId(id);
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
