package org.ray.pack.util;

public class Log {

//	private final static Logger dbg = Logger.getLogger("DEBUGS");
//	private final static Logger err = Logger.getLogger("ERR");
	
	public static void debug(String str){
//		if(!ServerConfig.isDebug()){
//			return;
//		}
		System.out.println(str);
	}
	public static void info(String str){
		System.out.println(str);
	}
	
	public static void error(Object message){
		System.err.println(message);
	}
	public static void error(Throwable ex){
		System.err.println(ex);
	}
	public static void error(Object message, Throwable ex){
		System.err.println(ex);
	}
}
