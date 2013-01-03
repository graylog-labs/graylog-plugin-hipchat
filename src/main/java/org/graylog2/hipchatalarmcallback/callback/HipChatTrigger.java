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

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import org.graylog2.plugin.alarms.Alarm;
import org.graylog2.plugin.alarms.callbacks.AlarmCallbackException;

/**
 * @author Lennart Koopmann <lennart@socketfeed.com>
 */
public class HipChatTrigger {

    private final String apiToken;
    private final String room;
    private final int SUCCESS_RESPONSE_CODE = 200;
    private final String API_URL = "https://api.hipchat.com/v1/rooms/message";

    public HipChatTrigger(String apiToken, String room) {
        this.apiToken = apiToken;
        this.room = room;
    }

    public void trigger(Alarm alarm) throws AlarmCallbackException {
        Writer writer = null;
        try {
          URL url = new URL("https://api.hipchat.com/v1/rooms/message?auth_token=" + this.apiToken);
          HttpURLConnection conn = (HttpURLConnection)url.openConnection();
          conn.setDoOutput(true);
          conn.setRequestMethod("POST");

          writer = new OutputStreamWriter(conn.getOutputStream());
          writer.write(buildParametersFromAlarm(alarm));
          writer.flush();

          if (conn.getResponseCode() != 200) {
            throw new AlarmCallbackException("Could not POST event trigger. Expected HTTP response code <200> but got <" + conn.getResponseCode() + ">.");
          }
        } catch (IOException e) {
          throw new AlarmCallbackException("Could not POST event trigger. IOException");
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {}
            }
        }
    }

    private String buildParametersFromAlarm(Alarm alarm) throws UnsupportedEncodingException {
        Map<String, String> params = new HashMap<String, String>();
        StringBuilder sb = new StringBuilder();

        params.put("room_id", this.room);
        params.put("from", "Graylog2");
        params.put("message", alarm.getTopic() + " - " + alarm.getDescription());
        params.put("message_format", "text");
        params.put("notify", "1");

        boolean first = true;
        for (Map.Entry<String, String> param : params.entrySet()) {
            if (first) {
                first = false;
            } else {
                sb.append("&");
            }

            sb.append(URLEncoder.encode(param.getKey(), "UTF-8")).append("=").append(URLEncoder.encode(param.getValue(), "UTF-8"));
        }

        return sb.toString();
    }
}