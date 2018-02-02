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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.graylog2.plugin.alarms.AlertCondition;
import org.graylog2.plugin.alarms.callbacks.AlarmCallback;
import org.graylog2.plugin.alarms.callbacks.AlarmCallbackConfigurationException;
import org.graylog2.plugin.alarms.callbacks.AlarmCallbackException;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.configuration.ConfigurationException;
import org.graylog2.plugin.configuration.ConfigurationRequest;
import org.graylog2.plugin.configuration.fields.BooleanField;
import org.graylog2.plugin.configuration.fields.ConfigurationField;
import org.graylog2.plugin.configuration.fields.DropdownField;
import org.graylog2.plugin.configuration.fields.TextField;
import org.graylog2.plugin.streams.Stream;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

public class HipChatAlarmCallback implements AlarmCallback {
    private static final String NAME = "HipChat alarm callback";
    private static final String CK_API_TOKEN = "api_token";
    private static final String CK_ROOM = "room";
    private static final String CK_COLOR = "color";
    private static final String CK_NOTIFY = "notify";
    private static final String CK_API_URL = "api_url";
    private static final String CK_GRAYLOG_BASE_URL = "graylog_base_url";
    private static final String CK_MESSAGE_TEMPLATE = "message_template";

    // Valid colors; see https://www.hipchat.com/docs/apiv2/method/send_room_notification
    private static final ImmutableMap<String, String> VALID_COLORS = ImmutableMap.<String, String>builder()
            .put("yellow", "yellow")
            .put("green", "green")
            .put("red", "red")
            .put("purple", "purple")
            .put("gray", "gray")
            .put("random", "random")
            .build();

    private Configuration configuration;

    @Override
    public void initialize(final Configuration config) throws AlarmCallbackConfigurationException {
        this.configuration = config;
    }

    @Override
    public void call(Stream stream, AlertCondition.CheckResult result) throws AlarmCallbackException {
        final HipChatTrigger trigger = new HipChatTrigger(
                configuration.getString(CK_API_TOKEN),
                configuration.getString(CK_ROOM),
                configuration.getString(CK_COLOR),
                configuration.getBoolean(CK_NOTIFY),
                configuration.getString(CK_API_URL),
                configuration.getString(CK_MESSAGE_TEMPLATE),
                getGraylogBaseUrl(configuration.getString(CK_GRAYLOG_BASE_URL)));
        trigger.trigger(result.getTriggeredCondition(), result);
    }

    protected static URI getGraylogBaseUrl(String graylogBaseUrlString) throws AlarmCallbackException {
        if (graylogBaseUrlString == null || graylogBaseUrlString.trim().isEmpty()) {
            return null;
        }
        try {
            String urlWithoutTrailingSlash =
                    graylogBaseUrlString.endsWith("/") ? graylogBaseUrlString.substring(0, graylogBaseUrlString.length() - 1) : graylogBaseUrlString;
            return new URI(urlWithoutTrailingSlash);
        } catch (URISyntaxException e) {
            throw new AlarmCallbackException("Graylog URL '" + graylogBaseUrlString + "' is not a valid URI.");
        }
    }

    @Override
    public Map<String, Object> getAttributes() {
        return Maps.transformEntries(configuration.getSource(), new Maps.EntryTransformer<String, Object, Object>() {
            @Override
            public Object transformEntry(String key, Object value) {
                if (CK_API_TOKEN.equals(key)) {
                    return "****";
                }
                return value;
            }
        });
    }

    @Override
    public void checkConfiguration() throws ConfigurationException {
        if (!configuration.stringIsSet(CK_API_TOKEN)) {
            throw new ConfigurationException(CK_API_TOKEN + " is mandatory and must not be empty.");
        }

        if (!configuration.stringIsSet(CK_ROOM)) {
            throw new ConfigurationException(CK_ROOM + " is mandatory and must not be empty.");
        }

        if (configuration.getString(CK_ROOM).length() > 100) {
            throw new ConfigurationException(CK_ROOM + " must be less than 100 characters long.");
        }

        if (configuration.stringIsSet(CK_COLOR) && !VALID_COLORS.containsKey(configuration.getString(CK_COLOR))) {
            throw new ConfigurationException(CK_COLOR + " is not a valid color.");
        }
    }

    @Override
    public ConfigurationRequest getRequestedConfiguration() {
        final ConfigurationRequest configurationRequest = new ConfigurationRequest();

        configurationRequest.addField(new TextField(
                CK_API_TOKEN, "Room Token", "", "HipChat room token",
                ConfigurationField.Optional.NOT_OPTIONAL)
        );
        configurationRequest.addField(new TextField(
                CK_ROOM, "Room", "", "ID or name of HipChat room",
                ConfigurationField.Optional.NOT_OPTIONAL)
        );
        configurationRequest.addField(new DropdownField(
                CK_COLOR, "Color", "yellow", VALID_COLORS,
                "Background color for message", ConfigurationField.Optional.OPTIONAL)
        );
        configurationRequest.addField(new BooleanField(
                CK_NOTIFY, "Notify", true, "Whether this message should trigger a user notification."));
        configurationRequest.addField(new TextField(
                CK_API_URL, "HipChat API URL", "https://api.hipchat.com",
                "Specify different API URL for self hosted HipChat", ConfigurationField.Optional.OPTIONAL));
        configurationRequest.addField(new TextField(
                CK_GRAYLOG_BASE_URL, "Graylog Base URL", "",
                "Graylog base URL for linking to the stream (e.g. https://your.graylogserver.com).", ConfigurationField.Optional.OPTIONAL));
        configurationRequest.addField(new TextField(
                CK_MESSAGE_TEMPLATE, "Message Template", "",
                "Custom message template (same as email templates).", ConfigurationField.Optional.OPTIONAL, TextField.Attribute.TEXTAREA));

        return configurationRequest;
    }

    @Override
    public String getName() {
        return NAME;
    }
}