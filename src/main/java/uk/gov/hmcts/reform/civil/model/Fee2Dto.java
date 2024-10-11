package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Fee2Dto {

    private ApplicantTypeDto applicantType;
    private ChannelTypeDto channelType;
    private String code;
    private FeeVersionDto currentVersion;
    private EventTypeDto eventType;
    private String feeType;
    private List<FeeVersionDto> feeVersions;
    private Jurisdiction1Dto jurisdiction1;
    private Jurisdiction2Dto jurisdiction2;
    private String keyword;
    private FeeVersionDto matchingVersion;
    private BigDecimal maxRange;
    private BigDecimal minRange;
    private String rangeUnit;
    private ServiceTypeDto serviceType;
    private Boolean unspecifiedClaimAmount;
}
