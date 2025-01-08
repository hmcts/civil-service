package uk.gov.hmcts.reform.civil.model.docmosis.settleanddiscontinue;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class NoticeOfDiscontinuanceForm implements MappableObject {

    private final String caseNumber;
    private final String claimant1Name;
    private final String claimant2Name;
    private final String defendant1Name;
    private final String defendant2Name;
    private final String claimantNum;
    private final String defendantNum;
    private final String claimantNumWelsh;
    private final String defendantNumWelsh;
    private String claimantWhoIsDiscontinue;
    private String claimantsConsentToDiscontinuance;
    private String courtPermission;
    private String permissionGranted;
    private String judgeName;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate judgementDate;
    private final String judgementDateWelsh;
    private String discontinuingAgainstBothDefendants;
    private String discontinuingAgainstOneDefendant;
    private String typeOfDiscontinuance;
    private String typeOfDiscontinuanceTxt;
    private String partOfDiscontinuanceTxt;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd MMMM yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate letterIssueDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd MMMM yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate dateOfEvent;
    private final String coverLetterName;
    private final String addressLine1;
    private final String addressLine2;
    private final String addressLine3;
    private final String postCode;
    private final String claimReferenceNumber;
    private final String welshDate;
}
