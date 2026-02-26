package uk.gov.hmcts.reform.civil.ga.service.docmosis;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAByCourtsInitiativeGAspec;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialMakeAnOrder;
import uk.gov.hmcts.reform.civil.ga.service.GeneralAppLocationRefDataService;
import uk.gov.hmcts.reform.civil.model.genapplication.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService.DATE_FORMATTER;

@ExtendWith(MockitoExtension.class)
public class DocmosisServiceTest {

    private static final List<LocationRefData> locationRefData = Arrays
        .asList(
            new LocationRefData().setEpimmsId("1").setVenueName("Reading"),
            new LocationRefData().setEpimmsId("2").setVenueName("London"),
            new LocationRefData().setEpimmsId("3").setVenueName("Manchester"),
            new LocationRefData().setEpimmsId("420219").setVenueName("CNBC")
        );
    @InjectMocks
    private DocmosisService docmosisService;
    @Mock
    private GeneralAppLocationRefDataService generalAppLocationRefDataService;

    @BeforeEach
    void setUp() {
        setField(docmosisService, "cnbcEpimmId", "420219");
    }

    @Test
    void shouldReturnLocationRefData() {
        when(generalAppLocationRefDataService.getCourtLocations(any())).thenReturn(locationRefData);

        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .caseManagementLocation(CaseLocationCivil.builder().baseLocation("2").build()).build();
        LocationRefData locationRefData = docmosisService.getCaseManagementLocationVenueName(caseData, "auth");
        assertThat(locationRefData.getVenueName())
            .isEqualTo("London");
    }

    @Test
    void shouldReturnLocationRefData_whenSpecAndCnbc() {
        when(generalAppLocationRefDataService.getCnbcLocation(any())).thenReturn(locationRefData);

        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .caseManagementLocation(CaseLocationCivil.builder().baseLocation("420219").build()).build();
        LocationRefData cnbcLocationRefData = docmosisService.getCaseManagementLocationVenueName(caseData, "auth");
        assertThat(cnbcLocationRefData.getVenueName())
            .isEqualTo("CNBC");
    }

    @Test
    void shouldReturnLocationRefData_whenUspecAndCnbc() {
        when(generalAppLocationRefDataService.getCnbcLocation(any())).thenReturn(locationRefData);

        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
            .caseManagementLocation(CaseLocationCivil.builder().baseLocation("420219").build()).build();
        LocationRefData cnbcLocationRefData = docmosisService.getCaseManagementLocationVenueName(caseData, "auth");
        assertThat(cnbcLocationRefData.getVenueName())
            .isEqualTo("CNBC");
    }

    @Test
    void shouldThrowExceptionWhenNoLocationMatch() {
        when(generalAppLocationRefDataService.getCourtLocations(any())).thenReturn(locationRefData);

        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .caseManagementLocation(CaseLocationCivil.builder().baseLocation("8").build()).build();

        Exception exception =
            assertThrows(
                IllegalArgumentException.class, ()
                    -> docmosisService.getCaseManagementLocationVenueName(caseData, "auth")
            );
        String expectedMessage = "Court Name is not found in location data";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void shouldPopulateJudgeReason() {

        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().build();

        GeneralApplicationCaseData caseDataBuilder = caseData.copy();
        caseDataBuilder.judicialDecisionMakeOrder(new GAJudicialMakeAnOrder()
                                                      .setReasonForDecisionText("Test Reason")
                                                      .setShowReasonForDecision(YesOrNo.YES)).build();
        GeneralApplicationCaseData updateData = caseDataBuilder.build();

        assertThat(docmosisService.populateJudgeReason(updateData)).isEqualTo("Test Reason");
    }

    @Test
    void shouldReturnEmptyJudgeReason() {

        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().build();

        GeneralApplicationCaseData caseDataBuilder = caseData.copy();
        caseDataBuilder.judicialDecisionMakeOrder(new GAJudicialMakeAnOrder()
                                                      .setReasonForDecisionText("Test Reason")
                                                      .setShowReasonForDecision(YesOrNo.NO)).build();
        GeneralApplicationCaseData updateData = caseDataBuilder.build();

        assertThat(docmosisService.populateJudgeReason(updateData)).isEqualTo(StringUtils.EMPTY);
    }

    @Test
    void shouldReturn_EmptyString_JudgeCourtsInitiative_Option3() {

        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().build();

        GeneralApplicationCaseData caseDataBuilder = caseData.copy();
        caseDataBuilder.judicialDecisionMakeOrder(new GAJudicialMakeAnOrder()
                                                      .setJudicialByCourtsInitiative(
                                                          GAByCourtsInitiativeGAspec.OPTION_3)).build();
        GeneralApplicationCaseData updateData = caseDataBuilder.build();

        assertThat(docmosisService.populateJudicialByCourtsInitiative(updateData)).isEqualTo(StringUtils.EMPTY);
    }

    @Test
    void shouldPopulate_JudgeCourtsInitiative_Option2() {

        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().build();

        GeneralApplicationCaseData caseDataBuilder = caseData.copy();
        caseDataBuilder.judicialDecisionMakeOrder(new GAJudicialMakeAnOrder()
                                                      .setOrderWithoutNotice("abcdef")
                                                      .setOrderWithoutNoticeDate(LocalDate.now())
                                                      .setJudicialByCourtsInitiative(
                                                          GAByCourtsInitiativeGAspec.OPTION_2)).build();
        GeneralApplicationCaseData updateData = caseDataBuilder.build();

        assertThat(docmosisService.populateJudicialByCourtsInitiative(updateData))
            .isEqualTo("abcdef ".concat(LocalDate.now().format(DATE_FORMATTER)));
    }

    @Test
    void shouldPopulate_JudgeCourtsInitiative_Option1() {

        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().build();

        GeneralApplicationCaseData caseDataBuilder = caseData.copy();
        caseDataBuilder.judicialDecisionMakeOrder(new GAJudicialMakeAnOrder()
                                                      .setOrderCourtOwnInitiative("abcdef")
                                                      .setOrderCourtOwnInitiativeDate(LocalDate.now())
                                                      .setJudicialByCourtsInitiative(
                                                          GAByCourtsInitiativeGAspec.OPTION_1)).build();
        GeneralApplicationCaseData updateData = caseDataBuilder.build();

        assertThat(docmosisService.populateJudicialByCourtsInitiative(updateData))
            .isEqualTo("abcdef ".concat(LocalDate.now().format(DATE_FORMATTER)));
    }

}
