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

package org.graylog2.alarmcallbacks.hipchat;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import org.graylog2.plugin.alarms.AlertCondition;
import org.graylog2.plugin.alarms.callbacks.AlarmCallbackException;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

public class HipChatTrigger {
    private final String apiToken;
    private final String room;
    private final String senderName;

    public HipChatTrigger(final String apiToken, final String room, final String senderName) {
        this.apiToken = apiToken;
        this.room = room;
        this.senderName = senderName;
    }

    public void trigger(AlertCondition alertCondition) throws AlarmCallbackException {
        final URL url;
        try {
            url = new URL("https://api.hipchat.com/v1/rooms/message?auth_token=" + this.apiToken);
        } catch (MalformedURLException e) {
            throw new AlarmCallbackException("Error while constructing URL of HipChat API.", e);
        }

        final HttpURLConnection conn;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
        } catch (IOException e) {
            throw new AlarmCallbackException("Could not open connection to HipChat API", e);
        }

        try (final Writer writer = new OutputStreamWriter(conn.getOutputStream())) {
            writer.write(buildParametersFromAlertCondition(alertCondition));
            writer.flush();

            if (conn.getResponseCode() != 200) {
                throw new AlarmCallbackException("Unexpected HTTP response status " + conn.getResponseCode());
            }
        } catch (IOException e) {
            throw new AlarmCallbackException("Could not POST event trigger to HipChat API", e);
        }
    }

    private String buildParametersFromAlertCondition(AlertCondition alertCondition) throws UnsupportedEncodingException {
        // See https://www.hipchat.com/docs/api/method/rooms/message for valid parameters
        final Map<String, String> params = ImmutableMap.of(
                "room_id", URLEncoder.encode(room, "UTF-8"),
                "from", URLEncoder.encode(senderName, "UTF-8"),
                "message", URLEncoder.encode(alertCondition.getDescription(), "UTF-8"),
                "message_format", "text",
                "notify", "1");

        return Joiner.on('&')
                .skipNulls()
                .withKeyValueSeparator("=")
                .join(params);
    }
}