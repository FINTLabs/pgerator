package no.fintlabs.aiven.model;

import lombok.Data;

@Data
public class ServiceIntegrationsItem{
	private String sourceService;
	private String destProject;
	private String destEndpoint;
	private boolean active;
	private String description;
	private Object destService;
	private String destServiceType;
	private String integrationType;
	private String destEndpointId;
	private boolean enabled;
	private UserConfig userConfig;
	private IntegrationStatus integrationStatus;
	private String sourceProject;
	private Object sourceEndpoint;
	private String sourceServiceType;
	private String serviceIntegrationId;
	private Object sourceEndpointId;
}