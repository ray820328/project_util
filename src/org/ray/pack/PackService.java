package org.ray.pack;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.ray.pack.model.Issue;
import org.ray.pack.model.LogEntry;
import org.ray.pack.model.Path;
import org.ray.pack.model.SvnLog;
import org.ray.pack.util.CommandRunner;
import org.ray.pack.util.FileUtil;
import org.ray.pack.util.Log;

public class PackService {
	
	public static void pack(String dirSrc, String dirDest)throws Exception{
		
		
	}
	
	public static void filterByIssues(List<Issue> issues, SvnLog svnLog)throws Exception{
		if(issues==null || svnLog==null || svnLog.getEntries()==null){
			return;
		}
		Iterator<LogEntry> it = svnLog.getEntries().iterator();
		while(it.hasNext()){
			LogEntry entry = it.next();
//			依次删除issue id不在目标里的log对象
			if(entry.getMessage() == null){
				continue;
			}
			for(int i=0; i<issues.size(); i++){
//				comment不带issue id，不会入包
				Issue issue = issues.get(i);
				if(entry.getMessage().indexOf(issue.getKeyName()) > -1){
					break;
				}
				if(i == issues.size()-1){
					it.remove();
				}
			}
		}
	}
	
	/**
	 * 直接按log生成command序列的方式
	 * @param svnUri
	 * @param dirDest
	 * @param svnLog
	 * @throws Exception
	 */
	public static void updateAsSequence(String svnUri, String dirDest, SvnLog svnLog)throws Exception{
		if(svnLog==null || svnLog.getEntries()==null){
			return;
		}
		List<LogEntry> entries = svnLog.getEntries();
		
		List<String> cmds = new ArrayList<String>();
//		添加和更新操作：取关联的issue文件版本中最新的文件（非库中最新的）
		Map<String, FileCommitAction> fileCommitActionnMap = new HashMap<String, FileCommitAction>();
		for(LogEntry entry : entries){
			if(entry.getPaths() == null){
				continue;
			}
			for(Path path : entry.getPaths()){
				FileCommitAction fileAction = fileCommitActionnMap.get(path.getUri());
				if(fileAction==null || fileAction.getRevision()<entry.getRevision()){
					fileAction = fileAction==null ? new FileCommitAction() : fileAction;
					fileAction.setAction(path.getAction());
					fileAction.setUri(path.getUri());
					fileAction.setRevision(entry.getRevision());
					fileAction.setPath(path);
//					更新到较新版本
					fileCommitActionnMap.put(path.getUri(), fileAction);
				}else{
					Log.debug("文件版本过低，被忽略: [r" + entry.getRevision() + "] " + path.getUri());
				}
			}
		}
//		按revision升序排序
		Set<FileCommitAction> actions = new TreeSet<FileCommitAction>();
		actions.addAll(fileCommitActionnMap.values());
		
//		生成拷贝命令
		for(FileCommitAction fileAction : actions){
			if(fileAction.getPath() == null){
				continue;
			}
			String dest = dirDest + fileAction.getUri();
			if(!fileAction.getPath().isDirectory()){
				FileUtil.createFile(dest);//文件需要确保目录存在
			}
			String cmd = null;
			if(!fileAction.getPath().isDelete()){
				cmd = "svn export --username " + PackTask.username + 
					" --password " + PackTask.password + " --non-interactive --force -r " + 
					fileAction.getRevision() + 
					" \"" + svnUri + fileAction.getPath().getUri() + "\" \"" + dest + "\"";
			}else if(fileAction.getPath().isDirectory()){
				cmd = "rd /s /q \"" + dest.replace("/", "\\") + "\"";///s子目录  /q不用确认
			}else{
				cmd = "del /q \"" + dest.replace("/", "\\") + "\"";
			}
			cmds.add(cmd);
		}
//		命令集合
		StringBuilder sb = new StringBuilder();
		for(String cmd : cmds){
//			输出到命令文件，手动参与
			sb.append(cmd).append("\r\n");
		}
		sb.append("pause\r\n").append("exit\r\n");
		File exportSvnLog = FileUtil.createFile(PackTask.projectHome + PackTask.batCommands);
		FileUtil.write(exportSvnLog, sb.toString());
//		执行命令集合
		CommandRunner.runExternalCommandFile(PackTask.projectHome + PackTask.batCommands);
	}
	
	/**
	 * 分析操作行为，智能方式
	 * @param svnUri
	 * @param dirDest
	 * @param svnLog
	 * @throws Exception
	 */
	public static void updateAsAnalysis(String svnUri, String dirDest, SvnLog svnLog)throws Exception{
		if(svnLog==null || svnLog.getEntries()==null){
			return;
		}
		List<LogEntry> entries = svnLog.getEntries();
		
		List<String> cmds = new ArrayList<String>();
		
//		添加和更新操作：按revision排序，取关联的issue文件版本中最新的文件（非库中最新的）
		Map<String, Integer> fileUpdateVersionMap = new HashMap<String, Integer>();
		for(LogEntry entry : entries){
			if(entry.getPaths() == null){
				continue;
			}
			for(Path path : entry.getPaths()){
				if(!path.isFile()){
					continue;
				}
				if(path.getAction()!=null && Path.action_delete.equals(path.getAction())){
//					删除操作
					continue;
				}
				Integer currentFileVersion = fileUpdateVersionMap.get(path.getUri());
				if(currentFileVersion==null || currentFileVersion<entry.getRevision()){
//					更新到较新版本
					fileUpdateVersionMap.put(path.getUri(), entry.getRevision());
				}else{
					Log.debug("文件版本过低，被忽略: [r" + entry.getRevision() + "] " + path.getUri());
				}
			}
		}
//		删除操作：如果更新版本中有对应值且版本大于删除版本，不做操作，否则删除
		Map<String, Integer> fileDeleteVersionMap = new HashMap<String, Integer>();
		for(LogEntry entry : entries){
			if(entry.getPaths() == null){
				continue;
			}
			for(Path path : entry.getPaths()){
				if(path.getAction()!=null && !Path.action_delete.equals(path.getAction())){
//					只处理删除操作
					continue;
				}
				if(path.isDirectory()){//删除目录，则删除所有此版本号之前的相关文件
					onDeletingDirectory(dirDest, fileUpdateVersionMap, path.getUri(), entry.getRevision());
					continue;
				}
				Integer deleteFileVersion = fileDeleteVersionMap.get(path.getUri());
				Integer updateVersion = fileUpdateVersionMap.get(path.getUri());
				if(deleteFileVersion==null || deleteFileVersion<entry.getRevision()){
					deleteFileVersion = entry.getRevision();
//					更新版本中有对应值且版本大于删除版本，不做操作，否则删除
					if(updateVersion==null || updateVersion<deleteFileVersion){
//						删除
						fileUpdateVersionMap.remove(path.getUri());
						fileDeleteVersionMap.put(path.getUri(), entry.getRevision());
						deleteFileVersion = fileDeleteVersionMap.get(path.getUri());
					}
				}
//				删除后重新建立了文件，需要从删除列表清除
				if(updateVersion!=null && updateVersion<=deleteFileVersion){
					fileDeleteVersionMap.remove(path.getUri());
				}
			}
		}
//		生成拷贝命令
		for(String file : fileUpdateVersionMap.keySet()){
			String dest = dirDest + file;
			FileUtil.createFile(dest);//确保目录存在
			String cmd = "svn export --username " + PackTask.username + 
					" --password " + PackTask.password + " --non-interactive --force -r " + 
					fileUpdateVersionMap.get(file).intValue() + 
					" \"" + svnUri + file + "\" \"" + dest + "\"";
			cmds.add(cmd);
		}
//		生成删除命令，某些文件在新版本中被删除，需删除基础版本中的文件
//		del/s/q d:\123\*.* ----(用于删除文件夹下的子文件) 
//		rd/s/q d:\123 ----(用于删除文件夹) 
//		/s子目录  /q不用确认 
		for(String file : fileDeleteVersionMap.keySet()){
			String dest = dirDest + file;
			File destFile = new File(dest);
			if(destFile.exists()){
				destFile.delete();
			}
//			String cmd = "del /q " + dest.replace("/", "\\");
////			cmds.add(cmd);
////			输出到命令文件，手动参与
//			StringBuilder sb = new StringBuilder();
//			sb.delete(0, sb.length());
//			sb.append(cmd).append("\r\n").append("pause\r\n").append("exit\r\n");
//			File exportSvnLog = FileUtil.createFile(PackTask.projectHome + PackTask.batDelete);
//			FileUtil.write(exportSvnLog, sb.toString());
		}
		
//		拷贝文件
		while(cmds.size() > 0){
			String cmd = cmds.remove(0);
			runCommand(cmd, null);
		}
	}
	/** 依次删除path目录相关的文件 */
	private static void onDeletingDirectory(String dirDest, Map<String, Integer> fileVersionMap, String path, int version){
		Iterator<String> it = fileVersionMap.keySet().iterator();
		while(it.hasNext()){
			String file = it.next();
			if(file.indexOf(path)>-1 && fileVersionMap.get(file)<=version){
//				如果文件版本大于此版本，为用户重建相同的目录提交
				it.remove();
			}
		}
//		删除目录；1，如果有基础版本，删除；2，如果无基础版本，不用删除，因为后续版本不会操作旧版本文件
		File dir = new File(dirDest + path);
		if(dir.exists() && dir.isDirectory()){
			dir.delete();
		}
	}
	
	public static void runCommand(String cmd, String[] envs)throws Exception{
		try{
			Process proc = Runtime.getRuntime().exec(cmd);//cmds.toArray(new String[]{}));  
	        int exitVal = proc.waitFor();
	        if(exitVal == 0){
	        	Log.debug("Success: " + cmd);
	        }else{
	//        	执行过程出错直接退出后续步骤
	        	throw new RuntimeException("指令执行失败");
	        }
		}catch(Exception ex){
			Log.debug("Failed: " + cmd);
			throw ex;
		}
	}
	
	private static class FileCommitAction implements Comparable{
		private String action;
		private String uri;
		private int revision;
		private Path path;
		
		public String getAction() {
			return action;
		}

		public void setAction(String action) {
			this.action = action;
		}

		public String getUri() {
			return uri;
		}

		public void setUri(String uri) {
			this.uri = uri;
		}

		public int getRevision() {
			return revision;
		}

		public void setRevision(int revision) {
			this.revision = revision;
		}
		
		public Path getPath() {
			return path;
		}

		public void setPath(Path path) {
			this.path = path;
		}

		public int compareTo(Object o){
			FileCommitAction bean = (FileCommitAction)o;
			if(bean.revision == revision){
				if(this.path.getIndex() > bean.getPath().getIndex()){
					return 1;
				}else if(this.path.getIndex() < bean.getPath().getIndex()){
					return -1;
				}
				return 0;
			}else if(bean.revision > revision){
				return -1;
			}else{
				return 1;
			}
		}

		public String toString(){
			return action + ":" + revision + ":" + uri;
		}
	}
}
