package uk.gov.hmcts.reform.civil.bankholidays;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class BankHolidaysTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    void class_HasJsonIgnorePropertiesAnnotation() {
        // Assert
        assertThat(BankHolidays.class.isAnnotationPresent(JsonIgnoreProperties.class)).isTrue();
        JsonIgnoreProperties annotation = BankHolidays.class.getAnnotation(JsonIgnoreProperties.class);
        assertThat(annotation.ignoreUnknown()).isTrue();
    }

    @Test
    void englandAndWales_HasJsonPropertyAnnotation() throws NoSuchFieldException {
        // Act
        Field field = BankHolidays.class.getField("englandAndWales");

        // Assert
        assertThat(field.isAnnotationPresent(JsonProperty.class)).isTrue();
        JsonProperty annotation = field.getAnnotation(JsonProperty.class);
        assertThat(annotation.value()).isEqualTo("england-and-wales");
    }

    @Test
    void englandAndWales_IsPublic() throws NoSuchFieldException {
        // Act
        Field field = BankHolidays.class.getField("englandAndWales");

        // Assert
        assertThat(Modifier.isPublic(field.getModifiers())).isTrue();
    }

    @Test
    void englandAndWales_IsOfTypeDivision() throws NoSuchFieldException {
        // Act
        Field field = BankHolidays.class.getField("englandAndWales");

        // Assert
        assertThat(field.getType()).isEqualTo(BankHolidays.Division.class);
    }

    @Test
    void bankHolidays_CanBeInstantiated() {
        // Act
        BankHolidays bankHolidays = new BankHolidays();

        // Assert
        assertThat(bankHolidays).isNotNull();
        assertThat(bankHolidays.englandAndWales).isNull();
    }

    @Test
    void division_CanBeInstantiated() {
        // Act
        BankHolidays.Division division = new BankHolidays.Division();

        // Assert
        assertThat(division).isNotNull();
        assertThat(division.events).isNull();
    }

    @Test
    void eventDate_CanBeInstantiated() {
        // Act
        BankHolidays.Division.EventDate eventDate = new BankHolidays.Division.EventDate();

        // Assert
        assertThat(eventDate).isNotNull();
        assertThat(eventDate.date).isNull();
    }

    @Test
    void bankHolidays_Structure_IsCorrect() {
        // This test verifies the overall structure of the nested classes
        assertThat(BankHolidays.class.getDeclaredClasses()).extracting(Class::getSimpleName).containsExactlyInAnyOrder("Countries",
                                                                                                                       "Division"
        );

        assertThat(BankHolidays.Division.class.getDeclaredClasses()).extracting(Class::getSimpleName).containsExactly(
            "EventDate");
    }

    @Nested
    class CountriesClassTests {

        @Test
        void countries_IsStaticFinalClass() {
            // Act
            Class<?> countriesClass = BankHolidays.Countries.class;

            // Assert
            assertThat(Modifier.isStatic(countriesClass.getModifiers())).isTrue();
            assertThat(Modifier.isFinal(countriesClass.getModifiers())).isTrue();
        }

        @Test
        void countries_HasPrivateConstructor() throws NoSuchMethodException {
            // Act
            Constructor<BankHolidays.Countries> constructor = BankHolidays.Countries.class.getDeclaredConstructor();

            // Assert
            assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }
    }

    @Nested
    class DivisionClassTests {

        @Test
        void division_IsStaticClass() {
            // Assert
            assertThat(Modifier.isStatic(BankHolidays.Division.class.getModifiers())).isTrue();
            assertThat(Modifier.isPublic(BankHolidays.Division.class.getModifiers())).isTrue();
        }

        @Test
        void division_HasJsonIgnorePropertiesAnnotation() {
            // Assert
            assertThat(BankHolidays.Division.class.isAnnotationPresent(JsonIgnoreProperties.class)).isTrue();
            JsonIgnoreProperties annotation = BankHolidays.Division.class.getAnnotation(JsonIgnoreProperties.class);
            assertThat(annotation.ignoreUnknown()).isTrue();
        }

        @Test
        void events_HasJsonPropertyAnnotation() throws NoSuchFieldException {
            // Act
            Field field = BankHolidays.Division.class.getField("events");

            // Assert
            assertThat(field.isAnnotationPresent(JsonProperty.class)).isTrue();
            JsonProperty annotation = field.getAnnotation(JsonProperty.class);
            assertThat(annotation.value()).isEqualTo("events");
        }

        @Test
        void events_IsPublic() throws NoSuchFieldException {
            // Act
            Field field = BankHolidays.Division.class.getField("events");

            // Assert
            assertThat(Modifier.isPublic(field.getModifiers())).isTrue();
        }

        @Test
        void events_IsListOfEventDate() throws NoSuchFieldException {
            // Act
            Field field = BankHolidays.Division.class.getField("events");

            // Assert
            assertThat(field.getGenericType().getTypeName()).isEqualTo(
                "java.util.List<uk.gov.hmcts.reform.civil.bankholidays.BankHolidays$Division$EventDate>");
        }
    }

    @Nested
    class EventDateClassTests {

        @Test
        void eventDate_IsStaticClass() {
            // Assert
            assertThat(Modifier.isStatic(BankHolidays.Division.EventDate.class.getModifiers())).isTrue();
            assertThat(Modifier.isPublic(BankHolidays.Division.EventDate.class.getModifiers())).isTrue();
        }

        @Test
        void eventDate_HasJsonIgnorePropertiesAnnotation() {
            // Assert
            assertThat(BankHolidays.Division.EventDate.class.isAnnotationPresent(JsonIgnoreProperties.class)).isTrue();
            JsonIgnoreProperties annotation = BankHolidays.Division.EventDate.class.getAnnotation(JsonIgnoreProperties.class);
            assertThat(annotation.ignoreUnknown()).isTrue();
        }

        @Test
        void date_HasJsonPropertyAnnotation() throws NoSuchFieldException {
            // Act
            Field field = BankHolidays.Division.EventDate.class.getField("date");

            // Assert
            assertThat(field.isAnnotationPresent(JsonProperty.class)).isTrue();
            JsonProperty annotation = field.getAnnotation(JsonProperty.class);
            assertThat(annotation.value()).isEqualTo("date");
        }

        @Test
        void date_HasJsonDeserializeAnnotation() throws NoSuchFieldException {
            // Act
            Field field = BankHolidays.Division.EventDate.class.getField("date");

            // Assert
            assertThat(field.isAnnotationPresent(JsonDeserialize.class)).isTrue();
            JsonDeserialize annotation = field.getAnnotation(JsonDeserialize.class);
            assertThat(annotation.using()).isEqualTo(LocalDateDeserializer.class);
        }

        @Test
        void date_IsPublic() throws NoSuchFieldException {
            // Act
            Field field = BankHolidays.Division.EventDate.class.getField("date");

            // Assert
            assertThat(Modifier.isPublic(field.getModifiers())).isTrue();
        }

        @Test
        void date_IsOfTypeLocalDate() throws NoSuchFieldException {
            // Act
            Field field = BankHolidays.Division.EventDate.class.getField("date");

            // Assert
            assertThat(field.getType()).isEqualTo(LocalDate.class);
        }
    }

    @Nested
    class JsonSerializationTests {

        @Test
        void deserialize_ValidJson_CreatesBankHolidaysObject() throws Exception {
            // Arrange
            String json = """
                {
                    "england-and-wales": {
                        "events": [
                            {"date": "2023-01-02", "title": "New Year's Day (substitute day)"},
                            {"date": "2023-04-07", "title": "Good Friday"},
                            {"date": "2023-04-10", "title": "Easter Monday"}
                        ]
                    },
                    "scotland": {
                        "events": [
                            {"date": "2023-01-02", "title": "2nd January"}
                        ]
                    }
                }
                """;

            // Act
            BankHolidays result = objectMapper.readValue(json, BankHolidays.class);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.englandAndWales).isNotNull();
            assertThat(result.englandAndWales.events).hasSize(3);
            assertThat(result.englandAndWales.events.get(0).date).isEqualTo(LocalDate.of(2023, 1, 2));
            assertThat(result.englandAndWales.events.get(1).date).isEqualTo(LocalDate.of(2023, 4, 7));
            assertThat(result.englandAndWales.events.get(2).date).isEqualTo(LocalDate.of(2023, 4, 10));
        }

        @Test
        void deserialize_EmptyEvents_CreatesEmptyList() throws Exception {
            // Arrange
            String json = """
                {
                    "england-and-wales": {
                        "events": []
                    }
                }
                """;

            // Act
            BankHolidays result = objectMapper.readValue(json, BankHolidays.class);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.englandAndWales).isNotNull();
            assertThat(result.englandAndWales.events).isEmpty();
        }

        @Test
        void deserialize_MissingEnglandAndWales_CreatesObjectWithNull() throws Exception {
            // Arrange
            String json = """
                {
                    "scotland": {
                        "events": []
                    }
                }
                """;

            // Act
            BankHolidays result = objectMapper.readValue(json, BankHolidays.class);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.englandAndWales).isNull();
        }

        @Test
        void deserialize_UnknownProperties_IgnoresThemDueToAnnotation() throws Exception {
            // Arrange
            String json = """
                {
                    "england-and-wales": {
                        "events": [{"date": "2023-01-01"}],
                        "unknown-property": "value"
                    },
                    "unknown-division": {}
                }
                """;

            // Act
            BankHolidays result = objectMapper.readValue(json, BankHolidays.class);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.englandAndWales).isNotNull();
            assertThat(result.englandAndWales.events).hasSize(1);
        }

        @Test
        void serialize_ValidObject_CreatesJson() throws Exception {
            // Arrange
            BankHolidays.Division division = new BankHolidays.Division();
            BankHolidays.Division.EventDate event1 = new BankHolidays.Division.EventDate();
            event1.date = LocalDate.of(2023, 1, 1);
            BankHolidays.Division.EventDate event2 = new BankHolidays.Division.EventDate();
            event2.date = LocalDate.of(2023, 12, 25);
            division.events = Arrays.asList(event1, event2);
            BankHolidays bankHolidays = new BankHolidays();
            bankHolidays.englandAndWales = division;

            // Act
            String json = objectMapper.writeValueAsString(bankHolidays);

            // Assert
            for (String s : Arrays.asList(
                "\"england-and-wales\"",
                "\"events\"",
                "\"date\":\"2023-01-01\"",
                "\"date\":\"2023-12-25\""
            )) {
                assertThat(json).contains(s);
            }
        }

        @Test
        void serialize_NullEnglandAndWales_HandlesGracefully() throws Exception {
            // Arrange
            BankHolidays bankHolidays = new BankHolidays();

            // Act
            String json = objectMapper.writeValueAsString(bankHolidays);

            // Assert
            assertThat(json).contains("\"england-and-wales\":null");
        }

        @Test
        void serialize_EmptyEvents_SerializesAsEmptyArray() throws Exception {
            // Arrange
            BankHolidays bankHolidays = new BankHolidays();
            BankHolidays.Division division = new BankHolidays.Division();
            division.events = Collections.emptyList();
            bankHolidays.englandAndWales = division;

            // Act
            String json = objectMapper.writeValueAsString(bankHolidays);

            // Assert
            assertThat(json).contains("\"events\":[]");
        }
    }
}
