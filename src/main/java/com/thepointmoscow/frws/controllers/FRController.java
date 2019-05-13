package com.thepointmoscow.frws.controllers;

import com.thepointmoscow.frws.FiscalGateway;
import com.thepointmoscow.frws.StatusResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * API endpoint for manual fiscal registrar managing.
 */
@RestController
@Slf4j
@RequestMapping(value = "/fr", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class FRController {

    /**
     * Fiscal registrar.
     */
    private final FiscalGateway frGateway;

    @Autowired
    public FRController(FiscalGateway frGateway) {
        this.frGateway = frGateway;
    }

    /**
     * Retrieves a status of a fiscal registrar.
     *
     * @return status
     */
    @RequestMapping("/status")
    public StatusResult status() {
        return frGateway.status();
    }

    /**
     * Manually closes a session with Z report execution.
     *
     * @return status
     */
    @RequestMapping("/session/close")
    public StatusResult closeSession() {
        return frGateway.closeSession();
    }

    /**
     * Manually opens a session.
     *
     * @return status
     */
    @RequestMapping("/session/open")
    public StatusResult openSession() {
        return frGateway.openSession();
    }

    /**
     * Manually cancels an opened check.
     *
     * @return status
     */
    @RequestMapping("/check/cancel")
    public StatusResult cancelCheck() {
        return frGateway.cancelCheck();
    }

    /**
     * Continues printing of an opened check.
     *
     * @return status
     */
    @RequestMapping("/continue/print")
    public StatusResult continuePrint() {
        return frGateway.continuePrint();
    }

}
