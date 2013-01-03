/**
 * Copyright 2013 Lennart Koopmann <lennart@socketfeed.com>
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

package org.graylog2.hipchatalarmcallback.callback;

import java.util.HashMap;
import java.util.Map;
import org.graylog2.plugin.alarms.Alarm;
import org.graylog2.plugin.alarms.callbacks.AlarmCallback;
import org.graylog2.plugin.alarms.callbacks.AlarmCallbackConfigurationException;
import org.graylog2.plugin.alarms.callbacks.AlarmCallbackException;

/**
 * @author Lennart Koopmann <lennart@socketfeed.com>
 */
public class HipChatAlarmCallback implements AlarmCallback {
    
    public static final String NAME = "HipChat alarm callback";
    
    private String apiToken;
    private String room;

    public void initialize(Map<String, String> config) throws AlarmCallbackConfigurationException {
        if (!configSet(config, "api_token")) {
          throw new AlarmCallbackConfigurationException("Required config parameter api_token is missing.");
        }

        if (!configSet(config, "room")) {
          throw new AlarmCallbackConfigurationException("Required config parameter room is missing.");
        }

        this.apiToken = ((String)config.get("api_token"));
        this.room = ((String)config.get("room"));
    }

    public void call(Alarm alarm) throws AlarmCallbackException {
        HipChatTrigger trigger = new HipChatTrigger(this.apiToken, this.room);
        trigger.trigger(alarm);
    }

    public Map<String, String> getRequestedConfiguration() {
        Map<String, String> config = new HashMap();

        config.put("api_token", "Notification API token");
        config.put("room", "ID or name of room");

        return config;
    }

    public String getName() {
        return "HipChat alarm callback";
    }

    private boolean configSet(Map<String, String> config, String key) {
        return config != null && config.containsKey(key) && !config.get(key).isEmpty();
    }
}