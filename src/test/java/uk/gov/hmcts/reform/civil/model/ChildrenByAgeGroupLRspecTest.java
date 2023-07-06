package uk.gov.hmcts.reform.civil.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChildrenByAgeGroupLRspecTest {

    @Test
    void shouldReturnZeroWhenAllFieldsAreNull() {
        //Given
        ChildrenByAgeGroupLRspec childrenByAgeGroupLRspec = ChildrenByAgeGroupLRspec.builder().build();
        //When
        int result = childrenByAgeGroupLRspec.getTotalChildren();
        //Then
        assertThat(result).isZero();
    }

    @Test
    void shouldReturnSumOfThoseFieldsThatAreNotNull() {
        //Given
        ChildrenByAgeGroupLRspec childrenByAgeGroupLRspec = ChildrenByAgeGroupLRspec.builder()
            .numberOfElevenToFifteen("2")
            .numberOfSixteenToNineteen("1")
            .build();
        int expectedNumber = 3;
        //When
        int result = childrenByAgeGroupLRspec.getTotalChildren();
        //Then
        assertThat(result).isEqualTo(expectedNumber);
    }

}
