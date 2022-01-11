package uk.gov.hmcts.reform.civil.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;

@Data
@Builder(toBuilder = true)
public class GeneralApplication implements MappableObject {

    private final ApplicationType generalAppType;
    private final RespondentOrderAgreement generalAppRespondentAgreement;
    private final BusinessProcess businessProcess;
    private final PBADetails generalAppPBADetails;
    private final String generalAppDetailsOfOrder;
    private final String generalAppReasonsOfOrder;
    private final InformOtherParty generalAppInformOtherParty;
    private final UrgencyRequirement generalAppUrgencyRequirement;

    @JsonCreator
    GeneralApplication(@JsonProperty("generalAppType") ApplicationType generalAppType,
                       @JsonProperty("generalAppRespondentAgreement")
                           RespondentOrderAgreement generalAppRespondentAgreement,
                       @JsonProperty("businessProcess") BusinessProcess businessProcess,
                       @JsonProperty("generalAppPBADetails") PBADetails generalAppPBADetails,
                       @JsonProperty("generalAppDetailsOfOrder") String generalAppDetailsOfOrder,
                       @JsonProperty("generalAppReasonsOfOrder") String generalAppReasonsOfOrder,
                       @JsonProperty("generalAppInformOtherParty") InformOtherParty generalAppInformOtherParty,
                       @JsonProperty("generalAppUrgencyRequirement") UrgencyRequirement generalAppUrgencyRequirement) {
        this.generalAppType = generalAppType;
        this.generalAppRespondentAgreement = generalAppRespondentAgreement;
        this.businessProcess = businessProcess;
        this.generalAppPBADetails = generalAppPBADetails;
        this.generalAppDetailsOfOrder = generalAppDetailsOfOrder;
        this.generalAppReasonsOfOrder = generalAppReasonsOfOrder;
        this.generalAppInformOtherParty = generalAppInformOtherParty;
        this.generalAppUrgencyRequirement = generalAppUrgencyRequirement;
    }
}
