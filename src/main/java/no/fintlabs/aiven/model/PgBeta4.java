package no.fintlabs.aiven.model;

import java.util.List;
import lombok.Data;

@Data
public class PgBeta4{
	private Object likelyErrorCause;
	private List<Object> errors;
	private String status;
}