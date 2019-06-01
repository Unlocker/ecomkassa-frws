package com.thepointmoscow.frws.controllers;

import com.thepointmoscow.frws.FiscalGateway;
import com.thepointmoscow.frws.SelectResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Arrays;
import java.util.Properties;
import java.util.stream.StreamSupport;

/**
 * API endpoint for fiscal manipulation.
 */
@Slf4j
@Controller
@RequestMapping("/frws")
@PropertySource("classpath:/application.yml")
public class FRWSController {

    private final FiscalGateway frGateway;
    private final Environment environment;

    @Autowired
    public FRWSController(FiscalGateway frGateway, Environment environment) {
        this.frGateway = frGateway;
        this.environment = environment;
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

    @GetMapping("/settings")
    public String getSettings(Model model) {
        log.info("Received request /frws/settigns");

        Properties props = new Properties();
        MutablePropertySources propSrcs = ((AbstractEnvironment) environment).getPropertySources();
        StreamSupport.stream(propSrcs.spliterator(), false)
                .filter(ps -> ps instanceof OriginTrackedMapPropertySource)
                .map(ps -> ((EnumerablePropertySource) ps).getPropertyNames())
                .flatMap(Arrays::stream)
                .forEach(propName -> props.setProperty(propName, environment.getProperty(propName)));

        model.addAttribute("props", props);

        return "settings";
    }

}
