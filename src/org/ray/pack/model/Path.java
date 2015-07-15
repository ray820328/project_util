package org.ray.pack.model;

/**
 * Modification path.
 * 
 * <blockquote>
 * 	Example in svn --xml log:
 * 	<path action="M">/src/blaat.java</path>
 * </blockquote>
 * 
 * @author jeroenvs
 * @date 16-03-2009
 */
public class Path {

	public static final String action_delete = "D";//删除操作
	public static final String file = "file";
	public static final String directory = "dir";
	/**
	 * Action type.
	 * 
	 * <pre>
	 * A: Added
	 * M: Modified
	 * D: Deleted
	 * </pre>
	 */
	private String action;
	
	private String kind;
	
	/**
	 * Location of altered file.
	 */
	private String uri;
	private int index;
	
	/**
	 * Retrieve action type.
	 * @return
	 */
	public String getAction() {
		return action;
	}
	
	/**
	 * Change action type.
	 * @param action
	 */
	public void setAction(String action) {
		this.action = action;
	}
	
	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	/**
	 * Retrieve path URI.
	 * @return URI
	 */
	public String getUri() {
		return uri;
	}
	
	/**
	 * Change path URI.
	 * @param URI Location of file
	 */
	public void setUri(String uri) {
		this.uri = uri;
	}
	
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public boolean isDirectory(){
		return this.kind!=null && this.kind.equals(directory);
	}
	public boolean isFile(){
		return this.kind!=null && this.kind.equals(file);
	}
	
	public boolean isDelete(){
		return this.action!=null && this.action.equals(action_delete);
	}

	public String toString(){
		return action + ":" + this.uri;
	}
}