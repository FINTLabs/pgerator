package no.fintlabs.aiven;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.MetricService;
import no.fintlabs.aiven.model.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AivenServiceValidation {

    private final AivenService aivenService;
    private final MetricService metricService;

    @Value("${fint.aiven.pg.max-connection-pools:50}")
    private int maxConnectionPools;


    public AivenServiceValidation(AivenService aivenService, MetricService metricService) {
        this.aivenService = aivenService;
        this.metricService = metricService;
    }

    @PostConstruct
    public void init() {
        validate();
    }

    @Scheduled(cron = "${fint.aiven.pg.validation-cron:0 */5 * * * *}")
    public void validate() {
        Service aivenPGService = aivenService.getAivenService()
                .orElseThrow(() -> new RuntimeException("We're not able to get the Aiven service information. Therefor we will shutdown!"))
                .getService();

        log.info("We found {} databases, {} users and {} connection pools",
                aivenPGService.getDatabases().size(),
                aivenPGService.getUsers().size(),
                aivenPGService.getConnectionPools().size());
        
        metricService.updateMetric("flais.aiven.pg.datases", aivenPGService.getDatabases().size());
        metricService.updateMetric("flais.aiven.pg.users", aivenPGService.getUsers().size());
        metricService.updateMetric("flais.aiven.pg.connection-pools", aivenPGService.getConnectionPools().size());

        if (aivenPGService.getConnectionPools().size() >= maxConnectionPools) {
            throw new RuntimeException("The number of connection pool has hit the limit that Aiven has configured. You might want to send a SR and ask them to increase the number of connection pools");
        }
    }
}
