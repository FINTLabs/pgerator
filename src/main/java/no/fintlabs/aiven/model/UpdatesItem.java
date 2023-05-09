package no.fintlabs.aiven.model;

import lombok.Data;

@Data
public class UpdatesItem{
	private String startAfter;
	private String description;
	private Object deadline;
	private Object startAt;
}