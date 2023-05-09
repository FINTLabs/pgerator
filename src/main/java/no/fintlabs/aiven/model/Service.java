package no.fintlabs.aiven.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Service{
	private List<ComponentsItem> components;
	private Metadata metadata;
	private int nodeCpuCount;
	private UserConfig userConfig;
	private Features features;
	private String updateTime;
	private boolean terminationProtection;
	private String serviceTypeDescription;
	private int nodeCount;
	private String state;
	private String plan;
	private String projectVpcId;
	private List<String> databases;
	private String createTime;
	private String cloudName;
	@JsonProperty("connection_pools")
	private List<ConnectionPoolsItem> connectionPools = new ArrayList<>();
	private String serviceName;
	private List<Object> serviceNotifications;
	private List<UsersItem> users;
	private Tags tags;
	private int nodeMemoryMb;
	private String serviceType;
	private List<String> groupList;
	private List<NodeStatesItem> nodeStates;
	private ConnectionInfo connectionInfo;
	private List<ServiceIntegrationsItem> serviceIntegrations;
	private String serviceUri;
	private int diskSpaceMb;
	private String cloudDescription;
	private Maintenance maintenance;
	private ServiceUriParams serviceUriParams;
	private List<BackupsItem> backups;
}