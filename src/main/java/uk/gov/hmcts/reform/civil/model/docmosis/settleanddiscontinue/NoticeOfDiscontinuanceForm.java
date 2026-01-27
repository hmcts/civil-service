package uk.gov.hmcts.reform.civil.model.docmosis.settleanddiscontinue;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class NoticeOfDiscontinuanceForm implements MappableObject {

    private String caseNumber;
    private String claimant1Name;
    private String claimant2Name;
    private String defendant1Name;
    private String defendant2Name;
    private String claimantNum;
    private String defendantNum;
    private String claimantNumWelsh;
    private String defendantNumWelsh;
    private String claimantWhoIsDiscontinue;
    private String claimantsConsentToDiscontinuance;
    private String courtPermission;
    private String permissionGranted;
    private String judgeName;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate judgementDate;
    private String judgementDateWelsh;
    private String discontinuingAgainstBothDefendants;
    private String discontinuingAgainstOneDefendant;
    private String typeOfDiscontinuance;
    private String typeOfDiscontinuanceTxt;
    private String partOfDiscontinuanceTxt;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd MMMM yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate letterIssueDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd MMMM yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate dateOfEvent;
    private String coverLetterName;
    private String addressLine1;
    private String addressLine2;
    private String addressLine3;
    private String postCode;
    private String claimReferenceNumber;
    private String welshDate;
    @JsonProperty("isQMEnabled")
    private boolean isQMEnabled;
    @JsonProperty("isRespondent1LiP")
    private boolean isRespondent1LiP;
}
