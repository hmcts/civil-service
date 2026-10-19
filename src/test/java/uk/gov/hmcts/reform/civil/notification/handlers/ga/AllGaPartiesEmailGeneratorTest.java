package uk.gov.hmcts.reform.civil.notification.handlers.ga;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.SolicitorEmailValidation;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.GeneralAppParentCaseLink;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The General Application side of the framework did not exist until DTSCCI-6726, so these pin the
 * two things it has to get right: the parent case is fetched once however many recipients there
 * are, and a generator standing for a collection of recipients can return more than one email.
 */
class AllGaPartiesEmailGeneratorTest {

    private static final String PARENT_REFERENCE = "1594901956117591";
    private static final String TASK_ID = "NotifyGaSomething";

    private CaseDetailsConverter caseDetailsConverter;
    private CoreCaseDataService coreCaseDataService;
    private SolicitorEmailValidation solicitorEmailValidation;
    private GeneralApplicationCaseData gaCaseData;
    private GeneralApplicationCaseData validatedCaseData;
    private GeneralApplicationCaseData mainCaseData;

    @BeforeEach
    void setUp() {
        caseDetailsConverter = mock(CaseDetailsConverter.class);
        coreCaseDataService = mock(CoreCaseDataService.class);
        solicitorEmailValidation = mock(SolicitorEmailValidation.class);

        gaCaseData = mock(GeneralApplicationCaseData.class);
        validatedCaseData = mock(GeneralApplicationCaseData.class);
        mainCaseData = mock(GeneralApplicationCaseData.class);

        when(gaCaseData.getGeneralAppParentCaseLink())
            .thenReturn(new GeneralAppParentCaseLink(PARENT_REFERENCE));
        when(coreCaseDataService.getCase(anyLong())).thenReturn(CaseDetails.builder().build());
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(mainCaseData);
        when(solicitorEmailValidation.validateSolicitorEmail(mainCaseData, gaCaseData))
            .thenReturn(validatedCaseData);
        when(validatedCaseData.getCcdCaseReference()).thenReturn(1234567890123456L);
        when(validatedCaseData.getGeneralAppParentCaseLink())
            .thenReturn(new GeneralAppParentCaseLink(PARENT_REFERENCE));
    }

    private AllGaPartiesEmailGenerator generatorWith(List<? extends GaEmailDTOGenerator> generators) {
        return new AllGaPartiesEmailGenerator(
            generators, caseDetailsConverter, coreCaseDataService, solicitorEmailValidation);
    }

    private EmailDTO email(String address) {
        EmailDTO dto = new EmailDTO();
        dto.setTargetEmail(address);
        dto.setEmailTemplate("template");
        dto.setReference(PARENT_REFERENCE);
        dto.setParameters(Map.of());
        return dto;
    }

    /** A generator for one fixed party, which is the civil shape. */
    private class SingleRecipientGenerator extends GaEmailDTOGenerator {
        private final String address;
        private final boolean notify;

        SingleRecipientGenerator(String address, boolean notify) {
            this.address = address;
            this.notify = notify;
        }

        @Override
        public Boolean getShouldNotify(GaNotificationContext context) {
            return notify;
        }

        @Override
        public Map<String, String> addProperties(GaNotificationContext context) {
            return new java.util.HashMap<>();
        }

        @Override
        protected String getEmailAddress(GaNotificationContext context) {
            return address;
        }

        @Override
        protected String getEmailTemplateId(GaNotificationContext context) {
            return "template";
        }

        @Override
        protected Map<String, String> addCustomProperties(Map<String, String> properties,
                                                          GaNotificationContext context) {
            return properties;
        }
    }

    /** A generator standing for a collection of respondents, which is the GA shape. */
    private class CollectionGenerator extends SingleRecipientGenerator {
        private final List<String> addresses;

        CollectionGenerator(List<String> addresses) {
            super(addresses.get(0), true);
            this.addresses = addresses;
        }

        @Override
        public Set<EmailDTO> buildEmailDTOs(GaNotificationContext context, String taskId) {
            return addresses.stream().map(AllGaPartiesEmailGeneratorTest.this::email)
                .collect(java.util.stream.Collectors.toSet());
        }
    }

    @Test
    void shouldFetchTheParentCaseOnceRegardlessOfHowManyGeneratorsThereAre() {
        AllGaPartiesEmailGenerator generator = generatorWith(List.of(
            new SingleRecipientGenerator("one@example.com", true),
            new SingleRecipientGenerator("two@example.com", true),
            new SingleRecipientGenerator("three@example.com", true)));

        generator.getPartiesToNotify(gaCaseData, TASK_ID);

        // a generator per recipient would otherwise mean a CCD fetch per recipient
        verify(coreCaseDataService, times(1)).getCase(Long.parseLong(PARENT_REFERENCE));
        verify(solicitorEmailValidation, times(1)).validateSolicitorEmail(mainCaseData, gaCaseData);
    }

    @Test
    void shouldPassTheValidatedCaseDataToGeneratorsNotTheRawCase() {
        var capturing = new SingleRecipientGenerator("one@example.com", true) {
            GaNotificationContext seen;

            @Override
            public Boolean getShouldNotify(GaNotificationContext context) {
                seen = context;
                return true;
            }
        };

        generatorWith(List.of(capturing)).getPartiesToNotify(gaCaseData, TASK_ID);

        // solicitor emails on the GA case can be stale, so the patched copy is what generators read
        assertThat(capturing.seen.gaCaseData()).isSameAs(validatedCaseData);
        assertThat(capturing.seen.mainCaseData()).isSameAs(mainCaseData);
    }

    @Test
    void shouldSkipGeneratorsThatDoNotWantToNotify() {
        Set<EmailDTO> parties = generatorWith(List.of(
            new SingleRecipientGenerator("wanted@example.com", true),
            new SingleRecipientGenerator("unwanted@example.com", false)))
            .getPartiesToNotify(gaCaseData, TASK_ID);

        assertThat(parties).extracting(EmailDTO::getTargetEmail)
            .containsExactly("wanted@example.com");
    }

    @Test
    void shouldLetOneGeneratorStandForSeveralRecipients() {
        Set<EmailDTO> parties = generatorWith(List.of(
            new CollectionGenerator(List.of("resp1@example.com", "resp2@example.com", "resp3@example.com"))))
            .getPartiesToNotify(gaCaseData, TASK_ID);

        // GA respondents are a list rather than fixed respondent1 and respondent2 slots, so the
        // aggregator must not assume one email per generator
        assertThat(parties).extracting(EmailDTO::getTargetEmail)
            .containsExactlyInAnyOrder("resp1@example.com", "resp2@example.com", "resp3@example.com");
    }

    @Test
    void shouldTolerateANullGenerator() {
        Set<EmailDTO> parties = generatorWith(java.util.Arrays.asList(
            new SingleRecipientGenerator("one@example.com", true), null))
            .getPartiesToNotify(gaCaseData, TASK_ID);

        assertThat(parties).hasSize(1);
        verify(coreCaseDataService, never()).getCase(eq(0L));
    }
}
