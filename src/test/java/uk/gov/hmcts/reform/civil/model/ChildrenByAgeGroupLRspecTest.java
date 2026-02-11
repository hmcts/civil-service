package uk.gov.hmcts.reform.civil.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChildrenByAgeGroupLRspecTest {

    @Test
    void shouldReturnZeroWhenAllFieldsAreNull() {
        //Given
        ChildrenByAgeGroupLRspec childrenByAgeGroupLRspec = new ChildrenByAgeGroupLRspec();
        //When
        int result = childrenByAgeGroupLRspec.getTotalChildren();
        //Then
        assertThat(result).isZero();
    }

    @Test
    void shouldReturnSumOfThoseFieldsThatAreNotNull() {
        //Given
        ChildrenByAgeGroupLRspec childrenByAgeGroupLRspec = new ChildrenByAgeGroupLRspec()
            .setNumberOfElevenToFifteen("2")
            .setNumberOfSixteenToNineteen("1");
        int expectedNumber = 3;
        //When
        int result = childrenByAgeGroupLRspec.getTotalChildren();
        //Then
        assertThat(result).isEqualTo(expectedNumber);
    }

}
