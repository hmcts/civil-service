package uk.gov.hmcts.reform.civil.crd.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CategorySearchResultTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void builder_WithCategories_ReturnsCategorySearchResult() {
        // Arrange
        Category category1 = Category.builder()
            .categoryKey("KEY1")
            .key("key1")
            .valueEn("Value 1")
            .build();
        Category category2 = Category.builder()
            .categoryKey("KEY2")
            .key("key2")
            .valueEn("Value 2")
            .build();
        List<Category> categories = Arrays.asList(category1, category2);

        // Act
        CategorySearchResult result = CategorySearchResult.builder()
            .categories(categories)
            .build();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getCategories()).hasSize(2)
            .containsExactly(category1, category2);
    }

    @Test
    void noArgsConstructor_CreatesEmptyInstance() {
        // Act
        CategorySearchResult result = new CategorySearchResult();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getCategories()).isNull();
    }

    @Test
    void allArgsConstructor_WithCategories_CreatesInstance() {
        // Arrange
        Category category = Category.builder()
            .categoryKey("KEY")
            .key("key")
            .valueEn("Value")
            .build();
        List<Category> categories = Collections.singletonList(category);

        // Act
        CategorySearchResult result = new CategorySearchResult(categories);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getCategories()).hasSize(1)
            .containsExactly(category);
    }

    @Test
    void setCategories_UpdatesCategories() {
        // Arrange
        CategorySearchResult result = new CategorySearchResult();
        Category category = Category.builder()
            .categoryKey("KEY")
            .key("key")
            .valueEn("Value")
            .build();
        List<Category> categories = Collections.singletonList(category);

        // Act
        result.setCategories(categories);

        // Assert
        assertThat(result.getCategories()).hasSize(1)
            .containsExactly(category);
    }

    @Test
    void equals_SameCategories_ReturnsTrue() {
        // Arrange
        Category category = Category.builder()
            .categoryKey("KEY")
            .key("key")
            .valueEn("Value")
            .build();
        List<Category> categories = Collections.singletonList(category);
        CategorySearchResult result1 = CategorySearchResult.builder()
            .categories(categories)
            .build();
        CategorySearchResult result2 = CategorySearchResult.builder()
            .categories(categories)
            .build();

        // Act & Assert
        assertThat(result1).isEqualTo(result2);
        assertThat(result1.hashCode()).hasSameHashCodeAs(result2.hashCode());
    }

    @Test
    void equals_DifferentCategories_ReturnsFalse() {
        // Arrange
        Category category1 = Category.builder()
            .categoryKey("KEY1")
            .key("key1")
            .valueEn("Value 1")
            .build();
        Category category2 = Category.builder()
            .categoryKey("KEY2")
            .key("key2")
            .valueEn("Value 2")
            .build();
        CategorySearchResult result1 = CategorySearchResult.builder()
            .categories(Collections.singletonList(category1))
            .build();
        CategorySearchResult result2 = CategorySearchResult.builder()
            .categories(Collections.singletonList(category2))
            .build();

        // Act & Assert
        assertThat(result1).isNotEqualTo(result2);
    }

    @Test
    void toString_WithCategories_ReturnsFormattedString() {
        // Arrange
        Category category = Category.builder()
            .categoryKey("KEY")
            .key("key")
            .valueEn("Value")
            .build();
        CategorySearchResult result = CategorySearchResult.builder()
            .categories(Collections.singletonList(category))
            .build();

        // Act
        String toString = result.toString();

        // Assert
        assertThat(toString).contains("CategorySearchResult")
            .contains("categories");
    }

    @Test
    void jsonSerialization_WithCategories_SerializesCorrectly() throws Exception {
        // Arrange
        Category category = Category.builder()
            .categoryKey("KEY")
            .key("key")
            .valueEn("Value")
            .valueCy("Gwerth")
            .lovOrder(1)
            .build();
        CategorySearchResult result = CategorySearchResult.builder()
            .categories(Collections.singletonList(category))
            .build();

        // Act
        String json = objectMapper.writeValueAsString(result);

        // Assert
        for (String s : Arrays.asList(
            "\"list_of_values\"",
            "\"category_key\":\"KEY\"",
            "\"key\":\"key\"",
            "\"value_en\":\"Value\"",
            "\"value_cy\":\"Gwerth\"",
            "\"lov_order\":1"
        )) {
            assertThat(json).contains(s);
        }
    }

    @Test
    void jsonDeserialization_ValidJson_DeserializesCorrectly() throws Exception {
        // Arrange
        String json = "{\"list_of_values\":[{\"category_key\":\"KEY\",\"key\":\"key\",\"value_en\":\"Value\",\"value_cy\":\"Gwerth\",\"lov_order\":1}]}";

        // Act
        CategorySearchResult result = objectMapper.readValue(json, CategorySearchResult.class);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getCategories()).hasSize(1);
        Category category = result.getCategories().get(0);
        assertThat(category.getCategoryKey()).isEqualTo("KEY");
        assertThat(category.getKey()).isEqualTo("key");
        assertThat(category.getValueEn()).isEqualTo("Value");
        assertThat(category.getValueCy()).isEqualTo("Gwerth");
        assertThat(category.getLovOrder()).isOne();
    }

    @Test
    void jsonDeserialization_EmptyCategories_DeserializesCorrectly() throws Exception {
        // Arrange
        String json = "{\"list_of_values\":[]}";

        // Act
        CategorySearchResult result = objectMapper.readValue(json, CategorySearchResult.class);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getCategories()).isEmpty();
    }

    @Test
    void jsonDeserialization_NullCategories_DeserializesCorrectly() throws Exception {
        // Arrange
        String json = "{\"list_of_values\":null}";

        // Act
        CategorySearchResult result = objectMapper.readValue(json, CategorySearchResult.class);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getCategories()).isNull();
    }

    @Test
    void builder_EmptyCategories_ReturnsCategorySearchResult() {
        // Act
        CategorySearchResult result = CategorySearchResult.builder()
            .categories(Collections.emptyList())
            .build();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getCategories()).isEmpty();
    }

    @Test
    void builder_NullCategories_ReturnsCategorySearchResult() {
        // Act
        CategorySearchResult result = CategorySearchResult.builder()
            .categories(null)
            .build();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getCategories()).isNull();
    }
}
