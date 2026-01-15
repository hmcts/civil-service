package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.utils.HearingTypeListUtils.INTERMEDIATE_LIST;
import static uk.gov.hmcts.reform.civil.utils.HearingTypeListUtils.MULTI_LIST;

class HearingTypeListUtilsTest {

    @Test
    void shouldHaveIntermediateListWithCorrectElements() {
        assertThat(INTERMEDIATE_LIST).isNotNull();
        assertThat(INTERMEDIATE_LIST.getListItems()).isNotNull();
        assertThat(INTERMEDIATE_LIST.getListItems()).hasSize(4);

        Map<String, String> codeToLabelMap = INTERMEDIATE_LIST.getListItems().stream()
            .collect(Collectors.toMap(
                DynamicListElement::getCode,
                DynamicListElement::getLabel
            ));

        assertThat(codeToLabelMap)
            .containsEntry("CASE_MANAGEMENT_CONFERENCE", "Case Management Conference (CMC)")
            .containsEntry("PRE_TRIAL_REVIEW", "Pre Trial Review (PTR)")
            .containsEntry("TRIAL", "Trial")
            .containsEntry("OTHER", "Other");
    }

    @Test
    void shouldHaveMultiListWithCorrectElements() {
        assertThat(MULTI_LIST).isNotNull();
        assertThat(MULTI_LIST.getListItems()).isNotNull();
        assertThat(MULTI_LIST.getListItems()).hasSize(5);

        Map<String, String> codeToLabelMap = MULTI_LIST.getListItems().stream()
            .collect(Collectors.toMap(
                DynamicListElement::getCode,
                DynamicListElement::getLabel
            ));

        assertThat(codeToLabelMap)
            .containsEntry("CASE_MANAGEMENT_CONFERENCE", "Case Management Conference (CMC)")
            .containsEntry("COSTS_CASE_MANAGEMENT_CONFERENCE", "Costs and Case Management Conference (CCMC)")
            .containsEntry("PRE_TRIAL_REVIEW", "Pre Trial Review (PTR)")
            .containsEntry("TRIAL", "Trial")
            .containsEntry("OTHER", "Other");
    }

    @Test
    void shouldHaveMultiListContainingOneMoreElementThanIntermediateList() {
        assertThat(MULTI_LIST.getListItems()).hasSize(INTERMEDIATE_LIST.getListItems().size() + 1);
    }

    @Test
    void shouldHaveMultiListContainingCostsCaseManagementConference() {
        List<String> multiListCodes = MULTI_LIST.getListItems().stream()
            .map(DynamicListElement::getCode)
            .toList();

        assertThat(multiListCodes).contains("COSTS_CASE_MANAGEMENT_CONFERENCE");
    }

    @Test
    void shouldNotHaveIntermediateListContainingCostsCaseManagementConference() {
        List<String> intermediateListCodes = INTERMEDIATE_LIST.getListItems().stream()
            .map(DynamicListElement::getCode)
            .toList();

        assertThat(intermediateListCodes)
            .isNotEmpty()
            .doesNotContain("COSTS_CASE_MANAGEMENT_CONFERENCE");
    }
}

