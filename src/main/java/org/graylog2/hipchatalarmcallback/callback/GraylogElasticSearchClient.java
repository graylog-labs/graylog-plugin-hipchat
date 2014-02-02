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

public class GraylogElasticSearchClient {

	private static Logger LOG = Logger.getLogger(GraylogElasticSearchClient.class.getName());
	
	
	public GraylogMessage getLastMessage(String streamId, String graylogUrl, String elasticSearchUrl) {
		
		GraylogMessage message = new GraylogMessage();
		
		Writer writer = null;
        try {
			URL url = new URL(elasticSearchUrl);
	        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
	        conn.setDoOutput(true);
	        conn.setRequestMethod("POST");
	
	        writer = new OutputStreamWriter(conn.getOutputStream());
	        writer.write("{\"query\": {\"term\": {\"streams\": \""+streamId+"\"}}, \"sort\":[{\"created_at\": \"desc\"}], \"size\":1}");
	        writer.flush();
	        
	        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
	        	
	        	BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	        	StringBuilder responseData = new StringBuilder();
	        	String line = null;
	        	while((line = br.readLine()) != null) {
				    responseData.append(line);
				}
	        	DBObject json = (DBObject)JSON.parse(responseData.toString());
	        	DBObject hits = (DBObject)json.get("hits");
	        	BasicDBList hitsList = (BasicDBList)hits.get("hits");
	        	DBObject hit = (DBObject)hitsList.get(0);
	    		DBObject source = (DBObject)hit.get("_source");
	    		message.setLevel( ((Integer)source.get("level")).intValue() );
	    		
	    		String id = (String)hit.get("_id");
	    		String text = (String)source.get("message");
	    		text = text + "\n" + graylogUrl + id;
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
