package org.ray.pack.xml;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.ray.pack.model.SvnLog;
import org.ray.pack.model.LogEntry;
import org.ray.pack.model.Path;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Parse XML document object into a specific Log model.
 * 
 * @author jeroenvs
 * @date 16-03-2009
 */
public class LogParser {
	
	public static final String ENTRY_ID = "logentry";
	
	public static final String REVISION_ID = "revision";
	public static final String AUTHOR_ID = "author";
	public static final String DATE_ID = "date";
	public static final String PATHS_ID = "paths";
	public static final String MESSAGE_ID = "msg";	
	
	public static final String PATH_ID = "path";
	public static final String ACTION_ID = "action";
	public static final String PROP_MODS_ID = "prop-mods";
	public static final String TEXT_MODS_ID = "text-mods";
	public static final String KIND_ID = "kind";

	public static SvnLog parseLog(Document document) throws Exception {
		SvnLog log = new SvnLog();
		
		if(document == null) { return log; }
		
		// Attach entries
		ArrayList<LogEntry> entries = new ArrayList<LogEntry>();
		NodeList elements = document.getElementsByTagName(ENTRY_ID);
		log.setEntries(entries);
		for(int i = 0; i < elements.getLength(); i++) {
			entries.add(parseEntry((Element) elements.item(i)));
		}
		
		return log;
	}
	
	@SuppressWarnings("deprecation")
	public static LogEntry parseEntry(Element element) throws Exception {
		LogEntry entry = new LogEntry();
		
		// Retrieve revision number
		String revision = "";
		try {
			revision = element.getAttribute(REVISION_ID);
			entry.setRevision(Integer.parseInt(revision));
		} catch(NumberFormatException e) {
			throw new ParsingException(revision + " is not a valid revision number");
		}
		
		// Retrieve author
		String author = getTextValue(element, AUTHOR_ID);
		entry.setAuthor(author);
		
		// Retrieve date
		String date = getTextValue(element, DATE_ID);
		try {
			// Format date
			date = date.replace("Z", "");
			date = date.replace("T", ":");
			date = date.replace("-", ":");
			
			// Split date
			String[] dt = date.split(":");
			if(dt.length != 6) { 
				throw new ParsingException(date + " is not a valid date");
			}
			
			// Store date
			entry.setDate(new Date(
					Integer.parseInt(dt[0]), Integer.parseInt(dt[1]) - 1, 
					Integer.parseInt(dt[2]), Integer.parseInt(dt[3]), 
					Integer.parseInt(dt[4]), (int) Double.parseDouble(dt[5])
				));
		} catch(NumberFormatException e) {
			throw new ParsingException(date + " is not a valid date", e);
		}
		
		// Retrieve paths
		List<Path> paths = new ArrayList<Path>();
		entry.setPaths(paths);
		NodeList elements = element.getElementsByTagName(PATHS_ID);
		for(int i = 0; i < elements.getLength(); i++) {
			NodeList pathList = ((Element) elements.item(i)).getElementsByTagName(PATH_ID);
			for(int j=0; j<pathList.getLength(); j++){
				Path path = parsePath((Element) pathList.item(j));
				path.setIndex(j);
				paths.add(path);
			}
		}
		
		// Retrieve message
		String message = getTextValue(element, MESSAGE_ID);
		entry.setMessage(message);
		
		return entry;
	}
	
	public static Path parsePath(Element element) throws ParsingException {
		Path path = new Path();
		String action = getTextProperty(element, PATH_ID, ACTION_ID);
		String propMods = getTextProperty(element, PATH_ID, PROP_MODS_ID);
		String textMods = getTextProperty(element, PATH_ID, TEXT_MODS_ID);
		String kind = getTextProperty(element, PATH_ID, KIND_ID);
		path.setAction(action);
		path.setKind(kind);
		
//		String uri = getTextValue(element, PATH_ID);
		Node value = element.getChildNodes().item(0);
		String uri = value.getNodeValue();
		path.setUri(uri);
		
		return path;
	}
	
	public static String getTextProperty(Element parent, String identifier, String propName) {
		try {
//			Node element = parent.getElementsByTagName(identifier).item(0);
			NamedNodeMap map = parent.getAttributes();
			Node node = map.getNamedItem(propName);
			return node==null ? "" : node.getNodeValue();
		} catch(NullPointerException e) {
			return "";
		}
	}
	
	/**
	 * Retrieve text value of sub element.
	 * @param parent Parent element
	 * @param identifier Identifier
	 * @return Value of sub element
	 */
	public static String getTextValue(Element parent, String identifier) {
		try {
			Node element = parent.getElementsByTagName(identifier).item(0);
			Node value = element.getChildNodes().item(0);
			return value.getNodeValue();
		} catch(NullPointerException e) {
			return "";
		}
	}
	
}