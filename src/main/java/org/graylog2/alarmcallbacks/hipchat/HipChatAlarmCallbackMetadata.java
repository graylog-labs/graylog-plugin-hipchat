package org.graylog2.alarmcallbacks.hipchat;

import org.graylog2.plugin.PluginMetaData;
import org.graylog2.plugin.Version;

import java.net.URI;

public class HipChatAlarmCallbackMetadata implements PluginMetaData {
    @Override
    public String getUniqueId() {
        return HipChatAlarmCallback.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "HipChat Alarmcallback Plugin";
    }

    @Override
    public String getAuthor() {
        return "TORCH GmbH";
    }

    @Override
    public URI getURL() {
        return URI.create("http://www.torch.sh");
    }

    @Override
    public Version getVersion() {
        return new Version(1, 0, 0);
    }

    @Override
    public String getDescription() {
        return "Alarm callback plugin that sends all stream alerts to a defined HipChat room.";
    }

    @Override
    public Version getRequiredVersion() {
        return new Version(0, 21, 0);
    }
}
