/**
 * Copyright 2014 Markus Wuersch <markus@wuersch.net>
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.log4j.Logger;

import com.mongodb.BasicDBList;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

/**
 * A simple elasticsearch client to get necessary information about a triggered Alarm. 
 * @author Markus Wuersch <markus@wuersch.net>
 */
public class GraylogElasticSearchClient {

	private static Logger LOG = Logger.getLogger(GraylogElasticSearchClient.class.getName());
	
	/**
	 * Get the last GrayLog message in a stream. 
	 * @param streamId The GrayLog2 stream id
	 * @param graylogUrl The URL to your GrayLog instance. 
	 * @param elasticSearchUrl	The URL of the elasticsearch instance. 
	 * @return GraylogMessage object of the last message in the stream.
	 */
	public GraylogMessage getLastMessageInStream(String streamId, String graylogUrl, String elasticSearchUrl) {
		
		GraylogMessage message = new GraylogMessage();
		
		Writer writer = null;
        try {
        	// query elasticsearch for last message in stream
			URL url = new URL(elasticSearchUrl+"/_search");
	        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
	        conn.setDoOutput(true);
	        conn.setRequestMethod("POST");
	
	        writer = new OutputStreamWriter(conn.getOutputStream());
	        writer.write("{\"query\": {\"term\": {\"streams\": \""+streamId+"\"}}, \"sort\":[{\"created_at\": \"desc\"}], \"size\":1}");
	        writer.flush();
	        
	        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
	        	
	        	// read elasticsearch API response into StringBuilder
	        	BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	        	StringBuilder responseData = new StringBuilder();
	        	String line = null;
	        	while((line = br.readLine()) != null) {
				    responseData.append(line);
				}
	        	// create JSON Object from elasticsearch result String
	        	DBObject json = (DBObject)JSON.parse(responseData.toString());
	        	DBObject hits = (DBObject)json.get("hits");
	        	BasicDBList hitsList = (BasicDBList)hits.get("hits");
	        	DBObject hit = (DBObject)hitsList.get(0);
	    		DBObject source = (DBObject)hit.get("_source");
	    		message.setLevel( ((Integer)source.get("level")).intValue() );
	    		
	    		String id = (String)hit.get("_id");
	    		// message content
	    		String text = (String)source.get("message");
	    		// append deep link to message on GrayLog2
	    		text = text + "\n" + graylogUrl + "/messages/" + id;
	    		message.setText( text );
			} else {
				LOG.error("unable to query elasticsearch. response code: "+conn.getResponseCode());
			}
			
        } catch (IOException e) {
			LOG.error("unable to query elasticsearch", e);
        	message = null;
        } catch (RuntimeException rte) {
			LOG.error("unable to query elasticsearch", rte);
        	message = null;
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {}
            }
        }
        
        return message;
	}
	
}
