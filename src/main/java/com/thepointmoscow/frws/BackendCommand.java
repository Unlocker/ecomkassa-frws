package com.thepointmoscow.frws;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class BackendCommand {
    @NonNull
    private BackendCommandType command;

    @NonNull
    private Long issueID;

    @NonNull
    private String ccmID;

    private Order order;
    private String documentNumber;

    public BackendCommand() {
    }

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
