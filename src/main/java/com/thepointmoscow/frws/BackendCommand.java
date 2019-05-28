package com.thepointmoscow.frws;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class BackendCommand {
    private BackendCommandType command;
    private Order order;
    private Long issueID;
    private String ccmID;
    private String documentNumber;

    /**
     * Command types.
     */
    public enum BackendCommandType {
        /**
         * Nothing to do.
         */
        NONE,
        /**
         * Make a registration.
         */
        REGISTER,
        /**
         * Close a session.
         */
        CLOSE_SESSION,
        /**
         * Select a doc.
         */
        SELECT_DOC
    }
}
