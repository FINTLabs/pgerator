package no.fintlabs.aiven.model;

import java.util.List;
import lombok.Data;

@Data
public class State{
	private Nodes nodes;
	private Object likelyErrorCause;
	private List<Object> errors;
	private String status;
}