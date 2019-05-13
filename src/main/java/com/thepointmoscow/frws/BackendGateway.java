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
}
