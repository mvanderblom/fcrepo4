/**
 * Copyright 2015 DuraSpace, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fcrepo.kernel.impl.observer.eventmappings;

import static javax.jcr.observation.Event.NODE_ADDED;
import static javax.jcr.observation.Event.PROPERTY_ADDED;
import static javax.jcr.observation.Event.PROPERTY_CHANGED;
import static org.jgroups.util.UUID.randomUUID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.modeshape.jcr.api.JcrConstants.JCR_CONTENT;

import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;

import org.fcrepo.kernel.exception.RepositoryRuntimeException;
import org.fcrepo.kernel.observer.FedoraEvent;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.stream.Stream;

/**
 * <p>AllNodeEventsOneEventTest class.</p>
 *
 * @author ajs6f
 */
@RunWith(MockitoJUnitRunner.class)
public class AllNodeEventsOneEventTest {

    private static final String TEST_PATH1 = "/test/node1";

    private static final String TEST_PATH2 = TEST_PATH1 + "/dc:title";

    private static final String TEST_IDENTIFIER3 = randomUUID().toString();

    private static final String TEST_PATH3 = "/test/node2";

    private static final String TEST_PATH4 = "/test/node3";

    private static final String TEST_PATH5 = "/test/node3/" + JCR_CONTENT;

    private final AllNodeEventsOneEvent testMapping = new AllNodeEventsOneEvent();

    @Mock
    private Event mockEvent1, mockEvent2, mockEvent3;

    @Mock
    private Event mockEvent4;

    @Mock
    private Event mockEvent5;

    private Stream<Event> testStream, testStream2, testStream3;

    @Before
    public void setUp() throws RepositoryException {
        when(mockEvent1.getPath()).thenReturn(TEST_PATH1);
        when(mockEvent1.getType()).thenReturn(NODE_ADDED);
        when(mockEvent2.getPath()).thenReturn(TEST_PATH2);
        when(mockEvent2.getType()).thenReturn(PROPERTY_ADDED);
        when(mockEvent3.getIdentifier()).thenReturn(TEST_IDENTIFIER3);
        when(mockEvent3.getPath()).thenReturn(TEST_PATH3);
        when(mockEvent3.getType()).thenReturn(PROPERTY_CHANGED);
        when(mockEvent4.getPath()).thenReturn(TEST_PATH4);
        when(mockEvent4.getType()).thenReturn(NODE_ADDED);
        when(mockEvent5.getPath()).thenReturn(TEST_PATH5);
        when(mockEvent5.getType()).thenReturn(NODE_ADDED);
        testStream = Stream.of(mockEvent1, mockEvent2, mockEvent3);
        testStream2 = Stream.of(mockEvent5, mockEvent4);
        testStream3 = Stream.of(mockEvent4, mockEvent5);
    }

    @Test
    public void testCardinality() {
        assertEquals("Didn't get 2 FedoraEvents for 3 input JCR Events, two of which were on the same node!", 2,
                testMapping.apply(testStream).count());
    }

    @Test
    public void testCollapseContentEvents() {
        assertEquals("Didn't collapse content node and fcr:content events!", 1, testMapping.apply(testStream3).count());
    }

    @Test
    @Ignore
    public void testFileEventProperties() {
        final Stream<FedoraEvent> result = testMapping.apply(testStream2);
        final FedoraEvent e = result.findFirst().get();
        assertTrue("Didn't add fedora:hasContent property to fcr:content events!: " + e.getProperties(),
                e.getProperties().contains("fedora:hasContent"));
    }

    @Test(expected = RuntimeException.class)
    public void testBadEvent() throws RepositoryException {
        reset(mockEvent1);
        when(mockEvent1.getIdentifier()).thenThrow(new RepositoryException("Expected."));
        testMapping.apply(testStream);
    }

    @Test
    public void testPropertyEvents() {
        final Stream<FedoraEvent> result = testMapping.apply(testStream);
        assertNotNull(result);
        assertTrue("Result is empty!", result.findAny().isPresent());
    }

    @Test
    public void testProperty() {
        final Stream<FedoraEvent> result = testMapping.apply(testStream);
        assertTrue("Third mock event was not found!", result
                .anyMatch(e -> TEST_IDENTIFIER3.equals(e.getIdentifier())));
    }

    @Test(expected = RepositoryRuntimeException.class)
    public void testError() throws RepositoryException {
        reset(mockEvent2);
        when(mockEvent2.getPath()).thenThrow(new RepositoryException("Expected."));
        final Stream<FedoraEvent> result = testMapping.apply(testStream);
        assertNotNull(result);
        result.count();
    }

}
