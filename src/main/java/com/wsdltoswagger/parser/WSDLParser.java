package com.wsdltoswagger.parser;

import com.wsdltoswagger.model.ServiceDefinition;
import com.wsdltoswagger.model.Operation;
import com.wsdltoswagger.util.Logger;
import javax.wsdl.*;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class WSDLParser {
    private static final Logger logger = new Logger(WSDLParser.class);

    public ServiceDefinition parse(String wsdlPath) throws Exception {
        logger.info("Parsing WSDL file: " + wsdlPath);

        WSDLFactory factory = WSDLFactory.newInstance();
        WSDLReader reader = factory.newWSDLReader();
        reader.setFeature("javax.wsdl.verbose", true);
        reader.setFeature("javax.wsdl.importDocuments", true);

        Definition definition = reader.readWSDL(wsdlPath);
        
        ServiceDefinition service = new ServiceDefinition();
        service.setServiceName(extractServiceName(definition));
        service.setOperations(extractOperations(definition));

        logger.info("WSDL parsing completed");
        return service;
    }

    private String extractServiceName(Definition definition) {
        if (definition.getServices().size() > 0) {
            Service service = (Service) definition.getServices().values().iterator().next();
            return service.getQName().getLocalPart();
        }
        return "UnnamedService";
    }

    private List<Operation> extractOperations(Definition definition) {
        List<Operation> operations = new ArrayList<>();
        
        Collection<PortType> portTypes = definition.getPortTypes().values();
        for (PortType portType : portTypes) {
            List<javax.wsdl.Operation> wsdlOperations = portType.getOperations();
            for (javax.wsdl.Operation wsdlOperation : wsdlOperations) {
                Operation operation = new Operation();
                operation.setName(wsdlOperation.getName());
                operation.setDescription("Operation from WSDL: " + wsdlOperation.getName());
                operations.add(operation);
                
                logger.debug("Extracted operation: " + operation.getName());
            }
        }
        
        return operations;
    }
}
