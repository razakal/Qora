package kryo;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import difflib.DiffUtils;
import difflib.Patch;
import difflib.PatchFailedException;

public class DiffHelper {

	public static String getDiff(String source, String destination) {
		
		Patch<String> diff = getPatch(source, destination);

		List<String> unifiedDiff = DiffUtils.generateUnifiedDiff("", "", Arrays.asList(StringUtils.split(source, "\n")), diff, 0);

		return StringUtils.join(unifiedDiff, "\n");
	}

	public static Patch<String> getPatch(String source, String destination) {
		if(source == null)
		{
			source = "";
		}
		
		if(destination == null)
		{
			destination = "";
		}
		
		String[] src = StringUtils.split(source, "\n");
		String[] dest = StringUtils.split(destination, "\n");
		Patch<String> diff = DiffUtils.diff(Arrays.asList(src),
				Arrays.asList(dest));
		
		return diff;
	}

	public static String patch(String source, String patch) throws PatchFailedException {

		List<String> patchs = Arrays.asList(StringUtils.split(patch, "\n"));

		Patch<String> diff = DiffUtils.parseUnifiedDiff(patchs); 
		
		String[] split = StringUtils.split(source, "\n");
		List<String> applyTo = diff.applyTo(Arrays.asList(split));
		String join = StringUtils.join(applyTo, "\n");
		
		return join;	
	}
}
