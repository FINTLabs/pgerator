package no.fintlabs.aiven.model;

import lombok.Data;

@Data
public class ComponentsItem{
	private String component;
	private String route;
	private int port;
	private String usage;
	private String host;
	private Object privatelinkConnectionId;
}