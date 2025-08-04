package uk.gov.hmcts.reform.hmc.model.messaging;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HmcMessage {

    private String hmctsServiceCode;

    @JsonProperty("caseRef")
    private Long caseId;

    @JsonProperty("hearingID")
    private String hearingId;

    private HearingUpdate hearingUpdate;
}
