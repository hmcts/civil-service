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

    private final String courtLocationName;
    private final String claimNumber;
    private final String defendantFullName;
    private final String defendantAddress;
    private final String defendantFullNameFromJudgment;
    private final String defendantAddressFromJudgment;
    private String applicationIssuedDate;
    private String judgmentOrderDate;
    private String dateFinalPaymentMade;
    private String judgmentTotalAmount;
    private String judgmentStatusText;
    private String judgmentStatus;
}
