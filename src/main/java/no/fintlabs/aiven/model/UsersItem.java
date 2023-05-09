package no.fintlabs.aiven.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UsersItem{
	private String password;
	@JsonProperty("access_control")
	private AccessControl accessControl;
	private String type;
	private String username;
}