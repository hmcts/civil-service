package uk.gov.hmcts.reform.civil.handler.migration;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.utils.PartyUtils;

import java.util.List;
import java.util.UUID;
import java.util.function.UnaryOperator;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.mockStatic;

class PartyDataMigrationUtilsTest {

    @Test
    void defaultIfNull_shouldReturnValueIfNotNull() {
        String result = PartyDataMigrationUtils.defaultIfNull("John");
        assertThat(result).isEqualTo("John");
    }

    @Test
    void defaultIfNull_shouldReturnTBCIfNull() {
        String result = PartyDataMigrationUtils.defaultIfNull(null);
        assertThat(result).isEqualTo("TBC");
    }

    @Test
    void generatePartyIdIfNull_shouldReturnValueIfNotNull() {
        String result = PartyDataMigrationUtils.generatePartyIdIfNull("existing-id");
        assertThat(result).isEqualTo("existing-id");
    }

    @Test
    void generatePartyIdIfNull_shouldReturnGeneratedIdIfNull() {
        try (MockedStatic<PartyUtils> utilities = mockStatic(PartyUtils.class)) {
            utilities.when(PartyUtils::createPartyId).thenReturn("generated-id");

            String result = PartyDataMigrationUtils.generatePartyIdIfNull(null);
            assertThat(result).isEqualTo("generated-id");
        }
    }

    @Test
    void updateElements_shouldTransformElements() {
        Element<String> element1 = Element.<String>builder()
            .id(UUID.fromString("00000000-0000-0000-0000-000000000001"))
            .value("A")
            .build();
        Element<String> element2 = Element.<String>builder()
            .id(UUID.fromString("00000000-0000-0000-0000-000000000002"))
            .value("B")
            .build();
        List<Element<String>> elements = List.of(element1, element2);

        UnaryOperator<String> transformer = s -> s + "-updated";

        List<Element<String>> result = PartyDataMigrationUtils.updateElements(elements, transformer);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getValue()).isEqualTo("A-updated");
        assertThat(result.get(1).getValue()).isEqualTo("B-updated");
        assertThat(result.get(0).getId()).isEqualTo(element1.getId());
        assertThat(result.get(1).getId()).isEqualTo(element2.getId());
    }

    @Test
    void updateElements_shouldReturnEmptyListIfInputIsNull() {
        List<Element<String>> result = PartyDataMigrationUtils.updateElements(null, s -> s + "-updated");
        assertThat(result).isEmpty();
    }
}
