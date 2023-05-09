package no.fintlabs.aiven.model;

import lombok.Data;

@Data
public class IntegrationStatus{
	private String statusUserDesc;
	private State state;
}