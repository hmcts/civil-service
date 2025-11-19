package uk.gov.hmcts.reform.civil.model.docmosis.hearing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import uk.gov.hmcts.reform.civil.model.CorrectEmail;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;

@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class HearingForm implements MappableObject {

    private String caseNumber;
    private String court;
    private String courtName;
    private String creationDate;
    private String claimant;
    private String defendant;
    private String claimantReference;
    private String defendantReference;
    private String hearingDate;
    private String hearingTime;
    private String hearingType;
    private String hearingDuration;
    private String additionalInfo;
    private String feeAmount;
    private String hearingDueDate;
    private String additionalText;
    private String applicant;
    private String applicationDate;
    private String claimant2;
    private String defendant2;
    private String claimant2Reference;
    private String defendant2Reference;
    private boolean claimant2exists;
    private boolean defendant2exists;
    private String listingOrRelistingWithFeeDue;

    private String claimReferenceNumber;
    private CorrectEmail emailAddress;
    private String hearingFee;
    private String deadlineDate;
    private String claimantReferenceNumber;
    private String defendantReferenceNumber;
}
