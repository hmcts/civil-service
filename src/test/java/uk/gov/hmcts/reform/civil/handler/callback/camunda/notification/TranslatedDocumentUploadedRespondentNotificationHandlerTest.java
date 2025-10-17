package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.GeneralAppParentCaseLink;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.GaForLipService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.SolicitorEmailValidation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
public class TranslatedDocumentUploadedRespondentNotificationHandlerTest extends BaseCallbackHandlerTest {

    private TranslatedDocumentUploadedRespondentNotificationHandler handler;
    private CallbackParams params;
    @Mock
    private NotificationService notificationService;
    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private GaForLipService gaForLipService;
    @Mock
    private OrganisationService organisationService;
    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private CoreCaseDataService coreCaseDataService;
    @Mock
    private SolicitorEmailValidation solicitorEmailValidation;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private NotificationsSignatureConfiguration configuration;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .registerModule(new Jdk8Module());

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setUp() {
            handler = new TranslatedDocumentUploadedRespondentNotificationHandler(
                notificationService,
                notificationsProperties,
                caseDetailsConverter,
                gaForLipService,
                coreCaseDataService,
                solicitorEmailValidation,
                organisationService,
                featureToggleService,
                configuration,
                OBJECT_MAPPER
            );
            when(configuration.getHmctsSignature()).thenReturn("Online Civil Claims \n HM Courts & Tribunal Service");
            when(configuration.getPhoneContact()).thenReturn("For anything related to hearings, call 0300 123 5577 "
                                                                 + "\n For all other matters, call 0300 123 7050");
            when(configuration.getOpeningHours()).thenReturn("Monday to Friday, 8.30am to 5pm");
            when(configuration.getWelshContact()).thenReturn("E-bost: ymholiadaucymraeg@justice.gov.uk");
            when(configuration.getSpecContact()).thenReturn("Email: contactocmc@justice.gov.uk");
            when(configuration.getWelshHmctsSignature()).thenReturn("Hawliadau am Arian yn y Llys Sifil Ar-lein \n Gwasanaeth Llysoedd a Thribiwnlysoedd EF");
            when(configuration.getWelshPhoneContact()).thenReturn("Ffôn: 0300 303 5174");
            when(configuration.getWelshOpeningHours()).thenReturn("Dydd Llun i ddydd Iau, 9am – 5pm, dydd Gwener, 9am – 4.30pm");
            when(solicitorEmailValidation.validateSolicitorEmail(
                any(CaseData.class),
                any(GeneralApplicationCaseData.class)
            )).thenAnswer(invocation -> invocation.getArgument(1));
        }

        @Test
        void shouldSendNotificationLiPRespondentConsent_WhenParentCaseInEnglish() {
            // Given
            CaseData caseData = gaCaseData(builder -> builder
                .withApplicantPartyName("applicant1")
                .withDefendant1PartyName("respondent1")
                .withGeneralAppRespondentSolicitors(
                    wrapElements(GASolicitorDetailsGAspec.builder().email("respondent@gmail.com").build()))
                .withIsGaRespondentOneLip(YES)
                .withParentClaimantIsApplicant(YES)
                .withGeneralAppParentCaseReference("56789")
            ).toBuilder()
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("1234567").build())
                .generalAppConsentOrder(YES)
                .ccdCaseReference(56786L)
                .parentCaseReference("56789")
                .build();
            CaseDetails civil = CaseDetails.builder().id(123L).data(Map.of("case_data", caseData)).build();
            when(coreCaseDataService.getCase(any())).thenReturn(civil);
            when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(caseData);
            when(gaForLipService.isLipRespGa(any(GeneralApplicationCaseData.class))).thenReturn(true);
            when(notificationsProperties.getLipGeneralAppRespondentEmailTemplate()).thenReturn("template-id");
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CaseEvent.NOTIFY_RESPONDENT_TRANSLATED_DOCUMENT_UPLOADED_GA.name())
                    .build()).build();
            ;
            // When
            handler.handle(params);

            // Then
            verify(notificationService).sendMail(
                eq("respondent@gmail.com"),
                eq("template-id"),
                anyMap(),
                anyString()
            );
        }

        @Test
        void shouldSendNotificationLiPRespondentConsent_WhenParentCaseInWelsh() {
            // Given
            CaseData caseData = gaCaseData(builder -> builder
                .withApplicantPartyName("applicant1")
                .withDefendant1PartyName("respondent1")
                .withGeneralAppRespondentSolicitors(
                    wrapElements(GASolicitorDetailsGAspec.builder().email("respondent@gmail.com").build()))
                .withIsGaRespondentOneLip(YES)
                .withParentClaimantIsApplicant(YES)
                .withRespondentBilingualPreference(YES)
                .withGeneralAppParentCaseReference("56789")
            ).toBuilder()
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("1234567").build())
                .generalAppConsentOrder(YES)
                .ccdCaseReference(56786L)
                .parentCaseReference("56789")
                .build();
            CaseDetails civil = CaseDetails.builder().id(123L).data(Map.of("case_data", caseData)).build();
            when(coreCaseDataService.getCase(any())).thenReturn(civil);
            when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(caseData);
            when(gaForLipService.isLipRespGa(any(GeneralApplicationCaseData.class))).thenReturn(true);
            when(notificationsProperties.getNotifyRespondentLiPTranslatedDocumentUploadedWhenParentCaseInBilingual()).thenReturn(
                "template-id");
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CaseEvent.NOTIFY_RESPONDENT_TRANSLATED_DOCUMENT_UPLOADED_GA.name())
                    .build()).build();
            ;
            // When
            handler.handle(params);

            // Then
            verify(notificationService).sendMail(
                eq("respondent@gmail.com"),
                eq("template-id"),
                anyMap(),
                anyString()
            );
        }

        @Test
        void shouldSendNotificationRespondentConsentForLR() {
            // Given
            CaseData caseData = gaCaseData(builder -> builder
                .withApplicantPartyName("applicant1")
                .withDefendant1PartyName("respondent1")
                .withGeneralAppRespondentSolicitors(
                    wrapElements(GASolicitorDetailsGAspec.builder().email("respondent@gmail.com").build()))
                .withIsGaRespondentOneLip(NO)
                .withParentClaimantIsApplicant(YES)
                .withRespondentBilingualPreference(YES)
                .withGeneralAppParentCaseReference("56789")
            ).toBuilder()
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("1234567").build())
                .generalAppConsentOrder(YES)
                .ccdCaseReference(56786L)
                .parentCaseReference("56789")
                .build();
            CaseDetails civil = CaseDetails.builder().id(123L).data(Map.of("case_data", caseData)).build();
            when(coreCaseDataService.getCase(any())).thenReturn(civil);
            when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(caseData);
            when(gaForLipService.isLipRespGa(any(GeneralApplicationCaseData.class))).thenReturn(false);
            when(organisationService.findOrganisationById(any())).thenReturn(Optional.of(Organisation.builder()
                                                                                             .name("LegalRep")
                                                                                             .build()));
            when(notificationsProperties.getNotifyLRTranslatedDocumentUploaded()).thenReturn(
                "template-id");
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CaseEvent.NOTIFY_RESPONDENT_TRANSLATED_DOCUMENT_UPLOADED_GA.name())
                    .build()).build();
            ;
            // When
            handler.handle(params);

            // Then
            verify(notificationService).sendMail(
                eq("respondent@gmail.com"),
                eq("template-id"),
                anyMap(),
                anyString()
            );
        }
    }

    private CaseData gaCaseData(Consumer<GeneralApplicationCaseDataBuilder> customiser) {
        GeneralApplicationCaseDataBuilder builder = GeneralApplicationCaseDataBuilder.builder();
        customiser.accept(builder);
        GeneralApplicationCaseData gaCaseData = builder.build();
        return OBJECT_MAPPER.convertValue(gaCaseData, CaseData.class);
    }
}
