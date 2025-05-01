package uk.gov.hmcts.reform.civil.service.mediation;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.model.Party;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MediationLitigant {

    private String partyID;
    private String partyRole;
    private Party.Type partyType;
    private String partyName;
    private String paperResponse;
    private boolean represented;
    private String solicitorOrgName;
    private String litigantEmail;
    private String litigantTelephone;
    private String mediationContactName;
    private String mediationContactNumber;
    private String mediationContactEmail;
    private List<MediationUnavailability> dateRangeToAvoid;
}
