package com.thepointmoscow.frws.controllers;

import com.thepointmoscow.frws.FiscalGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * API endpoint for fiscal manipulation.
 */
@Slf4j
@Controller
@RequestMapping("/frws")
public class FRWSController {

    /**
     * Fiscal registrar.
     */
    private final FiscalGateway frGateway;

    @Autowired
    public FRWSController(FiscalGateway frGateway) {
        this.frGateway = frGateway;
    }


    /**
     * Retrieves a status of a fiscal registrar.
     *
     * @return status
     */
    @GetMapping
    public String status() {
        log.info("YEEE");
        return "index";
    }

    /**
     * Retrieves a status of a fiscal registrar.
     *
     * @return status
     */
    @GetMapping("/document")
    public String getDocument() {
        log.info("Received request /frws/document");
        return "document";
    }

}
