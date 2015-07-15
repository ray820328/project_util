package org.ray.pack.util;

import java.io.*;

public class CommandRunner {
	public static void runCommand(String cmd, String[] envs, File dir)throws Exception{
		Runtime rt = Runtime.getRuntime();
		Process proc = rt.exec(cmd, envs, dir);
		proc.waitFor();
	}
	
	public static void runExternalCommandFile(String bat)throws Exception{
		Runtime rt = Runtime.getRuntime();
//		TODO 暂时只支持windows
		Process proc = rt.exec("cmd.exe /c start /wait " + bat);
		int code = proc.waitFor();
		Log.debug("End: " + bat + ", code=" + code);
	}
}

