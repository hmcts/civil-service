package uk.gov.hmcts.reform.hmc.model.messaging;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
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
