package uk.gov.hmcts.reform.civil.ga.model.docmosis;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Accessors(chain = true)
public class GADraftForm implements MappableObject {

    private String claimNumber;
    private String applicationId;
    private String claimantName;
    private String defendantName;
    private String claimantReference;
    private String defendantReference;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "d MMMM yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate date;

    /*
   Payment date will be issue Date
    */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "d MMMM yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate issueDate;
    private String applicantPartyName;
    private YesOrNo hasAgreed;
    private YesOrNo isWithNotice;
    private String reasonsForWithoutNotice;
    private YesOrNo generalAppUrgency;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "d MMMM yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate urgentAppConsiderationDate;

    private String reasonsForUrgency;
    private String generalAppType;
    private String generalAppDetailsOfOrder;
    private String generalAppReasonsOfOrder;
    private YesOrNo hearingYesorNo;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "d MMMM yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate hearingDate;

    private String hearingPreferencesPreferredType;
    private String reasonForPreferredHearingType;
    private String hearingPreferredLocation;
    private String hearingDetailsTelephoneNumber;
    private String hearingDetailsEmailId;
    private YesOrNo unavailableTrialRequiredYesOrNo;
    private List<UnavailableDates> unavailableTrialDates;

    private YesOrNo vulnerabilityQuestionsYesOrNo;
    private String supportRequirement;
    private String supportRequirementSignLanguage;
    private Boolean isSignLanguageExists;
    private String supportRequirementLanguageInterpreter;
    private Boolean isLanguageInterpreterExists;
    private String supportRequirementOther;
    private Boolean isOtherSupportExists;
    private String name;
    private String role;
    private String responseSotName;
    private String responseSotRole;

    private YesOrNo resp1HasAgreed;
    private YesOrNo gaResp1Consent;
    private String resp1DebtorOffer;
    private String resp1DeclineReason;
    private YesOrNo resp1HearingYesOrNo;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "d MMMM yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate resp1Hearingdate;

    private String resp1HearingPreferredType;
    private String resp1ReasonForPreferredType;
    private String resp1PreferredLocation;
    private String resp1PreferredTelephone;
    private String resp1PreferredEmail;
    private YesOrNo resp1UnavailableTrialRequired;
    private List<UnavailableDates> resp1UnavailableTrialDates;

    private YesOrNo resp1VulnerableQuestions;
    private String resp1SupportRequirement;
    private String resp1SignLanguage;
    private String resp1LanguageInterpreter;
    private String resp1Other;
    private YesOrNo isOneVTwoApp;
    private YesOrNo isConsentOrderApp;
    private YesOrNo isVaryJudgmentApp;
    private Boolean isResp1SignLanguageExists;
    private Boolean isResp1LanguageInterpreterExists;
    private Boolean isResp1OtherSupportExists;

    private YesOrNo resp2HasAgreed;
    private YesOrNo gaResp2Consent;
    private String resp2DebtorOffer;
    private YesOrNo resp2HearingYesOrNo;
    private String resp2DeclineReason;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "d MMMM yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate resp2Hearingdate;

    private String resp2HearingPreferredType;
    private String resp2ReasonForPreferredType;
    private String resp2PreferredLocation;
    private String resp2PreferredTelephone;
    private String resp2PreferredEmail;
    private YesOrNo resp2UnavailableTrialRequired;
    private List<UnavailableDates> resp2UnavailableTrialDates;

    private YesOrNo resp2VulnerableQuestions;
    private String resp2SupportRequirement;
    private String resp2SignLanguage;
    private String resp2LanguageInterpreter;
    private String resp2Other;
    private Boolean isResp2SignLanguageExists;
    private Boolean isResp2LanguageInterpreterExists;
    private Boolean isResp2OtherSupportExists;
    private Boolean isCasePastDueDate;
    private YesOrNo isLipCase;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "d MMMM yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate submittedDate;
}
