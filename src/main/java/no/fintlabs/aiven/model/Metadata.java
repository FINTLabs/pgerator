package no.fintlabs.aiven.model;

import lombok.Data;

@Data
public class Metadata{
	private int maxConnections;
	private String pgVersion;
	private boolean writeBlockThresholdExceeded;
}