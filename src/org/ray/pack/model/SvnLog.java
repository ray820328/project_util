package org.ray.pack.model;

import java.util.List;

/**
 * Log structure, which contains 0..* entries
 * 
 * @author jeroenvs
 * @date 16-03-2009
 */
public class SvnLog {

	/**
	 * Log entries.
	 */
	private List<LogEntry> entries;

	/**
	 * Retrieve entries.
	 * @return Entries
	 */
	public List<LogEntry> getEntries() {
		return entries;
	}

	/**
	 * Change entries.
	 * @param entries Entries collection
	 */
	public void setEntries(List<LogEntry> entries) {
		this.entries = entries;
	}

	public String toString(){
		StringBuilder sb = new StringBuilder();
		if(this.entries == null){
			return sb.toString();
		}
		for(LogEntry entry : entries){
			sb.append(entry);
		}
		return sb.toString();
	}
}