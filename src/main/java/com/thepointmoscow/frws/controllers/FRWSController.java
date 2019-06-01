package com.thepointmoscow.frws.controllers;

import com.thepointmoscow.frws.FiscalGateway;
import com.thepointmoscow.frws.SelectResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * API endpoint for fiscal manipulation.
 */
@Slf4j
@Controller
@RequestMapping("/frws")
public class FRWSController {

    private final FiscalGateway frGateway;

    @Autowired
    public FRWSController(FiscalGateway frGateway) {
        this.frGateway = frGateway;
    }

    @GetMapping
    public String home() {
        log.info("Received request /frws");
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
        return "document :: document-input";
    }

    /**
     * Retrieves a document by id.
     *
     * @return status
     */
    @GetMapping("/document/{documentId}")
    public String getDocumentById(@PathVariable(value = "documentId") String documentId, Model model) {
        log.info("Received request /frws/document/{documentId}");

        SelectResult selectResult = frGateway.selectDoc(documentId);

        model.addAttribute("document", selectResult);

        return "document :: document-body";
    }

}
