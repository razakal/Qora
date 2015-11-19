package qora.web;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import utils.DiffHelper;
import difflib.DiffUtils;
import difflib.Patch;

public class NamestorageKeyValueHistory {

	
	private final String before;
	private final String after;
	private final String change;
	private final String changekey;
	private final String key;
	private final String name;

	public NamestorageKeyValueHistory(String before, String change, String after, String changekey, String key, String name) {
		this.before = before;
		this.after = after;
		this.change = change;
		this.changekey = changekey;
		this.key = key;
		this.name = name;
	}

	public String getBefore() {
		return before;
	}

	public String getAfter() {
		return after;
	}

	public String getChange() {
		return change;
	}
	
	public String getDiff() {
		
		String[] split = StringUtils.split(before);
		if(split == null)
		{
			split = new String[]{};
		}
		
		Patch<String> patch = DiffHelper.getPatch(before, after);
		
		List<String> generateUnifiedDiff = DiffUtils.generateUnifiedDiff(before, after, Arrays.asList(split), patch, 3);
		return StringUtils.join(generateUnifiedDiff, "\n");
	}

	public String getChangekey() {
		return changekey;
	}

	public String getKey() {
		return key;
	}

	public String getName() {
		return name;
	}
	
	
	
	
}
