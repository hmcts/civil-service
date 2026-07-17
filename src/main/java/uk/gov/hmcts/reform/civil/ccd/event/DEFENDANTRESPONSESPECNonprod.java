package uk.gov.hmcts.reform.civil.ccd.event;

import java.util.Set;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECNonprodApplicationsPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECNonprodDefenceAdmittedPartRoutePage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECNonprodDefenceRoutePage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECNonprodDefendant2PartnersAndDependentsPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECNonprodDefendantBankAccountsPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECNonprodDefendantBankAccountsRespondent2Page;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECNonprodDefendantDebtsPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECNonprodDefendantDebtsRespondent2Page;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECNonprodDefendantHomeOptionsPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECNonprodDefendantHomeOptionsRespondent2Page;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECNonprodDefendantIncomeExpensesFullAdmissionPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECNonprodDefendantIncomeExpensesPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECNonprodDefendantIncomeExpensesRespondent2Page;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECNonprodDefendantPartnersAndDependentsPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECNonprodDefendantSelfEmploymentPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECNonprodDefendantSelfEmploymentRespondent2Page;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECNonprodDetailsOfPayingMoneyRepaymentPlanPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECNonprodDetailsOfPayingMoneyRepaymentPlanRespondent2Page;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECNonprodDeterminationWithoutHearingPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECNonprodDisabilityPremiumPaymentsPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECNonprodDisabilityPremiumPaymentsRespondent2Page;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECNonprodDisclosureOfElectronicDocumentsLRspecPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECNonprodDisclosureOfNonElectronicDocumentsLRspecPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECNonprodDisclosureReportPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECNonprodDraftDirectionsPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECNonprodEmploymentDeclarationPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECNonprodEmploymentDeclarationRespondent2Page;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECNonprodExpertsPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECNonprodFileDirectionsQuestionnairePage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECNonprodFinancialDetailsPurpose2Page;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECNonprodFinancialDetailsPurposePage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECNonprodFixedRecoverableCostsIntermediatePage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECNonprodFixedRecoverableCostsPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECNonprodFurtherInformationPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECNonprodHearingLRspecPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECNonprodHearingSupportPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECNonprodHowToAddEmploymentDetailsPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECNonprodHowToAddEmploymentDetailsRespondent2Page;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECNonprodHowToAddTimelineManualPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECNonprodHowToAddTimelinePage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECNonprodHowToAddTimelineUploadPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECNonprodLanguagePage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECNonprodMediationAvailabilityPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECNonprodMediationContactInformationPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECNonprodMediationPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECNonprodRepaymentPlanPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECNonprodRepaymentPlanRespondent2Page;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECNonprodRequestedCourtLocationLRspecPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECNonprodRespondentCheckListPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECNonprodRespondentResponseTypeSpec2v1Page;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECNonprodRespondentResponseTypeSpecPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECNonprodResponseConfirmDetailsPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECNonprodResponseConfirmNameAddressPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECNonprodSingleResponse2v1Page;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECNonprodSingleResponsePage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECNonprodSmaillClaimHearingPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECNonprodSmallClaimExpertsPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECNonprodSmallClaimWitnessesPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECNonprodStatementOfTruthPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECNonprodUploadPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECNonprodVulnerabilityQuestionsPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECNonprodWhenWillClaimBePaidPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECNonprodWhyDoesNotPayImmediatelyPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECNonprodWhyDoesNotPayImmediatelyRespondent2Page;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECNonprodWitnessesPage;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.EnvironmentFlags;
import uk.gov.hmcts.reform.civil.model.UserRole;

/**
 * Generated by ccd-definition-converter from CIVIL on migration; owned by this service team.
 *
 * <p>Event {@code DEFENDANT_RESPONSE_SPEC} for case type {@code CIVIL}.
 */
@Component
public class DEFENDANTRESPONSESPECNonprod implements CCDConfig<CaseData, CaseState, UserRole> {
    /**
     * The CCD event ID.
     */
    public static final String DEFENDANT_RESPONSE_SPEC = "DEFENDANT_RESPONSE_SPEC";

    /**
     * Registers overlay event {@code DEFENDANT_RESPONSE_SPEC} (active when CCD_DEF_ENV=prod).
     *
     * @param builder the config builder
     */
    @Override
    public void configure(ConfigBuilder<CaseData, CaseState, UserRole> builder) {
        if (EnvironmentFlags.flag("CCD_DEF_ENV", "prod")) return;
        var fields = builder.event(DEFENDANT_RESPONSE_SPEC)
            .forStates(CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT, CaseState.JUDGMENT_REQUESTED)
            .name("Respond to claim")
            .description("Defendant response to Specified claim")
            .displayOrder(6)
            .showSummary()
            .endButtonLabel("Submit")
            .explicitGrants()
            .grant(Set.of(Permission.R), UserRole.APP_SOL_SPEC_PROFILE, UserRole.CASEWORKER_CIVIL_ADMIN, UserRole.CASEWORKER_CIVIL_STAFF, UserRole.CITIZEN_CLAIMANT_PROFILE, UserRole.JUDGE_PROFILE, UserRole.LEGAL_ADVISER)
            .grant(Permission.CRU, UserRole.RES_SOL_ONE_SPEC_PROFILE, UserRole.RES_SOL_TWO_SPEC_PROFILE)
            .fields();
        DEFENDANTRESPONSESPECNonprodRespondentCheckListPage.apply(fields);
        DEFENDANTRESPONSESPECNonprodDefenceAdmittedPartRoutePage.apply(fields);
        DEFENDANTRESPONSESPECNonprodWhenWillClaimBePaidPage.apply(fields);
        DEFENDANTRESPONSESPECNonprodRepaymentPlanPage.apply(fields);
        DEFENDANTRESPONSESPECNonprodRepaymentPlanRespondent2Page.apply(fields);
        DEFENDANTRESPONSESPECNonprodFinancialDetailsPurposePage.apply(fields);
        DEFENDANTRESPONSESPECNonprodFinancialDetailsPurpose2Page.apply(fields);
        DEFENDANTRESPONSESPECNonprodDefendantBankAccountsPage.apply(fields);
        DEFENDANTRESPONSESPECNonprodDefendantBankAccountsRespondent2Page.apply(fields);
        DEFENDANTRESPONSESPECNonprodDisabilityPremiumPaymentsPage.apply(fields);
        DEFENDANTRESPONSESPECNonprodDisabilityPremiumPaymentsRespondent2Page.apply(fields);
        DEFENDANTRESPONSESPECNonprodDefendantHomeOptionsPage.apply(fields);
        DEFENDANTRESPONSESPECNonprodDefendantHomeOptionsRespondent2Page.apply(fields);
        DEFENDANTRESPONSESPECNonprodDefendantPartnersAndDependentsPage.apply(fields);
        DEFENDANTRESPONSESPECNonprodDefendant2PartnersAndDependentsPage.apply(fields);
        DEFENDANTRESPONSESPECNonprodEmploymentDeclarationPage.apply(fields);
        DEFENDANTRESPONSESPECNonprodEmploymentDeclarationRespondent2Page.apply(fields);
        DEFENDANTRESPONSESPECNonprodHowToAddEmploymentDetailsPage.apply(fields);
        DEFENDANTRESPONSESPECNonprodHowToAddEmploymentDetailsRespondent2Page.apply(fields);
        DEFENDANTRESPONSESPECNonprodDefendantSelfEmploymentPage.apply(fields);
        DEFENDANTRESPONSESPECNonprodDefendantSelfEmploymentRespondent2Page.apply(fields);
        DEFENDANTRESPONSESPECNonprodDetailsOfPayingMoneyRepaymentPlanPage.apply(fields);
        DEFENDANTRESPONSESPECNonprodDetailsOfPayingMoneyRepaymentPlanRespondent2Page.apply(fields);
        DEFENDANTRESPONSESPECNonprodDefendantDebtsPage.apply(fields);
        DEFENDANTRESPONSESPECNonprodDefendantDebtsRespondent2Page.apply(fields);
        DEFENDANTRESPONSESPECNonprodDefendantIncomeExpensesPage.apply(fields);
        DEFENDANTRESPONSESPECNonprodDefendantIncomeExpensesFullAdmissionPage.apply(fields);
        DEFENDANTRESPONSESPECNonprodDefendantIncomeExpensesRespondent2Page.apply(fields);
        DEFENDANTRESPONSESPECNonprodWhyDoesNotPayImmediatelyPage.apply(fields);
        DEFENDANTRESPONSESPECNonprodWhyDoesNotPayImmediatelyRespondent2Page.apply(fields);
        DEFENDANTRESPONSESPECNonprodResponseConfirmNameAddressPage.apply(fields);
        DEFENDANTRESPONSESPECNonprodResponseConfirmDetailsPage.apply(fields);
        DEFENDANTRESPONSESPECNonprodSingleResponse2v1Page.apply(fields);
        DEFENDANTRESPONSESPECNonprodSingleResponsePage.apply(fields);
        DEFENDANTRESPONSESPECNonprodRespondentResponseTypeSpec2v1Page.apply(fields);
        DEFENDANTRESPONSESPECNonprodRespondentResponseTypeSpecPage.apply(fields);
        DEFENDANTRESPONSESPECNonprodDefenceRoutePage.apply(fields);
        DEFENDANTRESPONSESPECNonprodUploadPage.apply(fields);
        DEFENDANTRESPONSESPECNonprodHowToAddTimelinePage.apply(fields);
        DEFENDANTRESPONSESPECNonprodHowToAddTimelineUploadPage.apply(fields);
        DEFENDANTRESPONSESPECNonprodHowToAddTimelineManualPage.apply(fields);
        DEFENDANTRESPONSESPECNonprodFurtherInformationPage.apply(fields);
        DEFENDANTRESPONSESPECNonprodMediationContactInformationPage.apply(fields);
        DEFENDANTRESPONSESPECNonprodMediationAvailabilityPage.apply(fields);
        DEFENDANTRESPONSESPECNonprodMediationPage.apply(fields);
        DEFENDANTRESPONSESPECNonprodFileDirectionsQuestionnairePage.apply(fields);
        DEFENDANTRESPONSESPECNonprodFixedRecoverableCostsPage.apply(fields);
        DEFENDANTRESPONSESPECNonprodDisclosureOfElectronicDocumentsLRspecPage.apply(fields);
        DEFENDANTRESPONSESPECNonprodDisclosureOfNonElectronicDocumentsLRspecPage.apply(fields);
        DEFENDANTRESPONSESPECNonprodDisclosureReportPage.apply(fields);
        DEFENDANTRESPONSESPECNonprodExpertsPage.apply(fields);
        DEFENDANTRESPONSESPECNonprodDeterminationWithoutHearingPage.apply(fields);
        DEFENDANTRESPONSESPECNonprodSmallClaimExpertsPage.apply(fields);
        DEFENDANTRESPONSESPECNonprodSmallClaimWitnessesPage.apply(fields);
        DEFENDANTRESPONSESPECNonprodWitnessesPage.apply(fields);
        DEFENDANTRESPONSESPECNonprodLanguagePage.apply(fields);
        DEFENDANTRESPONSESPECNonprodSmaillClaimHearingPage.apply(fields);
        DEFENDANTRESPONSESPECNonprodHearingLRspecPage.apply(fields);
        DEFENDANTRESPONSESPECNonprodRequestedCourtLocationLRspecPage.apply(fields);
        DEFENDANTRESPONSESPECNonprodHearingSupportPage.apply(fields);
        DEFENDANTRESPONSESPECNonprodVulnerabilityQuestionsPage.apply(fields);
        DEFENDANTRESPONSESPECNonprodApplicationsPage.apply(fields);
        DEFENDANTRESPONSESPECNonprodStatementOfTruthPage.apply(fields);
        DEFENDANTRESPONSESPECNonprodFixedRecoverableCostsIntermediatePage.apply(fields);
        DEFENDANTRESPONSESPECNonprodDraftDirectionsPage.apply(fields);
    }
}
