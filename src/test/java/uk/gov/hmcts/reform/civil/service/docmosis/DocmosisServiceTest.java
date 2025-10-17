package uk.gov.hmcts.reform.civil.service.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.GAByCourtsInitiativeGAspec;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.genapplication.GACaseLocation;
import uk.gov.hmcts.reform.civil.model.genapplication.GAJudicialMakeAnOrder;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.GeneralAppLocationRefDataService;
import uk.gov.hmcts.reform.civil.service.ga.GaCaseDataEnricher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService.DATE_FORMATTER;

@SpringBootTest(classes = {
    DocmosisService.class})
public class DocmosisServiceTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .registerModule(new Jdk8Module());

    @Autowired
    private DocmosisService docmosisService;
    @MockBean
    private GeneralAppLocationRefDataService generalAppLocationRefDataService;
    private final GaCaseDataEnricher gaCaseDataEnricher = new GaCaseDataEnricher();

    private static final List<LocationRefData> locationRefData = Arrays
        .asList(LocationRefData.builder().epimmsId("1").venueName("Reading").build(),
                LocationRefData.builder().epimmsId("2").venueName("London").build(),
                LocationRefData.builder().epimmsId("3").venueName("Manchester").build(),
                LocationRefData.builder().epimmsId("420219").venueName("CNBC").build());

    @Test
    void shouldReturnLocationRefData() {
        when(generalAppLocationRefDataService.getCourtLocations(any())).thenReturn(locationRefData);

        CaseData caseData = caseDataWithLocation("2", null);
        LocationRefData locationRefData = docmosisService.getCaseManagementLocationVenueName(caseData, "auth");
        assertThat(locationRefData.getVenueName())
            .isEqualTo("London");
    }

    @Test
    void shouldReturnLocationRefData_whenSpecAndCnbc() {
        when(generalAppLocationRefDataService.getCnbcLocation(any())).thenReturn(locationRefData);

        CaseData caseData = caseDataWithLocation("420219", CaseCategory.SPEC_CLAIM);
        LocationRefData cnbcLocationRefData = docmosisService.getCaseManagementLocationVenueName(caseData, "auth");
        assertThat(cnbcLocationRefData.getVenueName())
            .isEqualTo("CNBC");
    }

    @Test
    void shouldReturnLocationRefData_whenUspecAndCnbc() {
        when(generalAppLocationRefDataService.getCnbcLocation(any())).thenReturn(locationRefData);

        CaseData caseData = caseDataWithLocation("420219", CaseCategory.UNSPEC_CLAIM);
        LocationRefData cnbcLocationRefData = docmosisService.getCaseManagementLocationVenueName(caseData, "auth");
        assertThat(cnbcLocationRefData.getVenueName())
            .isEqualTo("CNBC");
    }

    @Test
    void shouldThrowExceptionWhenNoLocationMatch() {
        when(generalAppLocationRefDataService.getCourtLocations(any())).thenReturn(locationRefData);

        CaseData caseData = caseDataWithLocation("8", null);

        Exception exception =
            assertThrows(IllegalArgumentException.class, ()
                -> docmosisService.getCaseManagementLocationVenueName(caseData, "auth"));
        String expectedMessage = "Court Name is not found in location data";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void shouldPopulateJudgeReason() {

        CaseData caseData = CaseDataBuilder.builder().build();

        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();
        caseDataBuilder.judicialDecisionMakeOrder(GAJudicialMakeAnOrder.builder()
                                                      .reasonForDecisionText("Test Reason")
                                                      .showReasonForDecision(YesOrNo.YES).build()).build();
        CaseData updateData = caseDataBuilder.build();

        assertThat(docmosisService.populateJudgeReason(updateData)).isEqualTo("Test Reason");
    }

    @Test
    void shouldReturnEmptyJudgeReason() {

        CaseData caseData = CaseDataBuilder.builder().build();

        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();
        caseDataBuilder.judicialDecisionMakeOrder(GAJudicialMakeAnOrder.builder()
                                                      .reasonForDecisionText("Test Reason")
                                                      .showReasonForDecision(YesOrNo.NO).build()).build();
        CaseData updateData = caseDataBuilder.build();

        assertThat(docmosisService.populateJudgeReason(updateData)).isEqualTo(StringUtils.EMPTY);
    }

    @Test
    void shouldReturn_EmptyString_JudgeCourtsInitiative_Option3() {

        CaseData caseData = CaseDataBuilder.builder().build();

        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();
        caseDataBuilder.judicialDecisionMakeOrder(GAJudicialMakeAnOrder.builder()
                                                      .judicialByCourtsInitiative(
                                                          GAByCourtsInitiativeGAspec.OPTION_3).build()).build();
        CaseData updateData = caseDataBuilder.build();

        assertThat(docmosisService.populateJudicialByCourtsInitiative(updateData)).isEqualTo(StringUtils.EMPTY);
    }

    @Test
    void shouldPopulate_JudgeCourtsInitiative_Option2() {

        CaseData caseData = CaseDataBuilder.builder().build();

        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();
        caseDataBuilder.judicialDecisionMakeOrder(GAJudicialMakeAnOrder.builder()
                                                      .orderWithoutNotice("abcdef")
                                                      .orderWithoutNoticeDate(LocalDate.now())
                                                      .judicialByCourtsInitiative(
                                                          GAByCourtsInitiativeGAspec.OPTION_2)
                                                      .build()).build();
        CaseData updateData = caseDataBuilder.build();

        assertThat(docmosisService.populateJudicialByCourtsInitiative(updateData))
            .isEqualTo("abcdef ".concat(LocalDate.now().format(DATE_FORMATTER)));
    }

    @Test
    void shouldPopulate_JudgeCourtsInitiative_Option1() {

        CaseData caseData = CaseDataBuilder.builder().build();

        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();
        caseDataBuilder.judicialDecisionMakeOrder(GAJudicialMakeAnOrder.builder()
                                                      .orderCourtOwnInitiative("abcdef")
                                                      .orderCourtOwnInitiativeDate(LocalDate.now())
                                                      .judicialByCourtsInitiative(
                                                          GAByCourtsInitiativeGAspec.OPTION_1)
                                                      .build()).build();
        CaseData updateData = caseDataBuilder.build();

        assertThat(docmosisService.populateJudicialByCourtsInitiative(updateData))
            .isEqualTo("abcdef ".concat(LocalDate.now().format(DATE_FORMATTER)));
    }

    private CaseData caseDataWithLocation(String baseLocation, CaseCategory category) {
        return gaCaseData(builder -> {
            builder.withGaCaseManagementLocation(GACaseLocation.builder().baseLocation(baseLocation).build());
            if (category != null) {
                builder.withCaseAccessCategory(category);
            }
        });
    }

    private CaseData gaCaseData(Consumer<GeneralApplicationCaseDataBuilder> customiser) {
        GeneralApplicationCaseDataBuilder builder = GeneralApplicationCaseDataBuilder.builder();
        customiser.accept(builder);
        GeneralApplicationCaseData gaCaseData = builder.build();
        CaseData converted = OBJECT_MAPPER.convertValue(gaCaseData, CaseData.class);
        return gaCaseDataEnricher.enrich(converted, gaCaseData);
    }

}
