package com.thepointmoscow.frws;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Data
@Slf4j
public class AgentInfo {
    @Getter(AccessLevel.NONE)
    private String agentType;

    public AgentType getAgentType() {
        try {
            return Enum.valueOf(AgentType.class, agentType);
        } catch (Exception e) {
            log.warn("Cannot parse agent type from '{}'. Returns {}. {}"
                    , agentType
                    , null
                    , e.getMessage()
            );
            return null;
        }
    }

    private String payingOperation;
    private List<String> payingPhones;
    private List<String> receiverPhones;
    private List<String> transferPhones;
    private String transferName;
    private String transferAddress;
    private String transferInn;
}
