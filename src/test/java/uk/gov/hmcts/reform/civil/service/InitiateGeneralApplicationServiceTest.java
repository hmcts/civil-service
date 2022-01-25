package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.documents.Document;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;
import uk.gov.hmcts.reform.civil.model.genapplication.GAPbaDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GARespondentOrderAgreement;
import uk.gov.hmcts.reform.civil.model.genapplication.GAStatementOfTruth;
import uk.gov.hmcts.reform.civil.model.genapplication.GAUrgencyRequirement;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDate;

import static java.time.LocalDate.EPOCH;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.dq.GAHearingDuration.HOUR_1;
import static uk.gov.hmcts.reform.civil.enums.dq.GAHearingSupportRequirements.OTHER_SUPPORT;
import static uk.gov.hmcts.reform.civil.enums.dq.GAHearingType.IN_PERSON;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.EXTEND_TIME;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@SpringBootTest(classes = {
    InitiateGeneralApplicationService.class,
    JacksonAutoConfiguration.class,
})
class InitiateGeneralApplicationServiceTest {

    private static final String STRING_CONSTANT = "this is a string";
    private static final String STRING_NUM_CONSTANT = "123456789";
    private static final LocalDate APP_DATE_EPOCH = EPOCH;
    private static final DynamicList PBALIST = DynamicList.builder().build();

    @Autowired
    private InitiateGeneralApplicationService service;

    private CaseData getTestCaseDataWithEmptyCollectionOfApps(CaseData caseData) {
        return caseData.toBuilder()
            .generalAppType(GAApplicationType.builder()
                                .types(singletonList(EXTEND_TIME))
                                .build())
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder()
                                               .hasAgreed(NO)
                                               .build())
            .generalAppPBADetails(GAPbaDetails.builder()
                                      .applicantsPbaAccounts(PBALIST)
                                      .pbaReference(STRING_CONSTANT)
                                      .build())
            .generalAppDetailsOfOrder(STRING_CONSTANT)
            .generalAppReasonsOfOrder(STRING_CONSTANT)
            .generalAppInformOtherParty(GAInformOtherParty.builder()
                                            .isWithNotice(NO)
                                            .reasonsForWithoutNotice(STRING_CONSTANT)
                                            .build())
            .generalAppUrgencyRequirement(GAUrgencyRequirement.builder()
                                              .generalAppUrgency(YES)
                                              .reasonsForUrgency(STRING_CONSTANT)
                                              .urgentAppConsiderationDate(APP_DATE_EPOCH)
                                              .build())
            .generalAppStatementOfTruth(GAStatementOfTruth.builder()
                                            .name(STRING_CONSTANT)
                                            .role(STRING_CONSTANT)
                                            .build())
            .generalAppEvidenceDocument(wrapElements(Document.builder()
                                                   .documentUrl(STRING_CONSTANT)
                                                   .documentBinaryUrl(STRING_CONSTANT)
                                                   .documentFileName(STRING_CONSTANT)
                                                   .documentHash(STRING_CONSTANT)
                                                   .build()))
            .generalAppHearingDetails(GAHearingDetails.builder()
                                          .judgeName(STRING_CONSTANT)
                                          .hearingDate(APP_DATE_EPOCH)
                                          .trialDateFrom(APP_DATE_EPOCH)
                                          .trialDateTo(APP_DATE_EPOCH)
                                          .hearingYesorNo(YES)
                                          .hearingDuration(HOUR_1)
                                          .supportRequirement(singletonList(OTHER_SUPPORT))
                                          .judgeRequiredYesOrNo(YES)
                                          .trialRequiredYesOrNo(YES)
                                          .hearingDetailsEmailID(STRING_CONSTANT)
                                          .unavailableTrailDateTo(APP_DATE_EPOCH)
                                          .supportRequirementOther(STRING_CONSTANT)
                                          .hearingPreferredLocation(DynamicList.builder().build())
                                          .unavailableTrailDateFrom(APP_DATE_EPOCH)
                                          .hearingDetailsTelephoneNumber(STRING_NUM_CONSTANT)
                                          .reasonForPreferredHearingType(STRING_CONSTANT)
                                          .telephoneHearingPreferredType(STRING_CONSTANT)
                                          .supportRequirementSignLanguage(STRING_CONSTANT)
                                          .hearingPreferencesPreferredType(IN_PERSON)
                                          .unavailableTrailRequiredYesOrNo(YES)
                                          .supportRequirementLanguageInterpreter(STRING_CONSTANT)
                                          .build())
            .build();
    }

    private CaseData getTestCaseDataCollectionOfApps(CaseData caseData) {
        GeneralApplication application = GeneralApplication.builder()
                .generalAppType(GAApplicationType.builder()
                        .types(singletonList(EXTEND_TIME))
                        .build())
                .generalAppRespondentAgreement(GARespondentOrderAgreement.builder()
                        .hasAgreed(NO)
                        .build())
                .generalAppPBADetails(GAPbaDetails.builder()
                        .applicantsPbaAccounts(PBALIST)
                        .pbaReference(STRING_CONSTANT)
                        .build())
                .generalAppDetailsOfOrder(STRING_CONSTANT)
                .generalAppReasonsOfOrder(STRING_CONSTANT)
                .generalAppInformOtherParty(GAInformOtherParty.builder()
                        .isWithNotice(NO)
                        .reasonsForWithoutNotice(STRING_CONSTANT)
                        .build())
                .generalAppUrgencyRequirement(GAUrgencyRequirement.builder()
                        .generalAppUrgency(YES)
                        .reasonsForUrgency(STRING_CONSTANT)
                        .urgentAppConsiderationDate(APP_DATE_EPOCH)
                        .build())
                .generalAppStatementOfTruth(GAStatementOfTruth.builder()
                        .name(STRING_CONSTANT)
                        .role(STRING_CONSTANT)
                        .build())
                .generalAppEvidenceDocument(wrapElements(Document.builder()
                                .documentUrl(STRING_CONSTANT)
                                .documentBinaryUrl(STRING_CONSTANT)
                                .documentFileName(STRING_CONSTANT)
                                .documentHash(STRING_CONSTANT)
                                .build()))
                .generalAppHearingDetails(GAHearingDetails.builder()
                        .judgeName(STRING_CONSTANT)
                        .hearingDate(APP_DATE_EPOCH)
                        .trialDateFrom(APP_DATE_EPOCH)
                        .trialDateTo(APP_DATE_EPOCH)
                        .hearingYesorNo(YES)
                        .hearingDuration(HOUR_1)
                        .supportRequirement(singletonList(OTHER_SUPPORT))
                        .judgeRequiredYesOrNo(YES)
                        .trialRequiredYesOrNo(YES)
                        .hearingDetailsEmailID(STRING_CONSTANT)
                        .unavailableTrailDateTo(APP_DATE_EPOCH)
                        .supportRequirementOther(STRING_CONSTANT)
                        .hearingPreferredLocation(DynamicList.builder().build())
                        .unavailableTrailDateFrom(APP_DATE_EPOCH)
                        .hearingDetailsTelephoneNumber(STRING_NUM_CONSTANT)
                        .reasonForPreferredHearingType(STRING_CONSTANT)
                        .telephoneHearingPreferredType(STRING_CONSTANT)
                        .supportRequirementSignLanguage(STRING_CONSTANT)
                        .hearingPreferencesPreferredType(IN_PERSON)
                        .unavailableTrailRequiredYesOrNo(YES)
                        .supportRequirementLanguageInterpreter(STRING_CONSTANT)
                        .build())
                .build();
        return getTestCaseDataWithEmptyCollectionOfApps(caseData)
                .toBuilder()
                .generalApplications(wrapElements(application))
                .build();
    }

    @Test
    void shouldReturnCaseDataPopulated_whenValidApplicationIsBeingInitiated() {
        CaseData caseData = getTestCaseDataWithEmptyCollectionOfApps(CaseDataBuilder.builder().build());

        CaseData result = service.buildCaseData(caseData.toBuilder(), caseData);

        assertCollectionPopulated(result);
        assertCaseDateEntries(result);
    }

    @Test
    void shouldReturnCaseDataWithAdditionToCollection_whenAnotherApplicationIsBeingInitiated() {
        CaseData caseData = getTestCaseDataCollectionOfApps(CaseDataBuilder.builder().build());

        CaseData result = service.buildCaseData(caseData.toBuilder(), caseData);

        assertThat(result.getGeneralApplications().size()).isEqualTo(2);
    }

    private void assertCaseDateEntries(CaseData caseData) {
        assertThat(caseData.getGeneralAppType().getTypes()).isNull();
        assertThat(caseData.getGeneralAppRespondentAgreement().getHasAgreed()).isNull();
        assertThat(caseData.getGeneralAppPBADetails().getApplicantsPbaAccounts()).isNull();
        assertThat(caseData.getGeneralAppPBADetails().getPbaReference()).isNull();
        assertThat(caseData.getGeneralAppDetailsOfOrder()).isEmpty();
        assertThat(caseData.getGeneralAppReasonsOfOrder()).isEmpty();
        assertThat(caseData.getGeneralAppInformOtherParty().getIsWithNotice()).isNull();
        assertThat(caseData.getGeneralAppInformOtherParty().getReasonsForWithoutNotice()).isNull();
        assertThat(caseData.getGeneralAppUrgencyRequirement().getGeneralAppUrgency()).isNull();
        assertThat(caseData.getGeneralAppUrgencyRequirement().getReasonsForUrgency()).isNull();
        assertThat(caseData.getGeneralAppUrgencyRequirement().getUrgentAppConsiderationDate()).isNull();
        assertThat(caseData.getGeneralAppStatementOfTruth().getName()).isNull();
        assertThat(caseData.getGeneralAppStatementOfTruth().getRole()).isNull();
        assertThat(unwrapElements(caseData.getGeneralAppEvidenceDocument())).isEmpty();
        GAHearingDetails generalAppHearingDetails = caseData.getGeneralAppHearingDetails();
        assertThat(generalAppHearingDetails.getJudgeName()).isNull();
        assertThat(generalAppHearingDetails.getHearingDate()).isNull();
        assertThat(generalAppHearingDetails.getTrialDateFrom()).isNull();
        assertThat(generalAppHearingDetails.getTrialDateTo()).isNull();
        assertThat(generalAppHearingDetails.getHearingYesorNo()).isNull();
        assertThat(generalAppHearingDetails.getHearingDuration()).isNull();
        assertThat(generalAppHearingDetails.getSupportRequirement()).isNull();
        assertThat(generalAppHearingDetails.getJudgeRequiredYesOrNo()).isNull();
        assertThat(generalAppHearingDetails.getTrialRequiredYesOrNo()).isNull();
        assertThat(generalAppHearingDetails.getHearingDetailsEmailID()).isNull();
        assertThat(generalAppHearingDetails.getUnavailableTrailDateTo()).isNull();
        assertThat(generalAppHearingDetails.getUnavailableTrailDateFrom()).isNull();
        assertThat(generalAppHearingDetails.getSupportRequirementOther()).isNull();
        assertThat(generalAppHearingDetails.getHearingDetailsTelephoneNumber()).isNull();
        assertThat(generalAppHearingDetails.getReasonForPreferredHearingType()).isNull();
        assertThat(generalAppHearingDetails.getTelephoneHearingPreferredType()).isNull();
        assertThat(generalAppHearingDetails.getSupportRequirementSignLanguage()).isNull();
        assertThat(generalAppHearingDetails.getHearingPreferencesPreferredType()).isNull();
        assertThat(generalAppHearingDetails.getUnavailableTrailRequiredYesOrNo()).isNull();
        assertThat(generalAppHearingDetails.getSupportRequirementLanguageInterpreter()).isNull();
    }

    private void assertCollectionPopulated(CaseData caseData) {
        assertThat(unwrapElements(caseData.getGeneralApplications()).size()).isEqualTo(1);
        GeneralApplication application = unwrapElements(caseData.getGeneralApplications()).get(0);

        assertThat(application.getGeneralAppType().getTypes().contains(EXTEND_TIME)).isTrue();
        assertThat(application.getGeneralAppRespondentAgreement().getHasAgreed()).isEqualTo(NO);
        assertThat(application.getGeneralAppPBADetails().getApplicantsPbaAccounts())
            .isEqualTo(PBALIST);
        assertThat(application.getGeneralAppPBADetails().getPbaReference())
            .isEqualTo(STRING_CONSTANT);
        assertThat(application.getGeneralAppDetailsOfOrder()).isEqualTo(STRING_CONSTANT);
        assertThat(application.getGeneralAppReasonsOfOrder()).isEqualTo(STRING_CONSTANT);
        assertThat(application.getGeneralAppInformOtherParty().getIsWithNotice())
            .isEqualTo(NO);
        assertThat(application.getGeneralAppInformOtherParty().getReasonsForWithoutNotice())
            .isEqualTo(STRING_CONSTANT);
        assertThat(application.getGeneralAppUrgencyRequirement().getGeneralAppUrgency())
            .isEqualTo(YES);
        assertThat(application.getGeneralAppUrgencyRequirement().getReasonsForUrgency())
            .isEqualTo(STRING_CONSTANT);
        assertThat(application.getGeneralAppUrgencyRequirement().getUrgentAppConsiderationDate())
            .isEqualTo(APP_DATE_EPOCH);
        assertThat(application.getGeneralAppStatementOfTruth().getName()).isEqualTo(STRING_CONSTANT);
        assertThat(application.getGeneralAppStatementOfTruth().getRole()).isEqualTo(STRING_CONSTANT);
        assertThat(unwrapElements(application.getGeneralAppEvidenceDocument()).get(0).getDocumentUrl())
            .isEqualTo(STRING_CONSTANT);
        assertThat(unwrapElements(application.getGeneralAppEvidenceDocument()).get(0).getDocumentHash())
            .isEqualTo(STRING_CONSTANT);
        assertThat(unwrapElements(application.getGeneralAppEvidenceDocument()).get(0).getDocumentBinaryUrl())
            .isEqualTo(STRING_CONSTANT);
        assertThat(unwrapElements(application.getGeneralAppEvidenceDocument()).get(0).getDocumentFileName())
            .isEqualTo(STRING_CONSTANT);
        GAHearingDetails generalAppHearingDetails = application.getGeneralAppHearingDetails();
        assertThat(generalAppHearingDetails.getJudgeName()).isEqualTo(STRING_CONSTANT);
        assertThat(generalAppHearingDetails.getHearingDate()).isEqualTo(APP_DATE_EPOCH);
        assertThat(generalAppHearingDetails.getTrialDateFrom()).isEqualTo(APP_DATE_EPOCH);
        assertThat(generalAppHearingDetails.getTrialDateTo()).isEqualTo(APP_DATE_EPOCH);
        assertThat(generalAppHearingDetails.getHearingYesorNo()).isEqualTo(YES);
        assertThat(generalAppHearingDetails.getHearingDuration()).isEqualTo(HOUR_1);
        assertThat(generalAppHearingDetails.getSupportRequirement()
                       .contains(OTHER_SUPPORT)).isTrue();
        assertThat(generalAppHearingDetails.getJudgeRequiredYesOrNo()).isEqualTo(YES);
        assertThat(generalAppHearingDetails.getTrialRequiredYesOrNo()).isEqualTo(YES);
        assertThat(generalAppHearingDetails.getHearingDetailsEmailID()).isEqualTo(STRING_CONSTANT);
        assertThat(generalAppHearingDetails.getUnavailableTrailDateTo()).isEqualTo(APP_DATE_EPOCH);
        assertThat(generalAppHearingDetails.getUnavailableTrailDateFrom()).isEqualTo(APP_DATE_EPOCH);
        assertThat(generalAppHearingDetails.getSupportRequirementOther()).isEqualTo(STRING_CONSTANT);
        assertThat(generalAppHearingDetails.getHearingDetailsTelephoneNumber())
                .isEqualTo(STRING_NUM_CONSTANT);
        assertThat(generalAppHearingDetails.getReasonForPreferredHearingType()).isEqualTo(STRING_CONSTANT);
        assertThat(generalAppHearingDetails.getTelephoneHearingPreferredType()).isEqualTo(STRING_CONSTANT);
        assertThat(generalAppHearingDetails.getSupportRequirementSignLanguage()).isEqualTo(STRING_CONSTANT);
        assertThat(generalAppHearingDetails.getHearingPreferencesPreferredType())
            .isEqualTo(IN_PERSON);
        assertThat(generalAppHearingDetails.getUnavailableTrailRequiredYesOrNo()).isEqualTo(YES);
        assertThat(generalAppHearingDetails.getSupportRequirementLanguageInterpreter()).isEqualTo(STRING_CONSTANT);
        assertThat(application.getIsMultiParty()).isEqualTo(NO);
    }
}

