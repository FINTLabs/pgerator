package no.fintlabs.aiven.model;

import java.util.List;
import lombok.Data;

@Data
public class UserConfig{
	private int backupMinute;
	private Object recoveryTargetTime;
	private Pg pg;
	private Object migration;
	private Object projectToForkFrom;
	private int backupHour;
	private Object serviceToForkFrom;
	private boolean pgStatMonitorEnable;
	private Pglookout pglookout;
	private Object pgReadReplica;
	private List<String> ipFilter;
	private String pgVersion;
}