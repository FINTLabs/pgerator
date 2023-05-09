package no.fintlabs.aiven.model;

import lombok.Data;

@Data
public class ServiceUriParams{
	private String sslmode;
	private String password;
	private String dbname;
	private String port;
	private String host;
	private String user;
}