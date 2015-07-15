package org.ray.pack;

import java.io.File;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.ray.pack.model.Issue;
import org.ray.pack.model.SvnLog;
import org.ray.pack.util.CommandRunner;
import org.ray.pack.util.FileUtil;
import org.ray.pack.util.Log;
import org.ray.pack.xml.DocumentParser;
import org.ray.pack.xml.IssueParser;
import org.ray.pack.xml.LogParser;
import org.w3c.dom.Document;

public class PackTask extends Task {
	public static final int mode_auto = 0;//自动模式，无交互
	public static final int mode_normal = 1;//参与模式，在svn log后会停顿，可手动修改log文件决定文件集
	public static final int mode_complex = 2;
	
	public static String projectHome = "D:/ENV/eclipse/workspace/project_util";
	public static String batExportSvnLog = "/run/export_svn_log.bat";
	public static String batSvnUpdate = "/run/svn_update.bat";
	public static String batCommands = "/run/commands.bat";
	public static boolean svnCheckout = false;//是否更新svn文件版本为最新
	
	public static int revisionFrom = 25;//log起始版本号，增量文件的基础，建议必须是以版本号的完整文件库
	public static int revisionTo = 28;//log结束版本号，可大于issue提交的最大版本号，只取issue对应的文件版本
	public static String issueFile = "d:/SearchRequest.xml";//jira issue rss导出文件
	public static String logFile = "d:/svn.xml";//生成的svn log文件
	
	public static String svnUri = "https://lenovo-pc/svn/jiratest";
	public static String packDirSource = "d:/work/jiratest";//最新文件拷贝源
	public static String packDirDest = "d:/packtemp";//增量打包基本版本文件目录，增量文件的基础
	public static String username = "admin";//svn帐号密码
	public static String password = "admin";
	
	public static int runAs = 0;//0：按序列，1：按分析
	public static int mode = 0;//模式
	
	public static void main(String[] args) {
		try{
			new PackTask().execute();
		}catch(Exception ex){
			
		}
	}

	/**
	 * 必须在参数设置以后调用
	 */
	public static void initPackEnv(){
		try{
//			svn更新的bat，源svn目录需要更新到最新版本
			StringBuilder sb = new StringBuilder();
			sb.append("cd /d ").append(packDirSource).append("\r\n").
			append("svn cleanup --username " + PackTask.username + 
					" --password " + PackTask.password + " --non-interactive\r\n").
			append("svn update --username " + PackTask.username + 
						" --password " + PackTask.password + " --non-interactive\r\n");
			if(mode != mode_auto){
				sb.append("pause\r\n");
			}
			sb.append("exit\r\n");
			File updateSvn = FileUtil.createFile(projectHome + batSvnUpdate);
			FileUtil.write(updateSvn, sb.toString());
			
//			获取svn log的bat
			sb.delete(0, sb.length());
			sb.append("cd /d ").append(packDirSource).append("\r\n").
			append("svn log " + svnUri + " -v -r " + revisionFrom + ":" + revisionTo + " --xml > " + logFile).append("\r\n");
			if(mode != mode_auto){
				sb.append("pause\r\n");
			}
			sb.append("exit\r\n");
			File exportSvnLog = FileUtil.createFile(projectHome + batExportSvnLog);
			FileUtil.write(exportSvnLog, sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	 @Override  
	 public void execute() throws BuildException {
		 initPackEnv();
		 try {
//			 更新svn
			if(isSvnCheckout()){
				CommandRunner.runExternalCommandFile(projectHome + batSvnUpdate);
			}
				
			Document document = DocumentParser.parseDocument(issueFile);
			List<Issue> issues = IssueParser.parseIssues(document);
//			svn日志文件
			CommandRunner.runExternalCommandFile(projectHome + batExportSvnLog);
			document = DocumentParser.parseDocument(logFile);
			SvnLog log = LogParser.parseLog(document);
			Log.debug("LOLS: [" + logFile + "]\r\n" + log.toString());
				
//			过滤无issue id的对象
			PackService.filterByIssues(issues, log);
//			拷贝svn更新文件
			if(runAs == 0){
				PackService.updateAsSequence(svnUri, packDirDest, log);
			}else{
				PackService.updateAsAnalysis(svnUri, packDirDest, log);
			}
//		 } catch (SAXException e) {
//			 e.printStackTrace();
//		 } catch (IOException e) {
//			 e.printStackTrace();
//		 } catch (ParserConfigurationException e) {
//			 e.printStackTrace();
		 } catch (Exception e) {
			 e.printStackTrace();
		 }
	 }

	public static String getProjectHome() {
		return projectHome;
	}

	public static void setProjectHome(String projectHome) {
		PackTask.projectHome = projectHome;
	}

	public static String getBatExportSvnLog() {
		return batExportSvnLog;
	}

	public static void setBatExportSvnLog(String batExportSvnLog) {
		PackTask.batExportSvnLog = batExportSvnLog;
	}

	public static String getBatSvnUpdate() {
		return batSvnUpdate;
	}

	public static void setBatSvnUpdate(String batSvnUpdate) {
		PackTask.batSvnUpdate = batSvnUpdate;
	}

	public static int getRevisionFrom() {
		return revisionFrom;
	}

	public static void setRevisionFrom(int revisionFrom) {
		PackTask.revisionFrom = Integer.valueOf(revisionFrom);
	}

	public static int getRevisionTo() {
		return revisionTo;
	}

	public static void setRevisionTo(int revisionTo) {
		PackTask.revisionTo = Integer.valueOf(revisionTo);
	}

	public static String getIssueFile() {
		return issueFile;
	}

	public static void setIssueFile(String issueFile) {
		PackTask.issueFile = issueFile;
	}

	public static String getLogFile() {
		return logFile;
	}

	public static void setLogFile(String logFile) {
		PackTask.logFile = logFile;
	}

	public static String getSvnUri() {
		return svnUri;
	}

	public static void setSvnUri(String svnUri) {
		PackTask.svnUri = svnUri;
	}

	public static String getPackDirSource() {
		return packDirSource;
	}

	public static void setPackDirSource(String packDirSource) {
		PackTask.packDirSource = packDirSource;
	}

	public static String getPackDirDest() {
		return packDirDest;
	}

	public static void setPackDirDest(String packDirDest) {
		PackTask.packDirDest = packDirDest;
	}

	public static String getUsername() {
		return username;
	}

	public static void setUsername(String username) {
		PackTask.username = username;
	}

	public static String getPassword() {
		return password;
	}

	public static void setPassword(String password) {
		PackTask.password = password;
	}

	public static int getMode() {
		return mode;
	}

	public static void setMode(int mode) {
		PackTask.mode = mode;
	}

	public static boolean isSvnCheckout() {
		return svnCheckout;
	}

	public static void setSvnCheckout(boolean svnCheckout) {
		PackTask.svnCheckout = svnCheckout;
	}

	public static int getRunAs() {
		return runAs;
	}

	public static void setRunAs(int runAs) {
		PackTask.runAs = runAs;
	}
}