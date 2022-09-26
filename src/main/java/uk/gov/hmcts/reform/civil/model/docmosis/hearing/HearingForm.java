package uk.gov.hmcts.reform.civil.model.docmosis.hearing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.common.Party;
import uk.gov.hmcts.reform.civil.service.Time;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class HearingForm implements MappableObject {

    private final String caseNumber;
    private final String court;
    private final LocalDate creationDate;
    private final String claimant;
    private final String defendant;
    private final String claimantReference;
    private final String defendantReference;
    private final LocalDate hearingDate;
    private final String hearingTime;
    private final String hearingType;
    private final String duration;
    private final String additionalInfo;
    private final String feeAmount;
    private final LocalDate hearingDueDate;
    private final String additionalText;
    private final String applicant;
    private final LocalDate applicationDate;
    private final String claimant2;
    private final String defendant2;
    private final String claimant2Reference;
    private final String defendant2Reference;
    private final boolean claimant2exists;
    private final boolean defendant2exists;

}
