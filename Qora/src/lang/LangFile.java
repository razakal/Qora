package lang;

import controller.Controller;

public class LangFile {

	private String name;
	private String filename;
	private long timestamp;
	
	public LangFile() 
	{
		this.name = "English";
		this.filename = "en.json";
		this.timestamp = Controller.getInstance().getBuildTimestamp();
	}
	
	public LangFile(String name, String filename, long timestamp)
	{
		this.name = name;
		this.filename = filename;
		this.timestamp = timestamp;
	}
	
	public String getName() 
	{
		return this.name;
	}
	
	public String getFileName() 
	{
		return this.filename;
	}
	
	public long getTimestamp() 
	{
		return this.timestamp;
	}
	
	public String toString()
	{
		return  "[" + filename.substring(0, filename.lastIndexOf('.')) + "] " + this.name;
	}
}
