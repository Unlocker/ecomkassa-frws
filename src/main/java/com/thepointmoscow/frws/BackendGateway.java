package com.thepointmoscow.frws;

/**
 * Backend gateway.
 */
public interface BackendGateway {
    /**
     * Sends status to backend.
     *
     * @param ccmID cash machine ID
     * @param statusResult status
     * @return command
     */
    BackendCommand status(String ccmID, StatusResult statusResult);

    /**
     * Sends registration to backend.
     *
     * @param ccmID cash machine ID
     * @param registration registration
     * @return command
     */
    BackendCommand register(String ccmID, RegistrationResult registration);

    /**
     * Sends error to backend.
     *
     * @param ccmID cash machine ID
     * @param issueID command current issue
     * @param resultError error
     * @return command
     */
    BackendCommand error(String ccmID, Long issueID, FiscalResultError resultError);
}
