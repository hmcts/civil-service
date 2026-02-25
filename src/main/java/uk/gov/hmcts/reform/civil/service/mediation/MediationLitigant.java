package uk.gov.hmcts.reform.civil.service.mediation;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.model.Party;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Accessors(chain = true)
@EqualsAndHashCode
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
