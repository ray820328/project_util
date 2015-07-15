package org.ray.pack.model;

import java.util.Date;
import java.util.List;

/**
 * Log entry object.
 * 
 * @author jeroenvs
 * @date 16-03-2009
 */
public class LogEntry {

	/**
	 * Revision number.
	 */
	private int revision;
	
	/**
	 * Date of revision.
	 */
	private Date date;
	
	/**
	 * Author of revision.
	 */
	private String author;
	
	/**
	 * Affected files.
	 */
	private List<Path> paths;
	
	/**
	 * Revision message.
	 */
	private String message;
	
	/**
	 * Retrieve revision number.
	 * @return
	 */
	public int getRevision() {
		return revision;
	}
	
	/**
	 * Change revision number.
	 * @param revision Unique revision number
	 */
	public void setRevision(int revision) {
		this.revision = revision;
	}
	
	/**
	 * Retrieve data.
	 * @return Revision number
	 */
	public Date getDate() {
		return date;
	}
	
	/**
	 * Change date.
	 * @param date Date of revision
	 */
	public void setDate(Date date) {
		this.date = date;
	}
	
	/**
	 * Retrieve author.
	 * @return Author
	 */
	public String getAuthor() {
		return author;
	}
	
	/**
	 * Change author.
	 * @param author Author of revision
	 */
	public void setAuthor(String author) {
		this.author = author;
	}
	
	/**
	 * Retrieve paths.
	 * @return Paths
	 */
	public List<Path> getPaths() {
		return paths;
	}
	
	/**
	 * Change paths.
	 * @param paths Affected paths
	 */
	public void setPaths(List<Path> paths) {
		this.paths = paths;
	}
	
	/**
	 * Retrieve message.
	 * @return Message
	 */
	public String getMessage() {
		return message;
	}
	
	/**
	 * Change message.
	 * @param message Message of revision
	 */
	public void setMessage(String message) {
		this.message = message;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(message).append("\r\n");
		if(this.paths == null){
			return sb.toString();
		}
		for(Path path : paths){
			sb.append(revision).append(":").append(path.getAction()).append(":").
			append(path.getUri()).append("\r\n");
		}
		return sb.toString();
	}
}