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
 * @author Markus Wuersch <markus@wuersch.net>
 */
public class HipChatTrigger {
	
    private final String apiToken;
    private final String room;
    private final String graylogUrl;		// example: http://greylog.mydomain.com
    private final String elasticSearchUrl;	// example: http://localhost:9200/_search
    private static final String API_URL = "https://api.hipchat.com/v1/rooms/message?auth_token=";

    public HipChatTrigger(String apiToken, String room, String graylogUrl, String elasticSearchUrl) {
        this.apiToken = apiToken;
        this.room = room;
        this.graylogUrl = graylogUrl;
        this.elasticSearchUrl = elasticSearchUrl;
    }

    /**
     * Handles a triggered Alarm. 
     * 1. Finds detailed info for the alarm from MongoDB and elasticsearch.
     * 2. Posts a message to HipChat. 
     * @param alarm
     * @throws AlarmCallbackException
     */
    public void trigger(Alarm alarm) throws AlarmCallbackException {
        Writer writer = null;
        try {
        	// post to HipChat API
        	URL url = new URL(API_URL + this.apiToken);
        	HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        	conn.setDoOutput(true);
        	conn.setRequestMethod("POST");

        	writer = new OutputStreamWriter(conn.getOutputStream());
        	// get all data to be posted
        	writer.write(buildParametersFromAlarm(alarm));
        	writer.flush();

        	if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
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

    	// get last messag for this alarm
    	GraylogMessage message = getMessage(alarm);
        
    	// create HipChat API parameters
        Map<String, String> params = new HashMap<String, String>();
        params.put("message", message.getText());
        params.put("room_id", this.room);
        params.put("from", "Graylog2");
        params.put("message_format", "text");
        params.put("notify", "1");
        // set the HipChat message color to read or yellow
        params.put("color", message.getLevel() <= 3 ? "red": "yellow");
        
        // get the API URL query string
        return composeHipChatQueryString(params);
    }

    /**
     * Gets the last relevant message for this Alarm. The message is found by:
     * 1. finding the stream id based on the Alarm.topic
     * 2. finding the last message in the stream  
     * @param alarm The Alarm that was triggered.
     * @return GraylogMessage for this Alarm.
     */
	private GraylogMessage getMessage(Alarm alarm) {
		
		GraylogMessage message = null;

		// get the id of graylog stream
		GraylogMongoClient mongoClient = new GraylogMongoClient();
        String streamMessageId = mongoClient.getStreamId(alarm);
    	
        if(streamMessageId != null) {
        	// get the last message in the stream
        	GraylogElasticSearchClient elClient = new GraylogElasticSearchClient();
        	message = elClient.getLastMessageInStream(streamMessageId, this.graylogUrl, this.elasticSearchUrl);
        	if(message.getText()!=null) {
        		// composing message including text from GrayLog2
            	message.setText( alarm.getDescription() + "\n" + message.getText() );
        	} else {
        		// composing message using only data from the Alarm object
        		message.setText(alarm.getDescription());
        		message.setLevel(GraylogMessage.DEFAULT_LEVEL);
        	}
        } 
        
		return message;
	}

	/**
	 * Composes a URL query string from a map of parameters.
	 * @param params The parameters used to build the query string. 
	 * @return String Object of the URL query string
	 * @throws UnsupportedEncodingException
	 */
	private String composeHipChatQueryString(Map<String, String> params) throws UnsupportedEncodingException {
		
        StringBuilder sb = new StringBuilder();
        
        // loop through the map and append query parameters
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