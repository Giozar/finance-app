package com.giozar04.serverConnection.application.validators;

import com.giozar04.messages.domain.models.Message;
import com.giozar04.serverConnection.application.exceptions.ClientOperationException;

public class ServerResponseValidator {

    public static void validateResponse(Message response) throws ClientOperationException {
        if (response == null) {
            throw new ClientOperationException("No se recibió respuesta del servidor.");
        }

        if (response.getStatus() == Message.Status.ERROR) {
            throw new ClientOperationException(response.getContent());
        }
    }
}
