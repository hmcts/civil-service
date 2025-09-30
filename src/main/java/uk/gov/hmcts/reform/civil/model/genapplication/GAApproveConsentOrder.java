package uk.gov.hmcts.reform.civil.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

import java.time.LocalDate;

@Setter
@Data
@Builder(toBuilder = true)
public class GAApproveConsentOrder {

    private String consentOrderDescription;
    private LocalDate consentOrderDateToEnd;
    private YesOrNo showConsentOrderDate;
    private YesOrNo isOrderProcessedByStayScheduler;

    @JsonCreator
    GAApproveConsentOrder(@JsonProperty("consentOrderDescription") String consentOrderDescription,
                         @JsonProperty("consentOrderDateToEnd") LocalDate  consentOrderDateToEnd,
                          @JsonProperty("showConsentOrderDate") YesOrNo showConsentOrderDate,
                          @JsonProperty("isOrderProcessedByStayScheduler") YesOrNo isOrderProcessedByStayScheduler) {

        this.consentOrderDescription = consentOrderDescription;
        this.consentOrderDateToEnd = consentOrderDateToEnd;
        this.showConsentOrderDate = showConsentOrderDate;
        this.isOrderProcessedByStayScheduler = isOrderProcessedByStayScheduler;
    }
}
