/**
 * Copyright 2018, 2019, 2020, 2021 SourceLab.org https://github.com/SourceLabOrg/kafka-connect-client
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.sourcelab.kafka.connect.apiclient.util;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(DataProviderRunner.class)
public class UrlEscapingUtilTest {

    /**
     * Validate escaping works as expected.
     */
    @Test
    @UseDataProvider("provideTestCases")
    public void doTest(final String input, final String expectedResult) {
        final String result = UrlEscapingUtil.escapePath(input);
        assertEquals("Unexpected result!", expectedResult, result);
    }

    /**
     * To be honest I'm not entirely sure if this is the appropriate escaping for paths,
     * but aiming for backwards compatibility, and until someone complains and provides a test case
     * that is incorrect, I'm just going with the below sample values.
     */
    @DataProvider
    public static Object[][] provideTestCases() {
        return new Object[][] {
            // The alphanumeric characters "a" through "z", "A" through "Z" and "0" through "9" remain the same.
            { "easyInput", "easyInput" },
            { "123", "123" },
            { "abC123zxY", "abC123zxY" },

            // The unreserved characters ".", "-", "~", and "_" remain the same.
            { "easy-input", "easy-input" },
            { "easy.input", "easy.input" },
            { "easy~input", "easy~input" },
            { "easy_input", "easy_input" },

            // The general delimiters "@" and ":" remain the same.

            { "easy@input", "easy@input" },
            { "easy:input", "easy:input" },

            // The subdelimiters "!", "$", "&", "'", "(", ")", "*", "+", ",", ";", and "=" remain the same.
            { "easy!input", "easy!input" },
            { "easy$input", "easy$input" },
            { "easy&input", "easy&input" },
            { "easy'input", "easy'input" },
            { "easy(input)", "easy(input)" },
            { "easy*input", "easy*input" },
            { "easy+input", "easy+input" },
            { "easy,input", "easy,input" },
            { "easy;input", "easy;input" },
            { "easy=input", "easy=input" },

            // The space character " " is converted into %20.
            { "123 456", "123%20456" },
            { "123  456", "123%20%20456" },
            { " 123  456", "%20123%20%20456" },
            { "  123  456 ", "%20%20123%20%20456%20" },

            // Others
            { "12\\3", "12%5C3" },
            { "12/3/4", "12%2F3%2F4" },
            { "?easy=input&other=value", "%3Feasy=input&other=value" },

            // All other characters are converted into one or more bytes using UTF-8 encoding and each byte is then
            // represented by the 3-character string "%XY", where "XY" is the two-digit, uppercase, hexadecimal representation of the byte value.
            { "しんちゃん", "%E3%81%97%E3%82%93%E3%81%A1%E3%82%83%E3%82%93" },
            { "無料", "%E7%84%A1%E6%96%99" },
            { "abcdÈfghí", "abcd%C3%88fgh%C3%AD" },
        };
    }
}