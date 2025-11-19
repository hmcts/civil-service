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

    private String title;
    private Long caseNumber;
    private LocalDate creationDate;
    private String creationDateWelshText;
    private String claimant;
    private String claimant2;
    private String defendant;
    private String defendant2;
    private String claimantReference;
    private String claimant2Reference;
    private String defendantReference;
    private String defendant2Reference;
    private String feeAmount;
    private String hearingSiteName;
    private String caseManagementLocation;
    private String hearingLocation;
    private String hearingDays;
    private String totalHearingDuration;
    private String hearingType;
    private String hearingTypePluralWelsh;
    private LocalDate hearingDueDate;
    private String hearingDueDateWelshText;
    private PaymentDetails hearingFeePaymentDetails;
    private String partiesAttendingInPerson;
    private String partiesAttendingByTelephone;
    private String partiesAttendingByVideo;

}
