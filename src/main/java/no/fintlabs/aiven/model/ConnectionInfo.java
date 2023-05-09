package no.fintlabs.aiven.model;

import java.util.List;
import lombok.Data;

@Data
public class ConnectionInfo{
	private List<Object> pgSyncing;
	private List<Object> pgStandby;
	private List<String> pg;
	private String pgbouncer;
	private List<PgParamsItem> pgParams;
}