package com.giozar04.servers.infrastructure.transport.socket;

import java.io.IOException;

import com.giozar04.servers.infrastructure.transport.socket.ServerOperationException;
import com.giozar04.servers.infrastructure.transport.socket.ClientConnection;

/**
 * Define las operaciones fundamentales para un servidor de sockets.
 * Esta interfaz establece el contrato para el ciclo de vida y la gestión de clientes.
 */
public interface ServerInterface extends AutoCloseable {
    
    void startServer() throws ServerOperationException, IOException;
    
    void stopServer() throws ServerOperationException;
    
    void restartServer() throws ServerOperationException, IOException;
    
    boolean isServerRunning() throws ServerOperationException;
    
    void handleClientConnection(ClientConnection clientConnection) throws ServerOperationException;

    void acceptClientConnections() throws ServerOperationException, IOException;

    int getConnectedClientsCount() throws ServerOperationException;
}
