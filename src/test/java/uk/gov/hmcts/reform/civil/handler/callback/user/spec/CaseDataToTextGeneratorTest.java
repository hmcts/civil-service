package uk.gov.hmcts.reform.civil.handler.callback.user.spec;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.Application;
import uk.gov.hmcts.reform.civil.config.ClaimUrlsConfiguration;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.proceed.confirmation.PayImmediatelyConfText;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.response.confirmation.PartialAdmitPayImmediatelyConfirmationText;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.PaymentDateService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;

/**
 * CaseDataToTextGenerator can hold any generation of a text that uses only a CaseData. Each intention for that text
 * (e.g. respond to claim confirmation text) is an interface extending CaseDataToTextGenerator. Each class
 * implementing those interfaces are specific to a particular kind of CaseData and the particular intention.
 *
 * <p>This test class tries to ensure that (1) we check all CaseDataToTextGenerators, (2) that for one intention
 * we test at least once each of its CaseDataToTextGenerators, (3) that for a given intention and CaseData there is
 * at most one implementation handling the case.</p>
 */
@ExtendWith(SpringExtension.class)
public class CaseDataToTextGeneratorTest {

    @SuppressWarnings("rawtypes")
    private final List<CaseDataToTextGeneratorIntentionConfig> intentionConfigs = List.of(
        new RespondToClaimConfirmationTextSpecGeneratorTest(),
        new RespondToClaimConfirmationHeaderSpecGeneratorTest(),
        new RespondToResponseConfirmationHeaderGeneratorTest(),
        new RespondToResponseConfirmationTextGeneratorTest()
    );

    /**
     * ensures that each instance of CaseDataToTextGenerator is checked.
     */
    @SuppressWarnings({"SuspiciousMethodCalls", "rawtypes", "unchecked"})
    @Test
    public void allGeneratorsMustBeChecked() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
            CaseDataToTextGeneratorTestConfig.class);
        Collection<CaseDataToTextGenerator> allGenerators = context.getBeansOfType(CaseDataToTextGenerator.class)
            .values();
        List<CaseDataToTextGenerator> toCheck = new ArrayList<>(allGenerators);
        for (CaseDataToTextGeneratorIntentionConfig config : intentionConfigs) {
            Collection used = testIntentionConfig(config, allGenerators);
            toCheck.removeAll(used);
        }
        Assertions.assertTrue(
            toCheck.isEmpty(),
            "Some generators have not been checked " + toCheck.toArray().toString()
        );
    }

    /**
     * Given an intention interface, checks (1) that each implementation answers to at least one case, (2) that each
     * case provided is accepted by exactly one implementation and (3) that that implementation is the one expected for
     * the case.
     *
     * @param config        a config to describe which intention interface we are checking, which cases should we use
     *                      and which implementation should handle each
     * @param allGenerators a collection of all generators available
     * @param <T>           the type of intention interface
     * @return collection of checked implementations
     */
    private <T extends CaseDataToTextGenerator> Collection<T> testIntentionConfig(
        CaseDataToTextGeneratorIntentionConfig<T> config,
        Collection<CaseDataToTextGenerator> allGenerators) {
        Class<T> intentionInterface = config.getIntentionInterface();

        List<T> generators = allGenerators.stream().filter(intentionInterface::isInstance)
            .map(intentionInterface::cast)
            .collect(Collectors.toList());
        List<T> usedGenerators = new ArrayList<>();
        List<Pair<CaseData, Class<? extends T>>> cases = config.getCasesToExpectedImplementation();
        for (int i = 0; i < cases.size(); i++) {
            CaseData currentCase = cases.get(i).getLeft();
            List<T> suitable = generators.stream()
                .filter(generator -> generator.generateTextFor(currentCase).isPresent())
                .collect(Collectors.toList());
            Assertions.assertEquals(1, suitable.size(),
                                    "There should be exactly 1 suitable generator per case."
                                        + "Case in position " + i + " has " + suitable.size()
                                        + ", it was expecting " + cases.get(i).getRight().getSimpleName()
            );
            //noinspection ConstantConditions
            Assertions.assertTrue(
                cases.get(i).getRight().isInstance(suitable.get(0)),
                "Case " + i + " was expected to be processed by an instance of "
                    + cases.get(i).getRight() + " but wasn't"
            );
            usedGenerators.addAll(suitable);
        }
        List<T> toBeUsed = new ArrayList<>(generators);
        toBeUsed.removeIf(usedGenerators::contains);
        Assertions.assertTrue(
            toBeUsed.isEmpty(),
            "Each generator should be used at least once. Please complete the case list. " + toBeUsed
                .stream().map(element -> element.getClass().getSimpleName()).collect(Collectors.joining("-", "{", "}"))
        );
        return generators;
    }

    /**
     * configuration class to be sure to initialize all implementations of
     * RespondConfirmationTextGenerator.
     */
    @Configuration
    @ComponentScan(basePackageClasses = Application.class,
        useDefaultFilters = false,
        includeFilters = @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = {CaseDataToTextGenerator.class, ClaimUrlsConfiguration.class}
        )
    )
    public static class CaseDataToTextGeneratorTestConfig {
        @Bean
        public PaymentDateService paymentDateService() {
            PaymentDateService mockPaymentDateService = mock(PaymentDateService.class);
            when(mockPaymentDateService.getPaymentDateAdmittedClaim(any())).thenReturn(LocalDate.EPOCH);
            return mockPaymentDateService;
        }
    }

    /**
     * When we create an intention interface and their implementations, we should include in the intentionConfigs list
     * an instance of this class.
     *
     * @param <T> an intention interface
     */
    public interface CaseDataToTextGeneratorIntentionConfig<T extends CaseDataToTextGenerator> {

        /**
         * Each config is relevant to one intention.
         *
         * @return intention interface relevant to this config
         */
        Class<T> getIntentionInterface();

        /**
         * We need a set of cases to check that the implementations of T do not overlap.
         *
         * @return a list of pairs, each with an instance of a CaseData and the class of the implementation that
         *     should process it.
         */
        List<Pair<CaseData, Class<? extends T>>> getCasesToExpectedImplementation();
    }

    @InjectMocks
    private PayImmediatelyConfText generatorConf;
    @InjectMocks
    private PartialAdmitPayImmediatelyConfirmationText generatorHeader;

    @Mock
    private PaymentDateService paymentDateService;

    private CaseData buildFullAdmitPayImmediatelyWithoutWhenBePaidProceedCaseData() {
        return CaseData.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .legacyCaseReference("claimNumber")
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .build();
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenPaymentDateCannotBeFormattedPayImmediatelyConfText() {
        CaseData caseData = buildFullAdmitPayImmediatelyWithoutWhenBePaidProceedCaseData();

        Assertions.assertThrows(IllegalStateException.class, () -> generatorConf.generateTextFor(caseData));
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenPaymentDateCannotBeFormatted() {
        CaseData caseData = buildFullAdmitPayImmediatelyWithoutWhenBePaidProceedCaseData();

        Assertions.assertThrows(IllegalStateException.class, () -> generatorHeader.generateTextFor(caseData));
    }
}
