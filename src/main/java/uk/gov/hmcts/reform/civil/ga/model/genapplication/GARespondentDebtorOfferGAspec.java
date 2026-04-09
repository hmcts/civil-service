package uk.gov.hmcts.reform.civil.ga.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.ga.enums.GADebtorPaymentPlanGAspec;
import uk.gov.hmcts.reform.civil.ga.enums.GARespondentDebtorOfferOptionsGAspec;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class GARespondentDebtorOfferGAspec {

    private GARespondentDebtorOfferOptionsGAspec respondentDebtorOffer;
    private GADebtorPaymentPlanGAspec paymentPlan;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal monthlyInstalment;

    private LocalDate paymentSetDate;
    private String debtorObjections;

    @JsonCreator
    GARespondentDebtorOfferGAspec(
        @JsonProperty("respondentDebtorOffer") GARespondentDebtorOfferOptionsGAspec respondentDebtorOffer,
        @JsonProperty("paymentPlan") GADebtorPaymentPlanGAspec paymentPlan,
        @JsonProperty("monthlyInstalment") BigDecimal monthlyInstalment,
        @JsonProperty("paymentSetDate") LocalDate paymentSetDate,
        @JsonProperty("debtorObjections") String debtorObjections) {

        this.respondentDebtorOffer = respondentDebtorOffer;
        this.paymentPlan = paymentPlan;
        this.monthlyInstalment = monthlyInstalment;
        this.paymentSetDate = paymentSetDate;
        this.debtorObjections = debtorObjections;
    }

}
