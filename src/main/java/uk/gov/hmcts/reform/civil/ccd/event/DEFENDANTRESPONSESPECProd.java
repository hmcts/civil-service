package uk.gov.hmcts.reform.civil.ccd.event;

import java.util.Set;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECProdApplicationsPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECProdDefenceAdmittedPartRoutePage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECProdDefenceRoutePage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECProdDefendant2PartnersAndDependentsPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECProdDefendantBankAccountsPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECProdDefendantBankAccountsRespondent2Page;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECProdDefendantDebtsPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECProdDefendantDebtsRespondent2Page;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECProdDefendantHomeOptionsPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECProdDefendantHomeOptionsRespondent2Page;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECProdDefendantIncomeExpensesFullAdmissionPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECProdDefendantIncomeExpensesPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECProdDefendantIncomeExpensesRespondent2Page;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECProdDefendantPartnersAndDependentsPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECProdDefendantSelfEmploymentPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECProdDefendantSelfEmploymentRespondent2Page;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECProdDetailsOfPayingMoneyRepaymentPlanPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECProdDetailsOfPayingMoneyRepaymentPlanRespondent2Page;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECProdDeterminationWithoutHearingPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECProdDisabilityPremiumPaymentsPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECProdDisabilityPremiumPaymentsRespondent2Page;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECProdDisclosureOfElectronicDocumentsLRspecPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECProdDisclosureOfNonElectronicDocumentsLRspecPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECProdDisclosureReportPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECProdDraftDirectionsPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECProdEmploymentDeclarationPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECProdEmploymentDeclarationRespondent2Page;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECProdExpertsPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECProdFileDirectionsQuestionnairePage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECProdFinancialDetailsPurpose2Page;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECProdFinancialDetailsPurposePage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECProdFixedRecoverableCostsIntermediatePage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECProdFixedRecoverableCostsPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECProdFurtherInformationPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECProdHearingLRspecPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECProdHearingSupportPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECProdHowToAddEmploymentDetailsPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECProdHowToAddEmploymentDetailsRespondent2Page;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECProdHowToAddTimelineManualPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECProdHowToAddTimelinePage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECProdHowToAddTimelineUploadPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECProdLanguagePage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECProdMediationAvailabilityPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECProdMediationContactInformationPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECProdMediationPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECProdRepaymentPlanPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECProdRepaymentPlanRespondent2Page;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECProdRequestedCourtLocationLRspecPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECProdRespondentCheckListPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECProdRespondentResponseTypeSpec2v1Page;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECProdRespondentResponseTypeSpecPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECProdResponseConfirmDetailsPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECProdResponseConfirmNameAddressPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECProdSingleResponse2v1Page;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECProdSingleResponsePage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECProdSmaillClaimHearingPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECProdSmallClaimExpertsPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECProdSmallClaimWitnessesPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECProdStatementOfTruthPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECProdUploadPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECProdVulnerabilityQuestionsPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECProdWhenWillClaimBePaidPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECProdWhyDoesNotPayImmediatelyPage;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECProdWhyDoesNotPayImmediatelyRespondent2Page;
import uk.gov.hmcts.reform.civil.ccd.event.page.DEFENDANTRESPONSESPECProdWitnessesPage;
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
public class DEFENDANTRESPONSESPECProd implements CCDConfig<CaseData, CaseState, UserRole> {
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
        if (!EnvironmentFlags.flag("CCD_DEF_ENV", "prod")) return;
        var fields = builder.event(DEFENDANT_RESPONSE_SPEC)
            .forState(CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT)
            .name("Respond to claim")
            .description("Defendant response to Specified claim")
            .displayOrder(6)
            .showSummary()
            .endButtonLabel("Submit")
            .explicitGrants()
            .grant(Set.of(Permission.R), UserRole.APP_SOL_SPEC_PROFILE, UserRole.CASEWORKER_CIVIL_ADMIN, UserRole.CASEWORKER_CIVIL_STAFF, UserRole.CITIZEN_CLAIMANT_PROFILE, UserRole.JUDGE_PROFILE, UserRole.LEGAL_ADVISER)
            .grant(Permission.CRU, UserRole.RES_SOL_ONE_SPEC_PROFILE, UserRole.RES_SOL_TWO_SPEC_PROFILE)
            .fields();
        DEFENDANTRESPONSESPECProdRespondentCheckListPage.apply(fields);
        DEFENDANTRESPONSESPECProdDefenceAdmittedPartRoutePage.apply(fields);
        DEFENDANTRESPONSESPECProdWhenWillClaimBePaidPage.apply(fields);
        DEFENDANTRESPONSESPECProdRepaymentPlanPage.apply(fields);
        DEFENDANTRESPONSESPECProdRepaymentPlanRespondent2Page.apply(fields);
        DEFENDANTRESPONSESPECProdFinancialDetailsPurposePage.apply(fields);
        DEFENDANTRESPONSESPECProdFinancialDetailsPurpose2Page.apply(fields);
        DEFENDANTRESPONSESPECProdDefendantBankAccountsPage.apply(fields);
        DEFENDANTRESPONSESPECProdDefendantBankAccountsRespondent2Page.apply(fields);
        DEFENDANTRESPONSESPECProdDisabilityPremiumPaymentsPage.apply(fields);
        DEFENDANTRESPONSESPECProdDisabilityPremiumPaymentsRespondent2Page.apply(fields);
        DEFENDANTRESPONSESPECProdDefendantHomeOptionsPage.apply(fields);
        DEFENDANTRESPONSESPECProdDefendantHomeOptionsRespondent2Page.apply(fields);
        DEFENDANTRESPONSESPECProdDefendantPartnersAndDependentsPage.apply(fields);
        DEFENDANTRESPONSESPECProdDefendant2PartnersAndDependentsPage.apply(fields);
        DEFENDANTRESPONSESPECProdEmploymentDeclarationPage.apply(fields);
        DEFENDANTRESPONSESPECProdEmploymentDeclarationRespondent2Page.apply(fields);
        DEFENDANTRESPONSESPECProdHowToAddEmploymentDetailsPage.apply(fields);
        DEFENDANTRESPONSESPECProdHowToAddEmploymentDetailsRespondent2Page.apply(fields);
        DEFENDANTRESPONSESPECProdDefendantSelfEmploymentPage.apply(fields);
        DEFENDANTRESPONSESPECProdDefendantSelfEmploymentRespondent2Page.apply(fields);
        DEFENDANTRESPONSESPECProdDetailsOfPayingMoneyRepaymentPlanPage.apply(fields);
        DEFENDANTRESPONSESPECProdDetailsOfPayingMoneyRepaymentPlanRespondent2Page.apply(fields);
        DEFENDANTRESPONSESPECProdDefendantDebtsPage.apply(fields);
        DEFENDANTRESPONSESPECProdDefendantDebtsRespondent2Page.apply(fields);
        DEFENDANTRESPONSESPECProdDefendantIncomeExpensesPage.apply(fields);
        DEFENDANTRESPONSESPECProdDefendantIncomeExpensesFullAdmissionPage.apply(fields);
        DEFENDANTRESPONSESPECProdDefendantIncomeExpensesRespondent2Page.apply(fields);
        DEFENDANTRESPONSESPECProdWhyDoesNotPayImmediatelyPage.apply(fields);
        DEFENDANTRESPONSESPECProdWhyDoesNotPayImmediatelyRespondent2Page.apply(fields);
        DEFENDANTRESPONSESPECProdResponseConfirmNameAddressPage.apply(fields);
        DEFENDANTRESPONSESPECProdResponseConfirmDetailsPage.apply(fields);
        DEFENDANTRESPONSESPECProdSingleResponse2v1Page.apply(fields);
        DEFENDANTRESPONSESPECProdSingleResponsePage.apply(fields);
        DEFENDANTRESPONSESPECProdRespondentResponseTypeSpec2v1Page.apply(fields);
        DEFENDANTRESPONSESPECProdRespondentResponseTypeSpecPage.apply(fields);
        DEFENDANTRESPONSESPECProdDefenceRoutePage.apply(fields);
        DEFENDANTRESPONSESPECProdUploadPage.apply(fields);
        DEFENDANTRESPONSESPECProdHowToAddTimelinePage.apply(fields);
        DEFENDANTRESPONSESPECProdHowToAddTimelineUploadPage.apply(fields);
        DEFENDANTRESPONSESPECProdHowToAddTimelineManualPage.apply(fields);
        DEFENDANTRESPONSESPECProdFurtherInformationPage.apply(fields);
        DEFENDANTRESPONSESPECProdMediationContactInformationPage.apply(fields);
        DEFENDANTRESPONSESPECProdMediationAvailabilityPage.apply(fields);
        DEFENDANTRESPONSESPECProdMediationPage.apply(fields);
        DEFENDANTRESPONSESPECProdFileDirectionsQuestionnairePage.apply(fields);
        DEFENDANTRESPONSESPECProdFixedRecoverableCostsPage.apply(fields);
        DEFENDANTRESPONSESPECProdDisclosureOfElectronicDocumentsLRspecPage.apply(fields);
        DEFENDANTRESPONSESPECProdDisclosureOfNonElectronicDocumentsLRspecPage.apply(fields);
        DEFENDANTRESPONSESPECProdDisclosureReportPage.apply(fields);
        DEFENDANTRESPONSESPECProdExpertsPage.apply(fields);
        DEFENDANTRESPONSESPECProdDeterminationWithoutHearingPage.apply(fields);
        DEFENDANTRESPONSESPECProdSmallClaimExpertsPage.apply(fields);
        DEFENDANTRESPONSESPECProdSmallClaimWitnessesPage.apply(fields);
        DEFENDANTRESPONSESPECProdWitnessesPage.apply(fields);
        DEFENDANTRESPONSESPECProdLanguagePage.apply(fields);
        DEFENDANTRESPONSESPECProdSmaillClaimHearingPage.apply(fields);
        DEFENDANTRESPONSESPECProdHearingLRspecPage.apply(fields);
        DEFENDANTRESPONSESPECProdRequestedCourtLocationLRspecPage.apply(fields);
        DEFENDANTRESPONSESPECProdHearingSupportPage.apply(fields);
        DEFENDANTRESPONSESPECProdVulnerabilityQuestionsPage.apply(fields);
        DEFENDANTRESPONSESPECProdApplicationsPage.apply(fields);
        DEFENDANTRESPONSESPECProdStatementOfTruthPage.apply(fields);
        DEFENDANTRESPONSESPECProdFixedRecoverableCostsIntermediatePage.apply(fields);
        DEFENDANTRESPONSESPECProdDraftDirectionsPage.apply(fields);
    }
}
