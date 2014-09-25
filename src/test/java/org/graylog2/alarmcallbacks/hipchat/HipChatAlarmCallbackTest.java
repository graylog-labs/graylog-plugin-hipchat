package org.graylog2.alarmcallbacks.hipchat;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import org.graylog2.plugin.alarms.callbacks.AlarmCallbackConfigurationException;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.configuration.ConfigurationException;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertThat;

public class HipChatAlarmCallbackTest {
    private HipChatAlarmCallback alarmCallback;

    @Before
    public void setUp() {
        alarmCallback = new HipChatAlarmCallback();
    }

    @Test
    public void testInitialize() throws AlarmCallbackConfigurationException {
        final Configuration configuration = new Configuration(ImmutableMap.<String, Object>of(
                "api_token", "TEST_api_token",
                "room", "TEST_room"
        ));
        alarmCallback.initialize(configuration);
    }

    @Test
    public void testGetAttributes() throws AlarmCallbackConfigurationException {
        final Configuration configuration = new Configuration(ImmutableMap.<String, Object>of(
                "api_token", "TEST_api_token",
                "room", "TEST_room"
        ));
        alarmCallback.initialize(configuration);

        final Map<String, Object> attributes = alarmCallback.getAttributes();
        assertThat(attributes.keySet(), hasItems("api_token", "room"));
        assertThat((String) attributes.get("api_token"), equalTo("****"));
    }

    @Test
    public void checkConfigurationSucceedsWithValidConfiguration()
            throws AlarmCallbackConfigurationException, ConfigurationException {
        final Configuration configuration = new Configuration(ImmutableMap.<String, Object>of(
                "api_token", "TEST_api_token",
                "room", "TEST_room"
        ));
        alarmCallback.initialize(configuration);
        alarmCallback.checkConfiguration();
    }

    @Test(expected = ConfigurationException.class)
    public void checkConfigurationFailsIfApiTokenIsMissing()
            throws AlarmCallbackConfigurationException, ConfigurationException {
        final Configuration configuration = new Configuration(ImmutableMap.<String, Object>of(
                "room", "TEST_room"
        ));
        alarmCallback.initialize(configuration);
        alarmCallback.checkConfiguration();
    }

    @Test(expected = ConfigurationException.class)
    public void checkConfigurationFailsIfRoomIsMissing()
            throws AlarmCallbackConfigurationException, ConfigurationException {
        final Configuration configuration = new Configuration(ImmutableMap.<String, Object>of(
                "api_token", "TEST_api_token"
        ));
        alarmCallback.initialize(configuration);
        alarmCallback.checkConfiguration();
    }

    @Test(expected = ConfigurationException.class)
    public void checkConfigurationFailsIfRoomIsTooLong()
            throws AlarmCallbackConfigurationException, ConfigurationException {
        final Configuration configuration = new Configuration(ImmutableMap.<String, Object>of(
                "api_token", "TEST_api_token",
                "room", Strings.repeat("a", 101)
        ));
        alarmCallback.initialize(configuration);
        alarmCallback.checkConfiguration();
    }

    @Test
    public void testGetRequestedConfiguration() {
        assertThat(alarmCallback.getRequestedConfiguration().asList().keySet(),
                hasItems("api_token", "room"));
    }

    @Test
    public void testGetName() {
        assertThat(alarmCallback.getName(), equalTo("HipChat alarm callback"));
    }
}