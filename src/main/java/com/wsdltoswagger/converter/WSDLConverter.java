package com.wsdltoswagger.converter;

import com.wsdltoswagger.parser.WSDLParser;
import com.wsdltoswagger.model.ServiceDefinition;
import com.wsdltoswagger.model.Operation;
import com.wsdltoswagger.util.Logger;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.File;
import java.util.List;

public class WSDLConverter {
    private static final Logger logger = new Logger(WSDLConverter.class);
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    public void convert(String inputPath, String outputPath) throws Exception {
        logger.info("Starting WSDL to Swagger conversion");

        // Parse WSDL
        WSDLParser parser = new WSDLParser();
        ServiceDefinition service = parser.parse(inputPath);

        // Create OpenAPI specification
        OpenAPI openAPI = new OpenAPI();

        // Set basic information
        Info info = new Info()
                .title(service.getServiceName())
                .version("1.0.0")
                .description("API generated from WSDL: " + inputPath);
        openAPI.setInfo(info);

        // Create paths
        Paths paths = new Paths();
        List<Operation> operations = service.getOperations();

        for (Operation operation : operations) {
            PathItem pathItem = new PathItem();
            io.swagger.v3.oas.models.Operation swaggerOp = new io.swagger.v3.oas.models.Operation()
                    .summary(operation.getName())
                    .description(operation.getDescription());

            // Convert SOAP operation to REST
            String path = "/" + operation.getName().toLowerCase();
            pathItem.setPost(swaggerOp); // Use POST for SOAP operations
            paths.addPathItem(path, pathItem);

            logger.debug("Added operation: " + operation.getName());
        }

        openAPI.setPaths(paths);

        // Write to file using YAML format
        yamlMapper.writeValue(new File(outputPath), openAPI);

        logger.info("Conversion completed. Output written to: " + outputPath);
    }
}