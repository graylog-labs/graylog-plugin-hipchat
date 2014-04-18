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

/**
 * Object that holds necessary data of a message from GrayLog.
 * @author Markus Wuersch <markus@wuersch.net>
 */
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
	
	/* for PHP folks: sometimes we encounter xdebug messages. 
	 * These can be quite ugly and unreadable so we clean it
	 * a bit to make it more readable. 
	 */
	private String cleanXDebugFormat(String text) {
		
		String cleanText = text;
		
		if(text!=null && text.contains("xdebug_message")) {
			cleanText = text.substring(1, text.length()-4);
			cleanText = "<table>"+cleanText+"</table>";
		}
		
		return cleanText;
	}
	
}
