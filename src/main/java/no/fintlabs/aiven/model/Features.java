package no.fintlabs.aiven.model;

import lombok.Data;

@Data
public class Features{
	private boolean enhancedLogging;
	private boolean serviceIntegrations;
	private boolean pgbouncerPinnedPools;
	private boolean kafkaConnectServiceIntegration;
	private boolean pgbouncer;
	private boolean pgAllowReplication;
	private boolean pgbouncerUnpinnedPools;
}