package uk.gov.hmcts.reform.civil.model.hearingvalues;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartyFlagsModel {

    private String partyID;
    private String partyName;
    private String flagParentId;
    private String flagId;
    private String flagDescription;
    private String flagStatus;
    private LocalDateTime dateTimeCreated;
    private LocalDateTime dateTimeModified;
}
