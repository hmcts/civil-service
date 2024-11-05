package uk.gov.hmcts.reform.civil.service.docmosis.dq;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.SecuredDocumentManagementService;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.DQExtraDetailsLip;
import uk.gov.hmcts.reform.civil.model.citizenui.EvidenceConfirmDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.ExpertLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ExpertReportLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.HearingSupportLip;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.DirectionsQuestionnaireForm;
import uk.gov.hmcts.reform.civil.model.dq.RequirementsLip;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.RepresentativeService;
import uk.gov.hmcts.reform.civil.service.docmosis.dq.builders.DQGeneratorFormBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.dq.helpers.GetRespondentsForDQGenerator;
import uk.gov.hmcts.reform.civil.service.docmosis.dq.helpers.RespondentTemplateForDQGenerator;
import uk.gov.hmcts.reform.civil.service.docmosis.dq.helpers.SetApplicantsForDQGenerator;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.flowstate.TransitionsTestConfiguration;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.stateflow.simplegrammar.SimpleStateFlowBuilder;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.reform.civil.enums.dq.SupportRequirements.DISABLED_ACCESS;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    DirectionsQuestionnaireGenerator.class,
    DirectionsQuestionnaireLipGenerator.class,
    JacksonAutoConfiguration.class,
    SimpleStateFlowEngine.class,
    SimpleStateFlowBuilder.class,
    TransitionsTestConfiguration.class,
    DQGeneratorFormBuilder.class,
    GetRespondentsForDQGenerator.class,
    RespondentTemplateForDQGenerator.class,
    SetApplicantsForDQGenerator.class,
    CaseDetailsConverter.class
})
class DirectionsQuestionnaireLipGeneratorTest {

    private static final String BEARER_TOKEN = "Bearer Token";

    @MockBean
    private SecuredDocumentManagementService documentManagementService;

    @MockBean
    private DocumentGeneratorService documentGeneratorService;

    @MockBean
    private RepresentativeService representativeService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private LocationReferenceDataService locationRefDataService;

    @Autowired
    private DirectionsQuestionnaireLipGenerator generator;

    @Test
    void shouldSuccessfullyGenerateRespondentList() {
        //Given
        CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence().build().toBuilder()
            .applicant1(Party.builder()
                .partyEmail("email")
                .companyName("company")
                .type(Party.Type.COMPANY)
                .partyID("0808")
                .primaryAddress(Address.builder().build())
                .build())
            .respondent1(Party.builder()
                .partyEmail("email")
                .companyName("company")
                .type(Party.Type.COMPANY)
                .partyPhone("0808")
                .primaryAddress(Address.builder().build())
                .build())
            .build();
        //When
        DirectionsQuestionnaireForm form = generator.getTemplateData(caseData, BEARER_TOKEN);
        //Then
        assertThat(form.getRespondents().size()).isEqualTo(1);
        assertThat(form.getRespondents().get(0).getName()).isEqualTo(caseData.getRespondent1().getPartyName());
    }

    @Test
    void shouldGenerateLipCorrespondenceAddress_whenItExists() {
        //Given
        Address correspondenceAddress = Address.builder()
            .addressLine1("ds")
            .postCode("SN28AX")
            .build();
        CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence().build().toBuilder()
            .caseDataLiP(CaseDataLiP.builder()
                .respondent1LiPResponse(RespondentLiPResponse.builder()
                    .respondent1LiPCorrespondenceAddress(correspondenceAddress)
                    .build())
                .build())
            .build();
        //When
        DirectionsQuestionnaireForm form = generator.getTemplateData(caseData, BEARER_TOKEN);
        //Then
        assertNotNull(form.getRespondent1LiPCorrespondenceAddress());
        assertThat(form.getRespondent1LiPCorrespondenceAddress().getAddressLine1()).isEqualTo(correspondenceAddress.getAddressLine1());
        assertThat(form.getRespondent1LiPCorrespondenceAddress().getPostCode()).isEqualTo((correspondenceAddress.getPostCode()));
    }

    @Test
    void shouldNotGenerateLipCorrespondenceAddress_whenItDoesNotExist() {
        //Given
        CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence().build();
        //When
        DirectionsQuestionnaireForm form = generator.getTemplateData(caseData, BEARER_TOKEN);
        //Then
        assertNull(form.getRespondent1LiPCorrespondenceAddress());
    }

    @Test
    void shouldGenerateHearingLipRequirements_whenTheyExist() {
        //Given
        CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence().build().toBuilder()
            .caseDataLiP(CaseDataLiP
                .builder()
                .respondent1LiPResponse(
                    RespondentLiPResponse
                        .builder()
                        .respondent1DQHearingSupportLip(
                            HearingSupportLip
                                .builder()
                                .requirementsLip(wrapElements(List.of(
                                    RequirementsLip
                                        .builder()
                                        .name("Name")
                                        .requirements(
                                            List.of(
                                                DISABLED_ACCESS
                                            )
                                        )
                                        .build()
                                )))
                                .build())
                        .build())
                .build())
            .build();
        //When
        DirectionsQuestionnaireForm form = generator.getTemplateData(caseData, BEARER_TOKEN);
        //Then
        assertThat(form.getHearingLipSupportRequirements()).isNotEmpty();
        assertThat(form.getHearingLipSupportRequirements().get(0).getRequirements()).isNotEmpty();
    }

    @Test
    void shouldReturnEmptyList_whenNoHearingLipRequirements() {
        //Given
        CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence().build();
        //When
        DirectionsQuestionnaireForm form = generator.getTemplateData(caseData, BEARER_TOKEN);
        //Then
        assertThat(form.getHearingLipSupportRequirements().isEmpty());
    }

    @Test
    void shouldGenerateLipExtraDetails_whenTheyExist() {
        //Given
        CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence().build().toBuilder()
            .caseDataLiP(CaseDataLiP
                .builder()
                .respondent1LiPResponse(
                    RespondentLiPResponse
                        .builder()
                        .respondent1DQExtraDetails(
                            DQExtraDetailsLip.builder()
                                .requestExtra4weeks(YesOrNo.YES)
                                .build()
                        ).build())
                .build())
            .build();
        //When
        DirectionsQuestionnaireForm form = generator.getTemplateData(caseData, BEARER_TOKEN);
        //Then
        assertNotNull(form.getLipExtraDQ());
    }

    @Test
    void shouldGenerateLipGiveEvidenceYourselfConfirmDetails_whenTheyExist() {
        //Given
        CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence().build().toBuilder()
            .caseDataLiP(CaseDataLiP
                             .builder()
                             .respondent1LiPResponse(
                                 RespondentLiPResponse
                                     .builder()
                                     .respondent1DQExtraDetails(
                                         DQExtraDetailsLip.builder()
                                             .giveEvidenceYourSelf(YesOrNo.YES)
                                             .build())
                                     .respondent1DQEvidenceConfirmDetails(EvidenceConfirmDetails
                                                                              .builder()
                                                                              .firstName("Sam")
                                                                              .lastName("Wise")
                                                                              .phone("07788994455")
                                                                              .email("sam@wise.come")
                                                                              .jobTitle("wise man")
                                                                              .build())
                                     .build())
                             .build())
            .build();
        //When
        DirectionsQuestionnaireForm form = generator.getTemplateData(caseData, BEARER_TOKEN);
        //Then
        assertNotNull(form.getLipExtraDQ().getGiveEvidenceConfirmDetails());
    }

    @Test
    void shouldNotGenerateLipExtraDetails_whenNoExtraDetails() {
        //Given
        CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence().build();
        //When
        DirectionsQuestionnaireForm form = generator.getTemplateData(caseData, BEARER_TOKEN);
        //Then
        assertNull(form.getLipExtraDQ());
    }

    @Test
    void shouldGenerateLipExperts_whenReportExpertsExist() {
        //Given
        CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence().build().toBuilder()
            .caseDataLiP(CaseDataLiP
                .builder()
                .respondent1LiPResponse(
                    RespondentLiPResponse
                        .builder()
                        .respondent1DQExtraDetails(
                            DQExtraDetailsLip.builder()
                                .respondent1DQLiPExpert(
                                    ExpertLiP
                                        .builder()
                                        .caseNeedsAnExpert(YesOrNo.YES)
                                        .expertReportRequired(YesOrNo.YES)
                                        .expertCanStillExamineDetails("details")
                                        .details(
                                            wrapElements(List.of(
                                                ExpertReportLiP.builder()
                                                    .expertName("Name")
                                                    .reportDate(LocalDate.now())
                                                    .build()))
                                        )
                                        .build())
                                .build())
                        .build())
                .build())
            .build();
        //When
        DirectionsQuestionnaireForm form = generator.getTemplateData(caseData, BEARER_TOKEN);
        //Then
        assertNotNull(form.getLipExperts());
        assertThat(form.getLipExperts().getExpertReportRequired()).isEqualTo(YesOrNo.YES);
        assertThat(form.getLipExperts().getCaseNeedsAnExpert()).isEqualTo(YesOrNo.YES);
        assertThat(form.getLipExperts().getDetails()).isNotEmpty();
    }

    @Test
    void shouldGenerateLipExpertsWithEmptyFields_whenReportExpertsDoNotExist() {
        //Given
        CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence().build().toBuilder()
            .caseDataLiP(CaseDataLiP
                .builder()
                .respondent1LiPResponse(
                    RespondentLiPResponse
                        .builder()
                        .respondent1DQExtraDetails(
                            DQExtraDetailsLip.builder().build())
                        .build())
                .build())
            .build();
        //When
        DirectionsQuestionnaireForm form = generator.getTemplateData(caseData, BEARER_TOKEN);
        //Then
        assertThat(form.getLipExperts().getDetails()).isEmpty();
        assertNull(form.getLipExperts().getExpertReportRequired());
        assertNull(form.getLipExperts().getCaseNeedsAnExpert());
    }

    @Test
    void shouldNotGenerateLipExpertsWithEmptyFields_whenNoExtraDetails() {
        //Given
        CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence().build();
        //When
        DirectionsQuestionnaireForm form = generator.getTemplateData(caseData, BEARER_TOKEN);
        //Then
        assertNull(form.getLipExperts());
    }

}
