package no.fintlabs.aiven.model;

import lombok.Data;

@Data
public class BackupsItem{
	private long dataSize;
	private String backupName;
	private String backupTime;
}