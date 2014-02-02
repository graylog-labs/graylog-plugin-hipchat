package org.graylog2.hipchatalarmcallback.callback;

public class GraylogMessage {
	
	public static final int DEFAULT_LEVEL = 3;
	private String text;
	private int level;
	
	
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = cleanXDebugFormat(text);
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	
	
	private String cleanXDebugFormat(String text) {
		
		String cleanText = text;
		
		if(text!=null && text.contains("xdebug_message")) {
			cleanText = text.substring(1, text.length()-4);
			cleanText = "<table>"+cleanText+"</table>";
		}
		
		return cleanText;
	}
	
}
