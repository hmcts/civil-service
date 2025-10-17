package uk.gov.hmcts.reform.civil.sampledata;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.caseprogression.FinalOrderSelection;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.GARespondentRepresentative;
import uk.gov.hmcts.reform.civil.model.GeneralAppParentCaseLink;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;
import uk.gov.hmcts.reform.civil.model.genapplication.GAMakeApplicationAvailableCheck;
import uk.gov.hmcts.reform.civil.model.genapplication.GAPbaDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GAJudicialDecision;
import uk.gov.hmcts.reform.civil.model.genapplication.GACaseLocation;
import uk.gov.hmcts.reform.civil.model.genapplication.GACaseManagementCategory;
import uk.gov.hmcts.reform.civil.model.genapplication.GAJudicialMakeAnOrder;
import uk.gov.hmcts.reform.civil.model.genapplication.GAJudicialRequestMoreInfo;
import uk.gov.hmcts.reform.civil.model.genapplication.GAJudicialWrittenRepresentations;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GAStatementOfTruth;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDateGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GARespondentOrderAgreement;
import uk.gov.hmcts.reform.civil.model.genapplication.finalorder.AssistedOrderCost;
import uk.gov.hmcts.reform.civil.model.genapplication.finalorder.BeSpokeCostDetailText;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFees;

public class GeneralApplicationCaseDataBuilder {

    private final GeneralApplicationCaseData.GeneralApplicationCaseDataBuilder builder;

    private GeneralApplicationCaseDataBuilder() {
        builder = GeneralApplicationCaseData.builder()
            .ccdCaseReference(1234L)
            .ccdState(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
            .state(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
            .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.STARTED).build())
            .isGaApplicantLip(YesOrNo.NO)
            .isGaRespondentOneLip(YesOrNo.NO)
            .isGaRespondentTwoLip(YesOrNo.NO)
            .isMultiParty(YesOrNo.NO)
            .applicantBilingualLanguagePreference(YesOrNo.NO)
            .respondentBilingualLanguagePreference(YesOrNo.NO)
            .applicationIsCloaked(YesOrNo.NO)
            .applicationIsUncloakedOnce(YesOrNo.NO)
            .parentClaimantIsApplicant(YesOrNo.YES)
            .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("1234567890123456").build())
            .generalAppApplnSolicitor(defaultSolicitor("applicant@solicitor.test"))
            .generalAppRespondentSolicitors(List.of())
            .generalAppApplicantAddlSolicitors(List.of())
            .generalAppEvidenceDocument(List.of())
            .generalAppRespondDocument(List.of())
            .generalAppRespondConsentDocument(List.of())
            .generalAppRespondDebtorDocument(List.of())
            .generalAppWrittenRepUpload(List.of())
            .generalAppDirOrderUpload(List.of())
            .generalAppAddlnInfoUpload(List.of())
            .generalOrderDocument(List.of())
            .dismissalOrderDocument(List.of())
            .directionOrderDocument(List.of())
            .hearingNoticeDocument(List.of())
            .gaRespDocument(List.of())
            .gaDraftDocument(List.of())
            .gaAddlDoc(List.of());
    }

    public static GeneralApplicationCaseDataBuilder builder() {
        return new GeneralApplicationCaseDataBuilder();
    }

    public GeneralApplicationCaseDataBuilder withCcdCaseReference(Long reference) {
        builder.ccdCaseReference(reference);
        return this;
    }

    public GeneralApplicationCaseDataBuilder withCcdState(CaseState state) {
        builder.ccdState(state);
        return this;
    }

    public GeneralApplicationCaseDataBuilder withState(CaseState state) {
        builder.state(state);
        return this;
    }

    public GeneralApplicationCaseDataBuilder withParentClaimantIsApplicant(YesOrNo value) {
        builder.parentClaimantIsApplicant(value);
        return this;
    }

    public GeneralApplicationCaseDataBuilder withGeneralAppType(GAApplicationType type) {
        builder.generalAppType(type);
        return this;
    }

    public GeneralApplicationCaseDataBuilder withGeneralAppInformOtherParty(GAInformOtherParty informOtherParty) {
        builder.generalAppInformOtherParty(informOtherParty);
        return this;
    }

    public GeneralApplicationCaseDataBuilder withApplicationIsCloaked(YesOrNo value) {
        builder.applicationIsCloaked(value);
        return this;
    }

    public GeneralApplicationCaseDataBuilder withApplicationIsUncloakedOnce(YesOrNo value) {
        builder.applicationIsUncloakedOnce(value);
        return this;
    }

    public GeneralApplicationCaseDataBuilder withGeneralAppPBADetails(GAPbaDetails pbaDetails) {
        builder.generalAppPBADetails(pbaDetails);
        return this;
    }

    public GeneralApplicationCaseDataBuilder withGeneralAppRespondentAgreement(GARespondentOrderAgreement agreement) {
        builder.generalAppRespondentAgreement(agreement);
        return this;
    }

    public GeneralApplicationCaseDataBuilder withGeneralAppHelpWithFees(HelpWithFees helpWithFees) {
        builder.generalAppHelpWithFees(helpWithFees);
        return this;
    }

    public GeneralApplicationCaseDataBuilder withGeneralAppApplnSolicitor(GASolicitorDetailsGAspec solicitor) {
        builder.generalAppApplnSolicitor(solicitor);
        return this;
    }

    public GeneralApplicationCaseDataBuilder withMakeAppVisibleToRespondents(GAMakeApplicationAvailableCheck value) {
        builder.makeAppVisibleToRespondents(value);
        return this;
    }

    public GeneralApplicationCaseDataBuilder withGeneralAppRespondentSolicitors(List<Element<GASolicitorDetailsGAspec>> solicitors) {
        builder.generalAppRespondentSolicitors(solicitors);
        return this;
    }

    public GeneralApplicationCaseDataBuilder withLocationName(String locationName) {
        builder.locationName(locationName);
        return this;
    }

    public GeneralApplicationCaseDataBuilder withBusinessProcess(BusinessProcess process) {
        builder.businessProcess(process);
        return this;
    }

    public GeneralApplicationCaseDataBuilder withIsGaApplicantLip(YesOrNo value) {
        builder.isGaApplicantLip(value);
        return this;
    }

    public GeneralApplicationCaseDataBuilder withIsGaRespondentOneLip(YesOrNo value) {
        builder.isGaRespondentOneLip(value);
        return this;
    }

    public GeneralApplicationCaseDataBuilder withIsGaRespondentTwoLip(YesOrNo value) {
        builder.isGaRespondentTwoLip(value);
        return this;
    }

    public GeneralApplicationCaseDataBuilder withIsMultiParty(YesOrNo value) {
        builder.isMultiParty(value);
        return this;
    }

    public GeneralApplicationCaseDataBuilder withApplicantPartyName(String name) {
        builder.applicantPartyName(name);
        return this;
    }

    public GeneralApplicationCaseDataBuilder withClaimant1PartyName(String name) {
        builder.claimant1PartyName(name);
        return this;
    }

    public GeneralApplicationCaseDataBuilder withDefendant1PartyName(String name) {
        builder.defendant1PartyName(name);
        return this;
    }

    public GeneralApplicationCaseDataBuilder withApplicantBilingualPreference(YesOrNo value) {
        builder.applicantBilingualLanguagePreference(value);
        return this;
    }

    public GeneralApplicationCaseDataBuilder withRespondentBilingualPreference(YesOrNo value) {
        builder.respondentBilingualLanguagePreference(value);
        return this;
    }

    public GeneralApplicationCaseDataBuilder withEmailPartyReference(String reference) {
        builder.emailPartyReference(reference);
        return this;
    }

    public GeneralApplicationCaseDataBuilder withJudicialDecision(GAJudicialDecision decision) {
        builder.judicialDecision(decision);
        return this;
    }

    public GeneralApplicationCaseDataBuilder withJudicialDecisionMakeOrder(GAJudicialMakeAnOrder makeAnOrder) {
        builder.judicialDecisionMakeOrder(makeAnOrder);
        return this;
    }

    public GeneralApplicationCaseDataBuilder withJudicialDecisionMakeAnOrderForWrittenRepresentations(
        GAJudicialWrittenRepresentations writtenReps) {
        builder.judicialDecisionMakeAnOrderForWrittenRepresentations(writtenReps);
        return this;
    }

    public GeneralApplicationCaseDataBuilder withAssistedOrderMakeAnOrderForCosts(AssistedOrderCost value) {
        builder.assistedOrderMakeAnOrderForCosts(value);
        return this;
    }

    public GeneralApplicationCaseDataBuilder withAssistedOrderCostsBespoke(BeSpokeCostDetailText value) {
        builder.assistedOrderCostsBespoke(value);
        return this;
    }

    public GeneralApplicationCaseDataBuilder withFinalOrderSelection(FinalOrderSelection selection) {
        builder.finalOrderSelection(selection);
        return this;
    }

    public GeneralApplicationCaseDataBuilder withJudicialDecisionRequestMoreInfo(GAJudicialRequestMoreInfo requestMoreInfo) {
        builder.judicialDecisionRequestMoreInfo(requestMoreInfo);
        return this;
    }

    public GeneralApplicationCaseDataBuilder withGeneralAppApplicantAddlSolicitors(List<Element<GASolicitorDetailsGAspec>> solicitors) {
        builder.generalAppApplicantAddlSolicitors(solicitors);
        return this;
    }

    public GeneralApplicationCaseDataBuilder withGeneralAppParentCaseReference(String parentCaseReference) {
        builder.generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference(parentCaseReference).build());
        return this;
    }

    public GeneralApplicationCaseDataBuilder withGeneralAppNotificationDeadlineDate(LocalDateTime deadline) {
        builder.generalAppNotificationDeadlineDate(deadline);
        return this;
    }

    public GeneralApplicationCaseDataBuilder withGeneralAppSuperClaimType(String superClaimType) {
        builder.generalAppSuperClaimType(superClaimType);
        return this;
    }

    public GeneralApplicationCaseDataBuilder withGeneralAppStatementOfTruth(GAStatementOfTruth statementOfTruth) {
        builder.generalAppStatementOfTruth(statementOfTruth);
        return this;
    }

    public GeneralApplicationCaseDataBuilder withGeneralAppResponseStatementOfTruth(GAStatementOfTruth responseStatementOfTruth) {
        builder.generalAppResponseStatementOfTruth(responseStatementOfTruth);
        return this;
    }

    public GeneralApplicationCaseDataBuilder withGeneralAppVaryJudgementType(YesOrNo value) {
        builder.generalAppVaryJudgementType(value);
        return this;
    }

    public GeneralApplicationCaseDataBuilder withGeneralAppWrittenRepText(String text) {
        builder.generalAppWrittenRepText(text);
        return this;
    }

    public GeneralApplicationCaseDataBuilder withGeneralAppAddlnInfoText(String text) {
        builder.generalAppAddlnInfoText(text);
        return this;
    }

    public GeneralApplicationCaseDataBuilder withGeneralAppRespondReason(String reason) {
        builder.generalAppRespondReason(reason);
        return this;
    }

    public GeneralApplicationCaseDataBuilder withGeneralAppRespondConsentReason(String reason) {
        builder.generalAppRespondConsentReason(reason);
        return this;
    }

    public GeneralApplicationCaseDataBuilder withGeneralAppEvidenceDocument(List<Element<Document>> documents) {
        builder.generalAppEvidenceDocument(documents);
        return this;
    }

    public GeneralApplicationCaseDataBuilder withGeneralAppRespondDocuments(List<Element<Document>> documents) {
        builder.generalAppRespondDocument(documents);
        return this;
    }

    public GeneralApplicationCaseDataBuilder withGeneralAppRespondConsentDocuments(List<Element<Document>> documents) {
        builder.generalAppRespondConsentDocument(documents);
        return this;
    }

    public GeneralApplicationCaseDataBuilder withGeneralAppRespondDebtorDocuments(List<Element<Document>> documents) {
        builder.generalAppRespondDebtorDocument(documents);
        return this;
    }

    public GeneralApplicationCaseDataBuilder withGeneralAppWrittenRepUpload(List<Element<Document>> documents) {
        builder.generalAppWrittenRepUpload(documents);
        return this;
    }

    public GeneralApplicationCaseDataBuilder withGeneralAppDirOrderUpload(List<Document> documents) {
        builder.generalAppDirOrderUpload(documents);
        return this;
    }

    public GeneralApplicationCaseDataBuilder withGeneralAppAddlnInfoUpload(List<Element<Document>> documents) {
        builder.generalAppAddlnInfoUpload(documents);
        return this;
    }

    public GeneralApplicationCaseDataBuilder withGeneralAppHearingDetails(GAHearingDetails details) {
        builder.generalAppHearingDetails(details);
        return this;
    }

    public GeneralApplicationCaseDataBuilder withGeneralAppHearingDate(GAHearingDateGAspec hearingDate) {
        builder.generalAppHearingDate(hearingDate);
        return this;
    }

    public GeneralApplicationCaseDataBuilder withGeneralAppRespondent1Representative(GARespondentRepresentative representative) {
        builder.generalAppRespondent1Representative(representative);
        return this;
    }

    public GeneralApplicationCaseDataBuilder withGaCaseManagementLocation(GACaseLocation location) {
        builder.caseManagementLocation(location);
        return this;
    }

    public GeneralApplicationCaseDataBuilder withCaseManagementCategory(GACaseManagementCategory category) {
        builder.caseManagementCategory(category);
        return this;
    }

    public GeneralApplicationCaseDataBuilder withCaseAccessCategory(CaseCategory category) {
        builder.caseAccessCategory(category);
        return this;
    }

    public GeneralApplicationCaseData build() {
        return builder.build();
    }

    private GASolicitorDetailsGAspec defaultSolicitor(String email) {
        return GASolicitorDetailsGAspec.builder()
            .email(email)
            .id("SOLICITOR_ID")
            .forename("Solicitor")
            .surname(Optional.of("Test"))
            .organisationIdentifier("ORG123")
            .build();
    }
}
