package com.thepointmoscow.frws.controllers;

import com.thepointmoscow.frws.BackendCommand;
import com.thepointmoscow.frws.BackendGateway;
import com.thepointmoscow.frws.FiscalGateway;
import com.thepointmoscow.frws.StatusResult;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.stream.StreamSupport;

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
    public String getDocumentById(@PathVariable(value = "documentId") String documentId) {
        return frGateway.selectDocAsIs(documentId);
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

    @GetMapping("/management/closeArchive")
    @ResponseBody
    public StatusResult managementCloseArchive() {
        return frGateway.closeArchive();
    }

    @GetMapping("/backend/status")
    @ResponseBody
    public BackendCommand backendStatus() {
        return backend.status(ccmID, frGateway.status());
    }

    @GetMapping("/register")
    public String getRegister() {
        return "register";
    }

    @GetMapping("/registerLayout")
    public String getRegisterLayout() {
        return "register-layout";
    }

    @PostMapping("/postRegisterData")
    @ResponseBody
    public String postRegisterData(@RequestBody Map<String, Object> data) {
        return frGateway.fiscalize(data);
    }

    @GetMapping("/reRegisterLayout")
    public String getReRegisterLayout() {
        return "re-register-layout";
    }

    @PostMapping("/postReRegisterData")
    @ResponseBody
    public String postReRegisterData(@RequestBody Map<String, Object> data) {
        return frGateway.fiscalize(data);
    }
}
