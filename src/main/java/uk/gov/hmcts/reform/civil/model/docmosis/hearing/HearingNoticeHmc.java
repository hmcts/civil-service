package uk.gov.hmcts.reform.civil.model.docmosis.hearing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
@Data
public class HearingNoticeHmc implements MappableObject {

    private final Long caseNumber;
    private final LocalDate creationDate;
    private final String claimant;
    private final String claimant2;
    private final String defendant;
    private final String defendant2;
    private final String claimantReference;
    private final String claimant2Reference;
    private final String defendantReference;
    private final String defendant2Reference;
    private final String feeAmount;
    private final String hearingSiteName;
    private final String hearingLocation;
    private final String hearingDays;
    private final String totalHearingDuration;
    private final String hearingType;
    private final LocalDate hearingDueDate;
    private final PaymentDetails hearingFeePaymentDetails;

}
