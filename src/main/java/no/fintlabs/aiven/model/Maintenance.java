package no.fintlabs.aiven.model;

import java.util.List;
import lombok.Data;

@Data
public class Maintenance{
	private String time;
	private List<UpdatesItem> updates;
	private String dow;
}