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


    @JsonCreator
    GeneralApplication(@JsonProperty("generalAppType") ApplicationType generalAppType,
                       @JsonProperty("generalAppRespondentAgreement")
                           RespondentOrderAgreement generalAppRespondentAgreement,
                       @JsonProperty("businessProcess") BusinessProcess businessProcess) {
        this.generalAppType = generalAppType;
        this.generalAppRespondentAgreement = generalAppRespondentAgreement;
        this.businessProcess = businessProcess;
    }
}
