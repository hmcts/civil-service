package uk.gov.hmcts.reform.migration.migration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "migration")
public class MigrationProperties {

    private String id;
    private String jurisdiction;
    private String caseType;

    private String eventId;
    private String eventDescription;
    private String eventSummary;

    private Integer maxCasesToProcess;
    private Integer numThreads;
    private boolean dryRun;
    private String caseIds;

    private Integer esQuerySize;
    private Boolean esEnabled;
}
