package com.thepointmoscow.frws;

import com.thepointmoscow.frws.exceptions.FiscalException;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Optional;
import java.util.function.BiConsumer;

import static com.thepointmoscow.frws.BackendCommand.BackendCommandType.CLOSE_SESSION;

@Slf4j
public class FetchTask implements Runnable {

    private final BackendGateway backend;
    private final FiscalGateway fiscal;
    private final BiConsumer<Runnable, Boolean> callback;
    private final Collection<String> ccms;

    /**
     * A single fetch task.
     *
     * @param backend  backend
     * @param fiscal   fiscal gateway
     * @param callback callback function
     * @param ccms     CCMs
     */
    public FetchTask(BackendGateway backend, FiscalGateway fiscal, BiConsumer<Runnable, Boolean> callback,
                     Collection<String> ccms) {
        this.backend = backend;
        this.fiscal = fiscal;
        this.callback = callback;
        this.ccms = ccms;
    }

    @Override
    public void run() {
        boolean hasHits = false;
        try {
            hasHits = ccms.stream().anyMatch(this::doRound);
        } finally {
            doCallback(hasHits);
        }
    }

    /**
     * Makes a round of registrations.
     *
     * @param ccmID machine ID
     * @return has registration
     */
    private boolean doRound(String ccmID) {
        BackendCommand command = null;
        StatusResult status = null;
        try {
            status = fiscal.status();
            command = backend.status(ccmID, status);
            switch (command.getCommand()) {
                case NONE:
                    return false;
                case REGISTER:
                    RegistrationResult registration = fiscal
                            .register(command.getOrder(), command.getIssueID(), 4 == status.getModeFR());
                    BackendCommand registerResponse = backend.register(ccmID, registration);
                    // if response contains a session closing command then execute it
                    if (CLOSE_SESSION == registerResponse.getCommand()) {
                        fiscal.closeSession();
                    }
                    return true;
                case SELECT_DOC:
                    SelectResult select = fiscal.selectDoc(command.getDocumentNumber());
                    backend.selectDoc(ccmID, command.getIssueID(), select);
                    return true;
                case CLOSE_SESSION:
                    fiscal.closeSession();
                    return false;
            }
        } catch (FiscalException e) {
            BackendCommand response = backend.error(ccmID, command.getIssueID(), e.getFiscalResultError());
            // if response contains a session closing command then execute it
            if (
                    Optional.ofNullable(response)
                            .map(BackendCommand::getCommand)
                            .filter(CLOSE_SESSION::equals)
                            .isPresent()
            ) {
                fiscal.closeSession();
            }
        } catch (Exception e) {
            log.error("Error while processing own status ({}) or input command ({}). {}", status, command, e);
        }
        return false;
    }

    /**
     * Does a callback.
     *
     * @param hasHits has hits
     */
    private void doCallback(Boolean hasHits) {
        if (this.callback == null)
            return;
        this.callback.accept(this, hasHits);
    }
}
