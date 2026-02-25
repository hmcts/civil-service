package uk.gov.hmcts.reform.civil.ga.model.docmosis;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Accessors(chain = true)
public class GaHearingForm implements MappableObject {

    private String caseNumber;
    private String court;
    private String judgeHearingLocation;
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
    private String applicant;
    private String applicationDate;
    private String claimant2;
    private String defendant2;
    private String claimant2Reference;
    private String defendant2Reference;
    private boolean claimant1exists;
    private boolean claimant2exists;
    private boolean defendant1exists;
    private boolean defendant2exists;

    private String claimReferenceNumber;
    private String emailAddress;
    private String claimantReferenceNumber;
    private String defendantReferenceNumber;

    private String partyName;
    private String partyAddressAddressLine1;
    private String partyAddressAddressLine2;
    private String partyAddressAddressLine3;
    private String partyAddressPostTown;
    private String partyAddressPostCode;
}
