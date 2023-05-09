package no.fintlabs.aiven.model;

import java.util.List;
import lombok.Data;

@Data
public class NodeStatesItem{
	private String role;
	private String name;
	private List<Object> progressUpdates;
	private String state;
}