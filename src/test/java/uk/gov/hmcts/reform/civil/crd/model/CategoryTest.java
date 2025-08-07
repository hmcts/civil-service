package uk.gov.hmcts.reform.civil.crd.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CategoryTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void builder_AllFields_ReturnsCategoryWithAllFields() {
        // Arrange
        Category childCategory = Category.builder()
            .key("child-key")
            .valueEn("Child Value")
            .build();

        // Act
        Category category = Category.builder()
            .activeFlag("Y")
            .categoryKey("HearingChannel")
            .hintTextCy("Awgrym testun Cymraeg")
            .hintTextEn("English hint text")
            .key("hearing-channel-key")
            .lovOrder(1)
            .parentCategory("ParentCategory")
            .parentKey("parent-key")
            .valueCy("Gwerth Cymraeg")
            .valueEn("English Value")
            .childNodes(Collections.singletonList(childCategory))
            .build();

        // Assert
        assertThat(category).isNotNull();
        assertThat(category.getActiveFlag()).isEqualTo("Y");
        assertThat(category.getCategoryKey()).isEqualTo("HearingChannel");
        assertThat(category.getHintTextCy()).isEqualTo("Awgrym testun Cymraeg");
        assertThat(category.getHintTextEn()).isEqualTo("English hint text");
        assertThat(category.getKey()).isEqualTo("hearing-channel-key");
        assertThat(category.getLovOrder()).isOne();
        assertThat(category.getParentCategory()).isEqualTo("ParentCategory");
        assertThat(category.getParentKey()).isEqualTo("parent-key");
        assertThat(category.getValueCy()).isEqualTo("Gwerth Cymraeg");
        assertThat(category.getValueEn()).isEqualTo("English Value");
        assertThat(category.getChildNodes()).hasSize(1);
        assertThat(category.getChildNodes().get(0)).isEqualTo(childCategory);
    }

    @Test
    void noArgsConstructor_WhenCalled_ReturnsEmptyCategory() {
        // Act
        Category category = new Category();

        // Assert
        assertThat(category).isNotNull();
        assertThat(category.getActiveFlag()).isNull();
        assertThat(category.getCategoryKey()).isNull();
        assertThat(category.getHintTextCy()).isNull();
        assertThat(category.getHintTextEn()).isNull();
        assertThat(category.getKey()).isNull();
        assertThat(category.getLovOrder()).isZero();
        assertThat(category.getParentCategory()).isNull();
        assertThat(category.getParentKey()).isNull();
        assertThat(category.getValueCy()).isNull();
        assertThat(category.getValueEn()).isNull();
        assertThat(category.getChildNodes()).isNull();
    }

    @Test
    void allArgsConstructor_AllFields_ReturnsCategoryWithAllFields() {
        // Arrange
        List<Category> childNodes = Arrays.asList(
            Category.builder().key("child1").build(),
            Category.builder().key("child2").build()
        );

        // Act
        Category category = new Category(
            "N",
            "CategoryKey",
            "Hint Cy",
            "Hint En",
            "key-123",
            5,
            "Parent",
            "parent-key-123",
            "Value Cy",
            "Value En",
            childNodes
        );

        // Assert
        assertThat(category.getActiveFlag()).isEqualTo("N");
        assertThat(category.getCategoryKey()).isEqualTo("CategoryKey");
        assertThat(category.getHintTextCy()).isEqualTo("Hint Cy");
        assertThat(category.getHintTextEn()).isEqualTo("Hint En");
        assertThat(category.getKey()).isEqualTo("key-123");
        assertThat(category.getLovOrder()).isEqualTo(5);
        assertThat(category.getParentCategory()).isEqualTo("Parent");
        assertThat(category.getParentKey()).isEqualTo("parent-key-123");
        assertThat(category.getValueCy()).isEqualTo("Value Cy");
        assertThat(category.getValueEn()).isEqualTo("Value En");
        assertThat(category.getChildNodes()).hasSize(2);
    }

    @Test
    void setters_ValidValues_UpdatesFields() {
        // Arrange
        Category category = new Category();

        // Act
        category.setActiveFlag("Y");
        category.setCategoryKey("TestCategory");
        category.setHintTextCy("Welsh Hint");
        category.setHintTextEn("English Hint");
        category.setKey("test-key");
        category.setLovOrder(10);
        category.setParentCategory("ParentCat");
        category.setParentKey("parent-test-key");
        category.setValueCy("Welsh Value");
        category.setValueEn("English Value");
        category.setChildNodes(Collections.emptyList());

        // Assert
        assertThat(category.getActiveFlag()).isEqualTo("Y");
        assertThat(category.getCategoryKey()).isEqualTo("TestCategory");
        assertThat(category.getHintTextCy()).isEqualTo("Welsh Hint");
        assertThat(category.getHintTextEn()).isEqualTo("English Hint");
        assertThat(category.getKey()).isEqualTo("test-key");
        assertThat(category.getLovOrder()).isEqualTo(10);
        assertThat(category.getParentCategory()).isEqualTo("ParentCat");
        assertThat(category.getParentKey()).isEqualTo("parent-test-key");
        assertThat(category.getValueCy()).isEqualTo("Welsh Value");
        assertThat(category.getValueEn()).isEqualTo("English Value");
        assertThat(category.getChildNodes()).isEmpty();
    }

    @Test
    void equals_SameValues_ReturnsTrue() {
        // Arrange
        Category category1 = Category.builder()
            .key("key1")
            .categoryKey("cat1")
            .valueEn("value1")
            .lovOrder(1)
            .build();

        Category category2 = Category.builder()
            .key("key1")
            .categoryKey("cat1")
            .valueEn("value1")
            .lovOrder(1)
            .build();

        // Assert
        assertThat(category1).isEqualTo(category2);
        assertThat(category1.hashCode()).hasSameHashCodeAs(category2.hashCode());
    }

    @Test
    void equals_DifferentValues_ReturnsFalse() {
        // Arrange
        Category category1 = Category.builder()
            .key("key1")
            .categoryKey("cat1")
            .build();

        Category category2 = Category.builder()
            .key("key2")
            .categoryKey("cat2")
            .build();

        // Assert
        assertThat(category1).isNotEqualTo(category2).isNotNull()
            .isNotEqualTo(new Object());
    }

    @Test
    void toString_ValidObject_ReturnsStringRepresentation() {
        // Arrange
        Category category = Category.builder()
            .key("test-key")
            .categoryKey("TestCategory")
            .valueEn("Test Value")
            .build();

        // Act
        String result = category.toString();

        // Assert
        assertThat(result).isNotNull();
        for (String s : Arrays.asList("Category", "key=test-key", "categoryKey=TestCategory", "valueEn=Test Value")) {
            assertThat(result).contains(s);
        }
    }

    @Test
    void serialization_ValidObject_SerializesCorrectly() throws Exception {
        // Arrange
        Category childCategory = Category.builder()
            .key("child-key")
            .valueEn("Child")
            .build();

        Category category = Category.builder()
            .activeFlag("Y")
            .categoryKey("HearingChannel")
            .hintTextCy("Welsh hint")
            .hintTextEn("English hint")
            .key("hearing-key")
            .lovOrder(3)
            .parentCategory("Parent")
            .parentKey("parent-key")
            .valueCy("Welsh value")
            .valueEn("English value")
            .childNodes(Collections.singletonList(childCategory))
            .build();

        // Act
        String json = objectMapper.writeValueAsString(category);

        // Assert
        for (String s : Arrays.asList(
            "\"active_flag\":\"Y\"",
            "\"category_key\":\"HearingChannel\"",
            "\"hint_text_cy\":\"Welsh hint\"",
            "\"hint_text_en\":\"English hint\"",
            "\"key\":\"hearing-key\"",
            "\"lov_order\":3",
            "\"parent_category\":\"Parent\"",
            "\"parent_key\":\"parent-key\"",
            "\"value_cy\":\"Welsh value\"",
            "\"value_en\":\"English value\"",
            "\"child_nodes\""
        )) {
            assertThat(json).contains(s);
        }
    }

    @Test
    void deserialization_ValidJson_DeserializesCorrectly() throws Exception {
        // Arrange
        String json = """
            {
                "active_flag": "Y",
                "category_key": "HearingChannel",
                "hint_text_cy": "Awgrym Cymraeg",
                "hint_text_en": "English hint",
                "key": "hearing-channel",
                "lov_order": 2,
                "parent_category": "ParentCat",
                "parent_key": "parent-123",
                "value_cy": "Gwerth Cymraeg",
                "value_en": "English Value",
                "child_nodes": [
                    {
                        "key": "child-1",
                        "value_en": "Child Value 1"
                    }
                ]
            }
            """;

        // Act
        Category result = objectMapper.readValue(json, Category.class);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getActiveFlag()).isEqualTo("Y");
        assertThat(result.getCategoryKey()).isEqualTo("HearingChannel");
        assertThat(result.getHintTextCy()).isEqualTo("Awgrym Cymraeg");
        assertThat(result.getHintTextEn()).isEqualTo("English hint");
        assertThat(result.getKey()).isEqualTo("hearing-channel");
        assertThat(result.getLovOrder()).isEqualTo(2);
        assertThat(result.getParentCategory()).isEqualTo("ParentCat");
        assertThat(result.getParentKey()).isEqualTo("parent-123");
        assertThat(result.getValueCy()).isEqualTo("Gwerth Cymraeg");
        assertThat(result.getValueEn()).isEqualTo("English Value");
        assertThat(result.getChildNodes()).hasSize(1);
        assertThat(result.getChildNodes().get(0).getKey()).isEqualTo("child-1");
        assertThat(result.getChildNodes().get(0).getValueEn()).isEqualTo("Child Value 1");
    }

    @Test
    void deserialization_EmptyChildNodes_DeserializesWithEmptyList() throws Exception {
        // Arrange
        String json = """
            {
                "active_flag": "N",
                "category_key": "TestCategory",
                "key": "test-key",
                "lov_order": 1,
                "value_en": "Test Value",
                "child_nodes": []
            }
            """;

        // Act
        Category result = objectMapper.readValue(json, Category.class);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getChildNodes()).isEmpty();
    }

    @Test
    void deserialization_NullChildNodes_DeserializesWithNull() throws Exception {
        // Arrange
        String json = """
            {
                "active_flag": "Y",
                "category_key": "TestCategory",
                "key": "test-key",
                "lov_order": 5,
                "value_en": "Test Value",
                "child_nodes": null
            }
            """;

        // Act
        Category result = objectMapper.readValue(json, Category.class);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getChildNodes()).isNull();
    }

    @Test
    void childNodes_NestedCategories_HandlesHierarchyCorrectly() {
        // Arrange
        Category grandchild = Category.builder()
            .key("grandchild-key")
            .valueEn("Grandchild")
            .build();

        Category child = Category.builder()
            .key("child-key")
            .valueEn("Child")
            .childNodes(Collections.singletonList(grandchild))
            .build();

        Category parent = Category.builder()
            .key("parent-key")
            .valueEn("Parent")
            .childNodes(Collections.singletonList(child))
            .build();

        // Assert
        assertThat(parent.getChildNodes()).hasSize(1);
        assertThat(parent.getChildNodes().get(0).getKey()).isEqualTo("child-key");
        assertThat(parent.getChildNodes().get(0).getChildNodes()).hasSize(1);
        assertThat(parent.getChildNodes().get(0).getChildNodes().get(0).getKey()).isEqualTo("grandchild-key");
    }
}
