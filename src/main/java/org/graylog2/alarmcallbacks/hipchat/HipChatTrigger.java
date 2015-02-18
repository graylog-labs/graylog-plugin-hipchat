/**
 * Copyright 2013-2014 TORCH GmbH, 2015 Graylog, Inc.
 *
 * This file is part of Graylog.
 *
 * Graylog is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Graylog is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Graylog.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.graylog2.alarmcallbacks.hipchat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.graylog2.plugin.alarms.AlertCondition;
import org.graylog2.plugin.alarms.callbacks.AlarmCallbackException;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class HipChatTrigger {
    private final String apiToken;
    private final String room;
    private final String color;
    private final boolean notify;
    private final ObjectMapper objectMapper;

    public HipChatTrigger(final String apiToken, final String room, final String color, final boolean notify) {
        this(apiToken, room, color, notify, new ObjectMapper());
    }

    HipChatTrigger(final String apiToken, final String room, final String color, final boolean notify,
                   final ObjectMapper objectMapper) {
        this.apiToken = apiToken;
        this.room = room;
        this.color = color;
        this.notify = notify;
        this.objectMapper = objectMapper;
    }

    public void trigger(AlertCondition condition, AlertCondition.CheckResult alert) throws AlarmCallbackException {
        final URL url;
        try {
            url = new URL("https://api.hipchat.com/v2/room/" + URLEncoder.encode(room, "UTF-8") + "/notification");
        } catch (MalformedURLException | UnsupportedEncodingException e) {
            throw new AlarmCallbackException("Error while constructing URL of HipChat API.", e);
        }

        final HttpURLConnection conn;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.addRequestProperty("Authorization", "Bearer " + apiToken);
            conn.addRequestProperty("Content-Type", "application/json");
        } catch (IOException e) {
            throw new AlarmCallbackException("Could not open connection to HipChat API", e);
        }

        try (final OutputStream outputStream = conn.getOutputStream()) {
            outputStream.write(objectMapper.writeValueAsBytes(buildRoomNotification(condition, alert)));
            outputStream.flush();

            if (conn.getResponseCode() != 204) {
                throw new AlarmCallbackException("Unexpected HTTP response status " + conn.getResponseCode());
            }
        } catch (IOException e) {
            throw new AlarmCallbackException("Could not POST event trigger to HipChat API", e);
        }
    }

    private RoomNotification buildRoomNotification(AlertCondition condition, AlertCondition.CheckResult alert) {
        // See https://www.hipchat.com/docs/apiv2/method/send_room_notification for valid parameters
        final String message = String.format("Stream <%s> alert: %s",
                condition.getStream().getTitle(),
                alert.getResultDescription()
        );

        return new RoomNotification(message, color, notify);
    }

    public static class RoomNotification {
        @JsonProperty
        public String message;
        @JsonProperty
        public boolean notify = true;
        @JsonProperty("message_format")
        public final String messageFormat = "text";
        @JsonProperty
        public String color = "yellow";

        public RoomNotification(String message, String color, boolean notify) {
            this.message = message;
            this.color = color;
            this.notify = notify;
        }
    }
}