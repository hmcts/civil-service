package uk.gov.hmcts.reform.fees.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeeVersionDto {
    public static final String STATUS_DRAFT = "draft";
    public static final String STATUS_APPROVED = "approved";
    public static final String STATUS_PENDING = "pending_approval";
    public static final String STATUS_DISCONTINUED = "discontinued";

    private String approvedBy;
    private String author;
    private String description;
    private String direction;
    private String feeOrderName;
    private FlatAmountDto flatAmount;
    private String memoLine;
    private String naturalAccountCode;
    private PercentageAmountDto percentageAmount;
    private String siRefId;
    private String status;
    private String statutoryInstrument;
    private OffsetDateTime validFrom;
    private OffsetDateTime validTo;
    private Integer version;
    private VolumeAmountDto volumeAmount;
}
