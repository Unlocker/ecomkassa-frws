package com.thepointmoscow.frws;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum AgentType {
    BANK_PAYING_AGENT(1),
    BANK_PAYING_SUBAGENT(2),
    PAYING_AGENT(4),
    PAYING_SUBAGENT(8),
    ATTORNEY(16),
    COMMISSION_AGENT(32),
    OTHER(64);

    /**
     * Tag number.
     */
    public static final int AGENT_TYPE_FFD_TAG = 1222;

    @Getter
    private final int ffdCode;

}
