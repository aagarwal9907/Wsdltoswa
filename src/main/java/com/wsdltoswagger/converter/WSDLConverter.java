package com.wsdltoswagger.converter;

import com.wsdltoswagger.parser.WSDLParser;
import com.wsdltoswagger.model.ServiceDefinition;
import com.wsdltoswagger.model.Operation;
import com.wsdltoswagger.util.Logger;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.examples.Example;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
                .description("API generated from WSDL: " + new File(inputPath).getName())
                .contact(new Contact()
                        .name("API Support")
                        .email("support@example.com")
                        .url("https://example.com/support"))
                .license(new License()
                        .name("Apache 2.0")
                        .url("http://www.apache.org/licenses/LICENSE-2.0.html"));
        openAPI.setInfo(info);

        // Add servers
        List<Server> servers = new ArrayList<>();
        servers.add(new Server().url("http://api.example.com/v1").description("Production server"));
        servers.add(new Server().url("http://staging-api.example.com/v1").description("Staging server"));
        openAPI.setServers(servers);

        // Add security schemes
        Components components = new Components();
        SecurityScheme apiKeyScheme = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("X-API-Key");
        components.addSecuritySchemes("apiKey", apiKeyScheme);
        openAPI.setComponents(components);

        // Add global security requirement
        List<SecurityRequirement> security = new ArrayList<>();
        SecurityRequirement securityRequirement = new SecurityRequirement();
        securityRequirement.addList("apiKey", new ArrayList<>());
        security.add(securityRequirement);
        openAPI.setSecurity(security);

        // Add tags
        List<Tag> tags = new ArrayList<>();
        tags.add(new Tag()
                .name(service.getServiceName())
                .description("Operations for " + service.getServiceName()));
        openAPI.setTags(tags);

        // Create paths
        Paths paths = new Paths();
        List<Operation> operations = service.getOperations();

        for (Operation operation : operations) {
            PathItem pathItem = new PathItem();
            io.swagger.v3.oas.models.Operation swaggerOp = new io.swagger.v3.oas.models.Operation()
                    .summary(operation.getName())
                    .description(operation.getDescription())
                    .tags(List.of(service.getServiceName()));

            // Add request body schema
            RequestBody requestBody = new RequestBody();
            Schema<?> requestSchema = new Schema<>().type("object");
            Map<String, Schema> requestProperties = new HashMap<>();
            StringSchema citySchema = new StringSchema();
            citySchema.setDescription("Name of the city to get weather information");
            citySchema.setExample("San Francisco");
            requestProperties.put("city", citySchema);
            requestSchema.setProperties(requestProperties);
            requestSchema.setRequired(List.of("city"));

            requestBody.setDescription("Weather request parameters")
                    .setRequired(true)
                    .setContent(new io.swagger.v3.oas.models.media.Content()
                            .addMediaType("application/json",
                                    new io.swagger.v3.oas.models.media.MediaType()
                                            .schema(requestSchema)
                                            .addExamples("default",
                                                    new Example()
                                                            .value(Map.of("city", "San Francisco")))));
            swaggerOp.setRequestBody(requestBody);

            // Add response schemas
            ApiResponses responses = new ApiResponses();

            // Success response
            Schema<?> successSchema = new Schema<>().type("object");
            Map<String, Schema> successProperties = new HashMap<>();
            NumberSchema tempSchema = new NumberSchema();
            tempSchema.setDescription("Current temperature");
            tempSchema.setExample(72.5);
            successProperties.put("temperature", tempSchema);

            StringSchema descSchema = new StringSchema();
            descSchema.setDescription("Weather description");
            descSchema.setExample("Partly cloudy");
            successProperties.put("description", descSchema);
            successSchema.setProperties(successProperties);

            responses.addApiResponse("200", new ApiResponse()
                    .description("Successful operation")
                    .content(new io.swagger.v3.oas.models.media.Content()
                            .addMediaType("application/json",
                                    new io.swagger.v3.oas.models.media.MediaType()
                                            .schema(successSchema)
                                            .addExamples("default",
                                                    new Example()
                                                            .value(Map.of(
                                                                    "temperature", 72.5,
                                                                    "description", "Partly cloudy"))))));

            // Error responses
            Map<String, Schema> errorProperties = new HashMap<>();
            Schema<?> errorSchema = new Schema<>().type("object");
            StringSchema errorMessageSchema = new StringSchema();
            errorMessageSchema.setExample("Invalid city name");
            errorProperties.put("error", errorMessageSchema);
            errorSchema.setProperties(errorProperties);

            responses.addApiResponse("400", new ApiResponse()
                    .description("Invalid request")
                    .content(new io.swagger.v3.oas.models.media.Content()
                            .addMediaType("application/json",
                                    new io.swagger.v3.oas.models.media.MediaType()
                                            .schema(errorSchema))));

            errorMessageSchema = new StringSchema();
            errorMessageSchema.setExample("Internal server error");
            errorProperties = new HashMap<>();
            errorProperties.put("error", errorMessageSchema);
            errorSchema = new Schema<>().type("object");
            errorSchema.setProperties(errorProperties);

            responses.addApiResponse("500", new ApiResponse()
                    .description("Internal server error")
                    .content(new io.swagger.v3.oas.models.media.Content()
                            .addMediaType("application/json",
                                    new io.swagger.v3.oas.models.media.MediaType()
                                            .schema(errorSchema))));

            swaggerOp.setResponses(responses);

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