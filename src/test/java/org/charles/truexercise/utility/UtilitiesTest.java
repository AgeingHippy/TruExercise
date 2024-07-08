package org.charles.truexercise.utility;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UtilitiesTest {

    @ParameterizedTest
    @MethodSource("maskData")
    void testMask(String inputString, String maskedString ) {
        assertEquals(maskedString,Utilities.maskString(inputString));
    }

    @Test
    void brokenTestToVerifyGithubWorkflowIsInFactRunningTests() {
        assert(false);
    }

    private static Stream<Object[]> maskData() {

        return Stream.of(
                //inputString, maskedString
                new Object[]{null, null},               //1 -null String provided
                new Object[]{"","" },                   //2 - Empty String
                new Object[]{"abc","abc" },             //3 - Length less than 6 characters
                new Object[]{"abcdef","abcdef" },         //4 - length = 6 characters
                new Object[]{"abcdefg","ab*defg" }        //5 - length > 5 characters
        );
    }

}
