package org.bj.examples.trivia.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class SlackUtilsTest {
    @Test
    public void testNormalizeIdWithNullSlackId() {
        assertThat(SlackUtils.normalizeId(null), is(nullValue()));
    }

    @Test
    public void testNormalizeIdWithInvalidSlackId() {
        assertThat(SlackUtils.normalizeId("garbage"), is(equalTo("garbage")));
    }

    @Test
    public void testNormalizeIdWithValidSlackId() {
        assertThat(SlackUtils.normalizeId("<@U12345>"), is(equalTo("U12345")));
    }

    @Test
    public void testNormalizeIdWithValidSlackIdAndUsername() {
        assertThat(SlackUtils.normalizeId("<@U12345|jsmith>"), is(equalTo("U12345")));
    }

    @Test
    public void testNormalizeIdWithValidSlackIdAndEmptyUsername() {
        assertThat(SlackUtils.normalizeId("<@U12345|>"), is(equalTo("U12345")));
    }
}
