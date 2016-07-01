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
import com.floreysoft.jmte.Engine;
import com.google.common.base.Strings;
import org.graylog2.alerts.FormattedEmailAlertSender;
import org.graylog2.plugin.Message;
import org.graylog2.plugin.MessageSummary;
import org.graylog2.plugin.Tools;
import org.graylog2.plugin.alarms.AlertCondition;
import org.graylog2.plugin.alarms.callbacks.AlarmCallbackException;
import org.graylog2.plugin.streams.Stream;
import org.joda.time.DateTime;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HipChatTrigger {

    private final String apiToken;
    private final String room;
    private final String color;
    private final boolean notify;
    private final String apiURL;
    private final String messageTemplate;
    private final ObjectMapper objectMapper;
    private final URI graylogBaseUrl;
    private final Engine engine = new Engine();

    public HipChatTrigger(final String apiToken, final String room, final String color, final boolean notify,
                          final String apiURL, final String messageTemplate, final URI graylogBaseUrl) {
        this(apiToken, room, color, notify, apiURL, new ObjectMapper(), messageTemplate, graylogBaseUrl);
    }

    HipChatTrigger(final String apiToken, final String room, final String color, final boolean notify,
                   final String apiURL, final ObjectMapper objectMapper, final String messageTemplate, final URI graylogBaseUrl) {
        this.apiToken = apiToken;
        this.room = room;
        this.color = color;
        this.notify = notify;
        this.apiURL = apiURL;
        this.objectMapper = objectMapper;
        this.messageTemplate = messageTemplate;
        this.graylogBaseUrl = graylogBaseUrl;
    }

    public void trigger(AlertCondition condition, AlertCondition.CheckResult alert) throws AlarmCallbackException {
        final URL url;
        try {
            if (Strings.isNullOrEmpty(apiURL)) {
                url = new URL("https://api.hipchat.com/v2/room/" + URLEncoder.encode(room, "UTF-8") + "/notification");
            } else {
                url = new URL(apiURL + "/v2/room/" + URLEncoder.encode(room, "UTF-8") + "/notification");
            }
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

    //TODO: Copied from StaticEmailAlertSender, could be moved to a utility class?
    public static String buildStreamDetailsURL(URI baseUri, AlertCondition.CheckResult checkResult, Stream stream) {
        if (baseUri != null && !Strings.isNullOrEmpty(baseUri.getHost())) {
            int time = 5;
            if (checkResult.getTriggeredCondition().getParameters().get("time") != null) {
                time = ((Integer) checkResult.getTriggeredCondition().getParameters().get("time")).intValue();
            }
            DateTime dateAlertEnd = checkResult.getTriggeredAt();
            DateTime dateAlertStart = dateAlertEnd.minusMinutes(time);
            String alertStart = Tools.getISO8601String(dateAlertStart);
            String alertEnd = Tools.getISO8601String(dateAlertEnd);
            return baseUri + "/streams/" + stream.getId() + "/messages?rangetype=absolute&from=" + alertStart + "&to=" + alertEnd + "&q=*";
        } else {
            return "Please configure \'transport_email_web_interface_url\' in your Graylog configuration file.";
        }
    }

    protected static boolean invalidTemplate(String template) {
        return template == null || template.trim().isEmpty();
    }

    //TODO: Copied and adapted from FormattedEmailAlertSender; could be refactored into a general utility for alerts?
    private String buildBody(AlertCondition condition, AlertCondition.CheckResult alert) {
        String template;
        if (invalidTemplate(this.messageTemplate)) {
            template = FormattedEmailAlertSender.bodyTemplate;
        } else {
            template = this.messageTemplate;
        }
        Map<String, Object> model = this.getModel(condition, alert);
        return this.engine.transform(template, model);
    }

    //TODO: Copied from FormattedEmailAlertSender
    private Map<String, Object> getModel(AlertCondition condition, AlertCondition.CheckResult alert) {
        Stream stream = condition.getStream();
        List<Message> messages = new ArrayList<>();
        for (MessageSummary messageSummary : alert.getMatchingMessages()) {
            messages.add(messageSummary.getRawMessage());
        }
        HashMap<String, Object> model = new HashMap<>();
        model.put("stream", stream);
        model.put("check_result", alert);
        if (graylogBaseUrl != null) {
            model.put("stream_url", buildStreamDetailsURL(graylogBaseUrl, alert, stream));
        }
        model.put("backlog", messages);
        model.put("backlog_size", messages.size());
        return model;
    }

    private RoomNotification buildRoomNotification(AlertCondition condition, AlertCondition.CheckResult alert) {
        final String message = this.buildBody(condition, alert);
        return new RoomNotification(message, color, notify);
    }

    // See https://www.hipchat.com/docs/apiv2/method/send_room_notification for valid parameters
    public static class RoomNotification {
        @JsonProperty
        public String message;
        @JsonProperty
        public boolean notify = true;
        @JsonProperty("message_format")
        public final String messageFormat = "html";
        @JsonProperty
        public String color = "yellow";

        public RoomNotification(String message, String color, boolean notify) {
            this.message = message;
            this.color = color;
            this.notify = notify;
        }
    }
}