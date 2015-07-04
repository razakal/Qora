package utils;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.extension.escaper.EscaperExtension;
import com.mitchellbosecke.pebble.template.PebbleTemplate;

import qora.web.Profile;
import qora.web.ProfileHelper;

public class PebbleHelper {

	private PebbleTemplate template;
	private Map<String, Object> contextMap;

	private PebbleHelper(PebbleTemplate template, Map<String, Object> contextMap) {
		this.contextMap = contextMap;
		this.template = template;

	}

	private static PebbleTemplate getRawPebbleTemplate(String htmlTemplate) throws PebbleException {
		PebbleEngine engine = new PebbleEngine();
		EscaperExtension escaper = engine.getExtension(EscaperExtension.class);
		escaper.setAutoEscaping(false);
		PebbleTemplate compiledTemplate = engine.getTemplate(htmlTemplate);
		return compiledTemplate;
	}

	private static PebbleHelper getRawPebbleHelper(String htmlTemplate) throws PebbleException {
		PebbleTemplate compiledTemplate = getRawPebbleTemplate(htmlTemplate);
		PebbleHelper pebbleHelper = new PebbleHelper(compiledTemplate, new HashMap<String, Object>());
		return pebbleHelper;
	}

	public static PebbleHelper getPebbleHelper(String htmlTemplate) throws PebbleException {
		PebbleHelper pebbleHelper = getRawPebbleHelper(htmlTemplate);

		List<Profile> enabledProfiles = Profile.getEnabledProfiles();
		Profile activeProfileOpt = ProfileHelper.getInstance().getActiveProfileOpt();
		List<String> followedBlogs;
		if (activeProfileOpt != null) {
			followedBlogs = activeProfileOpt.getFollowedBlogs();
		} else {
			followedBlogs = new ArrayList<>();
		}
		
		addDataToPebbleHelper(pebbleHelper, enabledProfiles, activeProfileOpt, followedBlogs);
		String navbar = generateNavbar( enabledProfiles, activeProfileOpt, followedBlogs, htmlTemplate);
		pebbleHelper.getContextMap().put("navbar", navbar);
		

		return pebbleHelper;

	}

	private static void addDataToPebbleHelper(PebbleHelper pebbleHelper, List<Profile> enabledProfiles,
			Profile activeProfileOpt, List<String> followedBlogs) {
		pebbleHelper.getContextMap().put("profiles", enabledProfiles);
		pebbleHelper.getContextMap().put("activeProfile", activeProfileOpt);
		pebbleHelper.getContextMap().put("blogfollows", followedBlogs);
	}

	private static String generateNavbar(List<Profile> enabledProfiles, Profile activeProfileOpt, List<String> followedBlogs, String rootTemplate) throws PebbleException {
		
		PebbleHelper pebbleHelper = getRawPebbleHelper("web/navbar.html");
		addDataToPebbleHelper(pebbleHelper, enabledProfiles, activeProfileOpt, followedBlogs);
		if(rootTemplate.endsWith("blog.html"))
		{
			pebbleHelper.getContextMap().put("leftnavbar", getRawPebbleHelper("web/blogleftnavbar.html").evaluate());
		}else
		{
			pebbleHelper.getContextMap().put("leftnavbar", getRawPebbleHelper("web/searchnavbar.html").evaluate());
		}
		
		
		return pebbleHelper.evaluate();
		

	}

	public String evaluate() throws PebbleException {
		try (Writer writer = new StringWriter();) {
			template.evaluate(writer, contextMap);
			return writer.toString();
		} catch (IOException e) {
			throw new PebbleException(e, e.getMessage());
		}
	}

	public Map<String, Object> getContextMap() {
		return contextMap;
	}

}
