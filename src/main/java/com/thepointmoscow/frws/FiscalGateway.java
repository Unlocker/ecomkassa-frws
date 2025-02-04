package com.thepointmoscow.frws;

import java.util.Map;

/**
 * A gateway for a fiscal registrar.
 */
public interface FiscalGateway {
    /**
     * Makes a voucher register command.
     *
     * @param order       order info
     * @param issueID     issue ID
     * @param openSession is need session to open
     * @return result
     */
    RegistrationResult register(Order order, Long issueID, boolean openSession);

    /**
     * Opens a session.
     *
     * @return status
     */
    StatusResult openSession();

    /**
     * Makes a session closing command.
     *
     * @return status
     */
    StatusResult closeSession();

    /**
     * Makes an archive closing command.
     *
     * @return status
     */
    StatusResult closeArchive();

    /**
     * Cancels a check.
     *
     * @return status
     */
    StatusResult cancelCheck();

    /**
     * Makes a status retrieval command.
     *
     * @return status
     */
    StatusResult status();

    /**
     * Selects document by a document number
     *
     * @return status
     */
    SelectResult selectDoc(String documentNumber);

    /**
     * Makes a registration.
     *
     * @param data registration
     * @return registration response
     */
    String fiscalize(Map<String, Object> data);

    /**
     * Selects document by a document number without checking any tags
     *
     * @return status
     */
    String selectDocAsIs(String documentId);
}
