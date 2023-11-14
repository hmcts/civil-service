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

    private final String caseNumber;
    private final String court;
    private final String creationDate;
    private final String claimant;
    private final String defendant;
    private final String claimantReference;
    private final String defendantReference;
    private final String hearingDate;
    private final String hearingTime;
    private final String hearingType;
    private final String hearingDuration;
    private final String additionalInfo;
    private final String feeAmount;
    private final String hearingDueDate;
    private final String additionalText;
    private final String applicant;
    private final String applicationDate;
    private final String claimant2;
    private final String defendant2;
    private final String claimant2Reference;
    private final String defendant2Reference;
    private final boolean claimant2exists;
    private final boolean defendant2exists;
    private final String listingOrRelisting;

    private final String claimReferenceNumber;
    private final CorrectEmail emailAddress;
    private final String hearingFee;
    private final String deadlineDate;
    private final String claimantReferenceNumber;
    private final String defendantReferenceNumber;
}
