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

package org.sourcelab.kafka.connect.apiclient.request.get.connector;

import org.junit.Test;
import org.sourcelab.kafka.connect.apiclient.request.AbstractRequestTest;
import org.sourcelab.kafka.connect.apiclient.request.get.GetConnectors;

import java.io.IOException;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class GetConnectorsTest extends AbstractRequestTest {

    /**
     * Test Parsing GET /connectors response.
     */
    @Test
    public void testParseResponse() throws IOException {
        final String mockResponse = readFile("getConnectors.json");
        final Collection<String> result = new GetConnectors().parseResponse(mockResponse);

        // Validate
        assertNotNull("Should not be null", result);
        assertEquals("Should have two entries", 2, result.size());
        assertTrue("Should have connector", result.contains("My Test Connector"));
        assertTrue("Should have connector", result.contains("My Other Test Connector"));
    }

    @Override
    public void getApiEndpoint() {
        final String expectedUrl = "/connectors";
        final String result = new GetConnectors().getApiEndpoint();
        assertEquals("Unexpected URL returned!", expectedUrl, result);
    }
}