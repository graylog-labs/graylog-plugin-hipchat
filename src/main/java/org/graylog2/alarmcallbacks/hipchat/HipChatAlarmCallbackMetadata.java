/**
 * Copyright 2013-2014 TORCH GmbH
 *
 * This file is part of Graylog2.
 *
 * Graylog2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Graylog2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Graylog2.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

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
        return new Version(0, 90, 0);
    }

    @Override
    public String getDescription() {
        return "Alarm callback plugin that sends all stream alerts to a HipChat room.";
    }

    @Override
    public Version getRequiredVersion() {
        return new Version(0, 90, 0);
    }
}
