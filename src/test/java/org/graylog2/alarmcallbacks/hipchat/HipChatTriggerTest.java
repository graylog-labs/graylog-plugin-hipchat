package org.graylog2.alarmcallbacks.hipchat;

import static org.junit.Assert.assertEquals;

import org.graylog2.plugin.alarms.AlertCondition;
import org.graylog2.plugin.alarms.AlertCondition.CheckResult;
import org.graylog2.plugin.alarms.callbacks.AlarmCallbackConfigurationException;
import org.graylog2.plugin.alarms.callbacks.AlarmCallbackException;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.configuration.ConfigurationException;
import org.graylog2.plugin.streams.Stream;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import static org.mockito.Mockito.*;

public class HipChatTriggerTest {

    private HipChatAlarmCallback alarmCallback;
    private AlertCondition condition;
    private Stream stream;
    private CheckResult alert;
    
    @Before
    public void setUp() {
        alarmCallback = new HipChatAlarmCallback();
        condition = mock(AlertCondition.class);
        stream = mock(Stream.class);
        alert = mock(CheckResult.class);
        
        when(condition.getStream()).thenReturn(stream);
        when(condition.getStream().getTitle()).thenReturn("stream_name");		
		when(alert.getResultDescription()).thenReturn("description");

    }
	
    @Test
    public void testMsgTemplateNameIsReplaced() 
    		throws AlarmCallbackConfigurationException, ConfigurationException, AlarmCallbackException {
        final Configuration configuration = new Configuration(ImmutableMap.<String, Object>of(
                "api_token", "TEST_api_token",
                "room", "TEST_room",
                "color", "yellow",
                "notify", true,
                "msg_template", "Stream <<name>>"

        ));
        alarmCallback.initialize(configuration);
        alarmCallback.checkConfiguration();
                
        final HipChatTrigger trigger = new HipChatTrigger(configuration);

        assertEquals(trigger.buildRoomNotification(condition, alert).message, "Stream <stream_name>");
   }

    @Test
    public void testMsgTemplateDescriptionIsReplaced() 
    		throws AlarmCallbackConfigurationException, ConfigurationException, AlarmCallbackException {
        final Configuration configuration = new Configuration(ImmutableMap.<String, Object>of(
                "api_token", "TEST_api_token",
                "room", "TEST_room",
                "color", "yellow",
                "notify", true,
                "msg_template", "Stream <description>"

        ));
        alarmCallback.initialize(configuration);
        alarmCallback.checkConfiguration();
                
        final HipChatTrigger trigger = new HipChatTrigger(configuration);

        assertEquals(trigger.buildRoomNotification(condition, alert).message, "Stream description");
   }

    @Test
    public void testMsgTemplateNameAndDescriptionAreReplaced() 
    		throws AlarmCallbackConfigurationException, ConfigurationException, AlarmCallbackException {
        final Configuration configuration = new Configuration(ImmutableMap.<String, Object>of(
                "api_token", "TEST_api_token",
                "room", "TEST_room",
                "color", "yellow",
                "notify", true,
                "msg_template", "Stream <<name>> alert: <description>"

        ));
        alarmCallback.initialize(configuration);
        alarmCallback.checkConfiguration();
                
        final HipChatTrigger trigger = new HipChatTrigger(configuration);

        assertEquals(trigger.buildRoomNotification(condition, alert).message, "Stream <stream_name> alert: description");
   }

}
