package com.wsdltoswagger.model;

import java.util.List;

public class ServiceDefinition {
    private String serviceName;
    private List<Operation> operations;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public List<Operation> getOperations() {
        return operations;
    }

    public void setOperations(List<Operation> operations) {
        this.operations = operations;
    }
}
