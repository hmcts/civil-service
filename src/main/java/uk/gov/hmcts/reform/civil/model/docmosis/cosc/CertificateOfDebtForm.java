package uk.gov.hmcts.reform.civil.model.docmosis.cosc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;

@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class CertificateOfDebtForm implements MappableObject {

    private String courtLocationName;
    private String claimNumber;
    private String defendantFullName;
    private String defendantAddress;
    private String defendantFullNameFromJudgment;
    private String defendantAddressFromJudgment;
    private String applicationIssuedDate;
    private String judgmentOrderDate;
    private String dateFinalPaymentMade;
    private String judgmentTotalAmount;
    private String judgmentStatusText;
    private String judgmentOrderDateWelsh;
    private String dateFinalPaymentMadeWelsh;
    private String applicationIssuedDateWelsh;
    private String judgmentStatusWelshText;
}
