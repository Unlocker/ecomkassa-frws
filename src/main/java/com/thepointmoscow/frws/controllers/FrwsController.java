package com.thepointmoscow.frws.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.bind.annotation.ResponseBody;

import com.thepointmoscow.frws.BackendCommand;
import com.thepointmoscow.frws.BackendGateway;
import com.thepointmoscow.frws.FiscalGateway;
import com.thepointmoscow.frws.SelectResult;
import com.thepointmoscow.frws.StatusResult;

import java.util.Arrays;
import java.util.Properties;
import java.util.stream.StreamSupport;

import lombok.extern.slf4j.Slf4j;

/**
 * API endpoint for fiscal manipulation.
 */
@Slf4j
@Controller
@RequestMapping("/frws")
@PropertySource("classpath:/application.yml")
public class FrwsController {

    @Value("${backend.server.ccmID}")
    private String ccmID;

    private final FiscalGateway frGateway;
    private final Environment environment;
    private final BackendGateway backend;

    @Autowired
    public FrwsController(FiscalGateway frGateway, Environment environment, BackendGateway backend) {
        this.frGateway = frGateway;
        this.environment = environment;
        this.backend = backend;
    }

    @GetMapping
    public String home() {
        return "index";
    }

    /**
     * Retrieves a status of a fiscal registrar.
     *
     * @return status
     */
    @GetMapping("/document")
    public String getDocument() {
        return "document :: document-input";
    }

    /**
     * Retrieves a document by id.
     *
     * @return status
     */
    @GetMapping("/document/{documentId}")
    @ResponseBody
    public SelectResult getDocumentById(@PathVariable(value = "documentId") String documentId) {
        return frGateway.selectDoc(documentId);
    }

    @GetMapping("/settings")
    public String getSettings(Model model) {
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

    @GetMapping("/management")
    public String getManagement() {
        return "management";
    }

    @GetMapping("/management/open")
    @ResponseBody
    public StatusResult managementOpen() {
        return frGateway.openSession();
    }

    @GetMapping("/management/close")
    @ResponseBody
    public StatusResult managementClose() {
        return frGateway.closeSession();
    }

    @GetMapping("/backend/status")
    @ResponseBody
    public BackendCommand backendStatus() {
        return backend.status(ccmID, frGateway.status());
    }

}
