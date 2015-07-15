package org.ray.pack.xml;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.ray.pack.model.Issue;
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
public class IssueParser {
	
	public static final String rss_id = "rss";
	public static final String channel_id = "channel";
	
	public static final String item_id = "item";
	public static final String title_id = "title";
	public static final String key_id = "key";
	public static final String id = "id";
	public static final String status_id = "status";	
	
	public static final String resolution_id = "resolution";
	public static final String reporter_id = "reporter";

	public static List<Issue> parseIssues(Document document) throws Exception {
		List<Issue> list = new ArrayList<Issue>();
		
		if(document == null) { return list; }
		
		Element rootElement = document.getDocumentElement();  
        rootElement.normalize();  
        retrivalNode(rootElement, 0, list);
		
		return list;
	}
	
	private static void retrivalNode(Node node, int depth, List<Issue> list) {  
        if (node.getNodeType() == Node.ELEMENT_NODE) {  
            NodeList childNodes = node.getChildNodes();  
            int numChildren = childNodes.getLength();  
 
            if (node.getNodeName().equals(item_id)) {  
                NodeList child = node.getChildNodes();  
                int a = child.getLength();  
                String title = "";  
                String body = "";  
                String time = "";  
                String idStr=null, key=null, status=null, resolution=null, reporter=null;
                for (int j = 0; j < a; j++) {  
                    String s;  
                    String tagname = null;  
                    try {  
                        tagname = child.item(j).getNodeName();  
                        s = child.item(j).getFirstChild().getNodeValue();  
                    } catch (Exception e) {  
                        s = "";  
                    }  
                    if (tagname.equals(title_id)) {  
                        title = s;  
                        continue;  
                    }
                    if (tagname.equals(key_id)) {  
                    	key = s;
                    	idStr = getTextProperty(child.item(j), id);
                        continue;  
                    }
                    if (tagname.equals(status_id)) {  
                    	status = getTextProperty(child.item(j), id);
                        continue;  
                    }
                    if (tagname.equals(resolution_id)) {  
                    	resolution = getTextProperty(child.item(j), id);
                        continue;  
                    }
                    if (tagname.equals(reporter_id)) {  
                    	reporter = getTextProperty(child.item(j), "username");
                        continue;  
                    }  
                    if (tagname.equals("description")) {  
                        body = s;  
                        continue;  
                    }  
                    if (tagname.equals("pubDate")) {  
                        time = s;  
                        continue;  
                    }  
 
                }  
 
                Issue item = new Issue();  
                item.setId(Long.parseLong(idStr));
                item.setKeyName(key);
                item.setReporter(reporter);
                item.setResolution(resolution);
                item.setStatus(status);
                item.setTitle(title);
                list.add(item);
            } else {  
                for (int i = 0; i < numChildren; ++i) {  
                	retrivalNode(childNodes.item(i), depth + 1, list);  
                }  
            }  
 
        }  
    } 
	
	public static String getTextProperty(Node element, String propName) {
		try {
			NamedNodeMap map = element.getAttributes();
			Node node = map.getNamedItem(propName);
			return node==null ? "" : node.getNodeValue();
		} catch(NullPointerException e) {
			return "";
		}
	}
	
}