package org.graylog2.alarmcallbacks.hipchat;

import org.graylog2.plugin.PluginModule;

public class HipChatAlarmCallbackModule extends PluginModule {
    @Override
    protected void configure() {
        registerPlugin(HipChatAlarmCallbackMetadata.class);
        addAlarmCallback(HipChatAlarmCallback.class);
    }
}
