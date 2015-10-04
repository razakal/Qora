package webserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import kryo.DiffHelper;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.util.StringUtil;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import qora.account.Account;
import qora.blockexplorer.BlockExplorer;
import qora.crypto.Base58;
import qora.crypto.Base64;
import qora.naming.Name;
import qora.transaction.ArbitraryTransaction;
import qora.web.BlogBlackWhiteList;
import qora.web.BlogProfile;
import qora.web.HTMLSearchResult;
import qora.web.NameStorageMap;
import qora.web.NameStorageTransactionHistory;
import qora.web.NavbarElements;
import qora.web.Profile;
import qora.web.ProfileHelper;
import qora.web.ServletUtils;
import qora.web.WebNameStorageHistoryHelper;
import qora.web.blog.BlogEntry;
import settings.Settings;
import utils.AccountBalanceComparator;
import utils.BlogUtils;
import utils.JSonWriter;
import utils.NameUtils;
import utils.NameUtils.NameResult;
import utils.Pair;
import utils.PebbleHelper;
import utils.Qorakeys;
import utils.StorageUtils;
import utils.StrJSonFine;
import utils.UpdateUtil;
import api.BlogPostResource;
import api.NameStorageResource;

import com.mitchellbosecke.pebble.error.PebbleException;

import controller.Controller;
import database.DBSet;
import database.NameMap;

@Path("/")
public class WebResource {
	@Context
	HttpServletRequest request;

	@GET
	public Response Default() {

		// REDIRECT
		return Response.status(302).header("Location", "index/main.html")
				.build();
	}

	public Response handleDefault() {
		try {

			String searchValue = request.getParameter("search");

			PebbleHelper pebbleHelper = PebbleHelper.getPebbleHelper(
					"web/main.mini.html", request);

			if (searchValue == null) {

				return Response.ok(
						PebbleHelper.getPebbleHelper("web/main.html", request)
								.evaluate(), "text/html; charset=utf-8")
						.build();
			}

			if (StringUtils.isBlank(searchValue)) {
				return Response.ok(

				pebbleHelper.evaluate(), "text/html; charset=utf-8").build();
			}

			List<Pair<String, String>> searchResults;
			searchResults = NameUtils.getWebsitesByValue(searchValue);

			List<HTMLSearchResult> results = generateHTMLSearchresults(searchResults);

			pebbleHelper.getContextMap().put("searchresults", results);

			return Response.ok(pebbleHelper.evaluate(),
					"text/html; charset=utf-8").build();
		} catch (Throwable e) {
			e.printStackTrace();
			return error404(request, null);
		}
	}

	public List<HTMLSearchResult> generateHTMLSearchresults(
			List<Pair<String, String>> searchResults) throws IOException,
			PebbleException {
		List<HTMLSearchResult> results = new ArrayList<>();
		for (Pair<String, String> result : searchResults) {
			String name = result.getA();
			String websitecontent = result.getB();
			Document htmlDoc = Jsoup.parse(websitecontent);
			String title = selectTitleOpt(htmlDoc);
			title = title == null ? "" : title;
			String description = selectDescriptionOpt(htmlDoc);
			description = description == null ? "" : description;
			description = StringUtils.abbreviate(description, 150);
			results.add(new HTMLSearchResult(title, description, name, "/"
					+ name, "/" + name, "/namestorage:" + name, null));

		}
		return results;
	}

	@SuppressWarnings("rawtypes")
	@Path("index/blockexplorer.json")
	@GET
	public Response jsonQueryMain(@Context UriInfo info) {
		Map output = BlockExplorer.getInstance().jsonQueryMain(info);

		return Response.status(200)
				.header("Content-Type", "application/json; charset=utf-8")
				.entity(StrJSonFine.convert(JSONValue.toJSONString(output)))
				.build();
	}

	@Path("index/blockexplorer")
	@GET
	public Response blockexplorer() {
		return blockexplorerhtml();
	}

	@Path("index/blockexplorer.html")
	@GET
	public Response blockexplorerhtml() {
		try {
			String content = readFile("web/blockexplorer.html",
					StandardCharsets.UTF_8);

			return Response.ok(content, "text/html; charset=utf-8").build();
		} catch (IOException e) {
			e.printStackTrace();
			return error404(request, null);
		}
	}

	@Path("index/blogsearch.html")
	@GET
	public Response doBlogSearch() {

		String searchValue = request.getParameter("search");
		try {
			PebbleHelper pebbleHelper = PebbleHelper.getPebbleHelper(
					"web/main.mini.html", request);
			if (StringUtil.isBlank(searchValue)) {

				return Response.ok(pebbleHelper.evaluate(),
						"text/html; charset=utf-8").build();
			}

			List<HTMLSearchResult> results = handleBlogSearch(searchValue);
			pebbleHelper.getContextMap().put("searchresults", results);
			return Response.ok(pebbleHelper.evaluate(),
					"text/html; charset=utf-8").build();

		} catch (Throwable e) {
			e.printStackTrace();
			return error404(request, null);
		}

	}
	
	@Path("index/nsrepopulate.html")
	@GET
	public Response doNsRepopulate() {
		
			UpdateUtil.repopulateNameStorage(70000);
			return error404(request, "Namestorage repopulated!");
		
	}
	

	@Path("index/namestorage.html")
	@GET
	public Response doNameStorage() {

		String name = request.getParameter("name");
		String key = request.getParameter("key");
		try {
			PebbleHelper pebbleHelper = PebbleHelper.getPebbleHelper(
					"web/namestorage.html", request);

			List<Name> namesAsList = new CopyOnWriteArrayList<Name>(Controller
					.getInstance().getNamesAsList());

			pebbleHelper.getContextMap().put("names", namesAsList);

			Name nameobj = null;
			if (name != null) {
				nameobj = Controller.getInstance().getName(name);
				if (nameobj == null) {
					return error404(request,
							"You don't own this name or it is not confirmed by now!");
				}
			}

			if (namesAsList.size() > 0) {

				if (name == null) {
					Profile activeProfileOpt = ProfileHelper.getInstance()
							.getActiveProfileOpt(request);
					if (activeProfileOpt != null) {
						nameobj = activeProfileOpt.getName();
					} else {
						nameobj = namesAsList.get(0);
					}
				}

				String websiteOpt;
				if (key != null) {
					websiteOpt = DBSet.getInstance().getNameStorageMap()
							.getOpt(nameobj.getName(), key);
					pebbleHelper.getContextMap().put("key", key);
				} else {
					websiteOpt = DBSet
							.getInstance()
							.getNameStorageMap()
							.getOpt(nameobj.getName(),
									Qorakeys.WEBSITE.toString());
				}

				pebbleHelper.getContextMap().put("name", nameobj.getName());
				pebbleHelper.getContextMap().put("website", websiteOpt);

			} else {
				pebbleHelper
						.getContextMap()
						.put("result",
								"<div class=\"alert alert-danger\" role=\"alert\">You need to register a name to create a website.<br></div>");
			}
			return Response.ok(pebbleHelper.evaluate(),
					"text/html; charset=utf-8").build();

		} catch (Throwable e) {
			e.printStackTrace();
			return error404(request, null);
		}

	}

	@Path("index/blogdirectory.html")
	@GET
	public Response doBlogdirectory() {

		try {
			PebbleHelper pebbleHelper = PebbleHelper.getPebbleHelper(
					"web/main.mini.html", request);

			List<HTMLSearchResult> results = handleBlogSearch(null);
			pebbleHelper.getContextMap().put("searchresults", results);
			return Response.ok(pebbleHelper.evaluate(),
					"text/html; charset=utf-8").build();
		} catch (Throwable e) {
			e.printStackTrace();
			return error404(request, null);
		}

	}

	@Path("index/namestoragehistory.html")
	@GET
	public Response getNameStorage() {

		try {
			String amount = request.getParameter("amount");
			String name = request.getParameter("name");

			Integer maxAmount = 20;
			try {
				maxAmount = Integer.valueOf(amount);
			} catch (Throwable e) {
				// then we use default!
			}

			if (StringUtils.isBlank(name)) {
				Profile activeProfileOpt = ProfileHelper.getInstance()
						.getActiveProfileOpt(request);

				if (activeProfileOpt != null) {
					name = activeProfileOpt.getName().getName();
				} else {
					List<Name> namesAsList = new CopyOnWriteArrayList<Name>(
							Controller.getInstance().getNamesAsList());

					if (namesAsList.size() > 0) {
						name = namesAsList.get(0).getName();
					}
				}

			}

			name = name == null ? "" : name;

			PebbleHelper pebbleHelper = PebbleHelper.getPebbleHelper(
					"web/namestoragehistory.html", request);

			List<NameStorageTransactionHistory> history = WebNameStorageHistoryHelper
					.getHistory(name, maxAmount);

			pebbleHelper.getContextMap().put("history", history);
			pebbleHelper.getContextMap().put("name", name);
			pebbleHelper.getContextMap().put("amount", maxAmount);

			return Response.ok(pebbleHelper.evaluate(),
					"text/html; charset=utf-8").build();
		} catch (Throwable e) {
			e.printStackTrace();
			return error404(request, null);
		}

	}

	@Path("index/messaging.html")
	@GET
	public Response getMessaging() {

		try {
			PebbleHelper pebbleHelper = PebbleHelper.getPebbleHelper(
					"web/messaging.html", request);
			return Response.ok(pebbleHelper.evaluate(),
					"text/html; charset=utf-8").build();
		} catch (Throwable e) {
			e.printStackTrace();
			return error404(request, null);
		}

	}

	@POST
	@Path("index/websitepreview.html")
	@Consumes("application/x-www-form-urlencoded")
	public Response previewWebsite(@Context HttpServletRequest request,
			MultivaluedMap<String, String> form) {

		try {
			String website = form.getFirst("website");

			website = website == null ? "" : website;

			return enhanceAndShowWebsite(website);

		} catch (Throwable e) {
			e.printStackTrace();
			return error404(request, null);
		}

	}

	@SuppressWarnings("unchecked")
	@POST
	@Path("index/api.html")
	@Consumes("application/x-www-form-urlencoded")
	public Response createApiCall(@Context HttpServletRequest request,
			MultivaluedMap<String, String> form) throws IOException {

		String type = form.getFirst("type");
		String apiurl = form.getFirst("apiurl");

		String jsonContent = form.getFirst("json");
		JSONObject json = new JSONObject();
		JSONObject jsonanswer = new JSONObject();
		if (StringUtils.isNotBlank(jsonContent)) {
			json = (JSONObject) JSONValue.parse(jsonContent);
		}

		if (StringUtils.isBlank(type)
				|| (!type.equalsIgnoreCase("get")
						&& !type.equalsIgnoreCase("post") && !type
							.equalsIgnoreCase("delete"))) {

			jsonanswer.put("type", "apicallerror");
			jsonanswer.put("errordetail",
					"type parameter must be post, get or delete");

			return Response.status(200)
					.header("Content-Type", "application/json; charset=utf-8")
					.entity(jsonanswer.toJSONString()).build();
		}

		if (StringUtils.isBlank(apiurl)) {
			jsonanswer.put("type", "apicallerror");
			jsonanswer.put("errordetail",
					"apiurl parameter must be correct set");

			return Response.status(200)
					.header("Content-Type", "application/json; charset=utf-8")
					.entity(jsonanswer.toJSONString()).build();
		}

		// CREATE CONNECTION

		apiurl = apiurl.startsWith("/") ? apiurl.substring(1) : apiurl;

		URL urlToCall = new URL("http://127.0.0.1:"
				+ Settings.getInstance().getRpcPort() + "/" + apiurl);
		HttpURLConnection connection = (HttpURLConnection) urlToCall
				.openConnection();

		// EXECUTE
		connection.setRequestMethod(type.toUpperCase());

		if (type.equalsIgnoreCase("POST")) {
			connection.setDoOutput(true);
			connection.getOutputStream().write(
					json.toJSONString().getBytes("UTF-8"));
			connection.getOutputStream().flush();
			connection.getOutputStream().close();
		}

		// READ RESULT
		InputStream stream;
		if (connection.getResponseCode() == 400) {
			stream = connection.getErrorStream();
		} else {
			stream = connection.getInputStream();
		}

		InputStreamReader isReader = new InputStreamReader(stream, "UTF-8");
		BufferedReader br = new BufferedReader(isReader);
		String result = br.readLine();

		if (result.contains("message") && result.contains("error")) {
			jsonanswer.put("type", "apicallerror");
			jsonanswer.put("errordetail", result);
			return Response.status(200)
					.header("Content-Type", "application/json; charset=utf-8")
					.entity(jsonanswer.toJSONString()).build();
		} else {
			jsonanswer.put("type", "success");
			jsonanswer.put("result", result);
			return Response.status(200)
					.header("Content-Type", "application/json; charset=utf-8")
					.entity(jsonanswer.toJSONString()).build();
		}

	}

	@SuppressWarnings("unchecked")
	@POST
	@Path("index/encodefile.html")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadMultipart(@FormDataParam("file") FormDataBodyPart is)
			throws IOException {
		try {
			InputStream valueAs = is.getValueAs(InputStream.class);
			byte[] byteArray = IOUtils.toByteArray(valueAs);
			String encode = Base64.encode(byteArray);
			MediaType mediaType = is.getMediaType();
			String result = "data:" + mediaType.getType() + "/"
					+ mediaType.getSubtype() + ";base64, ";
			result += encode;

			JSONObject json = new JSONObject();
			if (StringUtils.isEmpty(encode)) {
				json.put("type", "error");
				json.put("result", "You did not choose a file or the file was empty!");
			}else if (checkPlainTypes(mediaType)) {
				json.put("type", "success");
				json.put("result", new String(byteArray));
			} else {
				json.put("type", "success");
				json.put("result", result);
			}

			return Response.status(200)
					.header("Content-Type", "application/json; charset=utf-8")
					.entity(json.toJSONString()).build();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
		// prepare the response
	}

	public boolean checkPlainTypes(MediaType mediaType) {
		
		List<Pair<String,String>> pairsToCheck = new ArrayList<Pair<String,String>>();
		pairsToCheck.add(new Pair<String, String>("text", "html"));
		pairsToCheck.add(new Pair<String, String>("text", "plain"));
		
		for (Pair<String, String> pair : pairsToCheck) {
			if(pair.getA().equalsIgnoreCase(mediaType.getType()) && pair.getB().equalsIgnoreCase(mediaType.getSubtype()))
			{
				return true;
			}
		}
		
		return false;
	}

	@SuppressWarnings("unchecked")
	@POST
	@Path("index/websitesave.html")
	@Consumes("application/x-www-form-urlencoded")
	public Response saveWebsite(@Context HttpServletRequest request,
			MultivaluedMap<String, String> form) {

		String name = form.getFirst("name");
		String website = form.getFirst("website");
		String key = form.getFirst("key");

		JSONObject json = new JSONObject();

		if (key != null && !key.equalsIgnoreCase(Qorakeys.WEBSITE.toString())) {
			if (Qorakeys.isPartOf(key)) {
				json.put("type", "badKey");

				return Response
						.status(200)
						.header("Content-Type",
								"application/json; charset=utf-8")
						.entity(json.toJSONString()).build();
			}
		}

		if (StringUtils.isBlank(name)) {
			json.put("type", "parametersMissing");

			return Response.status(200)
					.header("Content-Type", "application/json; charset=utf-8")
					.entity(json.toJSONString()).build();
		}

		// TODO
		Pair<String, String> websitepair;
		if (StringUtils.isNotBlank(key)) {
			websitepair = new Pair<String, String>(key, website);
		} else {
			websitepair = new Pair<String, String>(Qorakeys.WEBSITE.toString(),
					website);
		}

		JSONObject storageJsonObject = null;
		if (website == null || website.isEmpty()) {

			storageJsonObject = StorageUtils
					.getStorageJsonObject(
							null,
							Collections.singletonList(StringUtils.isBlank(key) ? Qorakeys.WEBSITE
									.toString() : key), null, null, null, null);
		} else {

			try {
				String source = DBSet.getInstance().getNameStorageMap()
						.getOpt(name, websitepair.getA());

				if (StringUtils.isNotBlank(source)) {
					String diff = DiffHelper.getDiff(source, website);

					if (website.length() > diff.length()
							&& diff.length() < 3500) {
						websitepair.setB(diff);
						storageJsonObject = StorageUtils.getStorageJsonObject(
								null, null, null, null, null,
								Collections.singletonList(websitepair));
					}
				}

			} catch (Throwable e) {
				e.printStackTrace();
			}

			if (storageJsonObject == null) {
				storageJsonObject = StorageUtils.getStorageJsonObject(
						Collections.singletonList(websitepair), null, null,
						null, null, null);
			}
		}

		new NameStorageResource().updateEntry(storageJsonObject.toString(),
				name);

		json.put("type", "settingsSuccessfullySaved");
		return Response.status(200)
				.header("Content-Type", "application/json; charset=utf-8")
				.entity(json.toJSONString()).build();

	}

	@SuppressWarnings("unchecked")
	@POST
	@Path("index/settingssave.html")
	@Consumes("application/x-www-form-urlencoded")
	public Response saveProfileSettings(@Context HttpServletRequest request,
			MultivaluedMap<String, String> form) {

		JSONObject json = new JSONObject();

		try {

			String profileName = form.getFirst("profilename");

			if (StringUtils.isBlank(profileName)) {
				json.put("type", "parametersMissing");

				return Response
						.status(200)
						.header("Content-Type",
								"application/json; charset=utf-8")
						.entity(json.toJSONString()).build();
			}

			Name name = null;
			name = Controller.getInstance().getName(profileName);

			if (name == null || !Profile.isAllowedProfileName(profileName)) {

				json.put("type", "profileNameisnotAllowed");
				return Response
						.status(200)
						.header("Content-Type",
								"application/json; charset=utf-8")
						.entity(json.toJSONString()).build();
			}

			boolean blogenable = Boolean.valueOf(form
					.getFirst(Qorakeys.BLOGENABLE.toString()));
			boolean profileenable = Boolean.valueOf(form
					.getFirst(Qorakeys.PROFILEENABLE.toString()));
			String titleOpt = form.getFirst(Qorakeys.BLOGTITLE.toString());
			titleOpt = decodeIfNotNull(titleOpt);
			String blogDescrOpt = form.getFirst(Qorakeys.BLOGDESCRIPTION
					.toString());
			blogDescrOpt = decodeIfNotNull(blogDescrOpt);
			String profileAvatarOpt = form.getFirst(Qorakeys.PROFILEAVATAR
					.toString());
			String profileBannerOpt = form.getFirst(Qorakeys.PROFILEMAINGRAPHIC
					.toString());

			String bwlistkind = form.getFirst("bwlistkind");
			String blackwhitelist = form.getFirst("blackwhitelist");
			blackwhitelist = URLDecoder.decode(blackwhitelist, "UTF-8");

			profileAvatarOpt = decodeIfNotNull(profileAvatarOpt);
			profileBannerOpt = decodeIfNotNull(profileBannerOpt);

			Profile profile = Profile.getProfileOpt(name);
			profile.saveAvatarTitle(profileAvatarOpt);
			profile.saveProfileMainGraphicOpt(profileBannerOpt);
			profile.saveBlogDescription(blogDescrOpt);
			profile.saveBlogTitle(titleOpt);
			profile.setBlogEnabled(blogenable);
			profile.setProfileEnabled(profileenable);

			profile.getBlogBlackWhiteList().clearList();
			profile.getBlogBlackWhiteList().setWhitelist(
					!bwlistkind.equals("black"));
			String[] bwList = StringUtils.split(blackwhitelist, ";");
			for (String listentry : bwList) {
				profile.getBlogBlackWhiteList().addAddressOrName(listentry);
			}

			try {

				profile.saveProfile();

				json.put("type", "settingsSuccessfullySaved");
				return Response
						.status(200)
						.header("Content-Type",
								"application/json; charset=utf-8")
						.entity(json.toJSONString()).build();

			} catch (WebApplicationException e) {

				json = new JSONObject();
				json.put("type", "error");
				json.put("error", e.getResponse().getEntity());

				return Response
						.status(200)
						.header("Content-Type",
								"application/json; charset=utf-8")
						.entity(json.toJSONString()).build();
			}

		} catch (Throwable e) {
			e.printStackTrace();

			json.put("type", "error");
			json.put("error", e.getMessage());

			return Response.status(200)
					.header("Content-Type", "application/json; charset=utf-8")
					.entity(json.toJSONString()).build();
		}

	}

	@Path("index/settings.html")
	@GET
	public Response doProfileSettings() {

		try {

			PebbleHelper pebbleHelper = PebbleHelper.getPebbleHelper(
					"web/settings.html", request);

			if (ServletUtils.isRemoteRequest(request)) {
				return error404(request,
						"This page is disabled for remote usage");
			}

			String profileName = request.getParameter("profilename");

			List<Name> namesAsList = new CopyOnWriteArrayList<Name>(Controller
					.getInstance().getNamesAsList());

			for (Name name : namesAsList) {
				if (!Profile.isAllowedProfileName(name.getName())) {
					namesAsList.remove(name);
				}
			}

			pebbleHelper.getContextMap().put("names", namesAsList);

			handleSelectNameAndProfile(pebbleHelper, profileName, namesAsList);

			return Response.ok(pebbleHelper.evaluate(),
					"text/html; charset=utf-8").build();
		} catch (Throwable e) {
			e.printStackTrace();
			return error404(request, null);
		}

	}

	public void handleSelectNameAndProfile(PebbleHelper pebbleHelper,
			String profileName, List<Name> namesAsList) {
		Name name = null;
		if (profileName != null) {
			name = Controller.getInstance().getName(profileName);
		}

		if (namesAsList.size() > 0) {

			if (name == null) {
				Profile activeProfileOpt = ProfileHelper.getInstance()
						.getActiveProfileOpt(request);
				if (activeProfileOpt != null) {
					name = activeProfileOpt.getName();
				} else {
					name = namesAsList.get(0);
				}
			}

			// WE HAVE HERE ONLY ALLOWED NAMES SO PROFILE CAN'T BE NULL HERE
			Profile profile = Profile.getProfileOpt(name);

			pebbleHelper.getContextMap().put("profile", profile);
			pebbleHelper.getContextMap().put("name", name);

		} else {
			pebbleHelper
					.getContextMap()
					.put("result",
							"<div class=\"alert alert-danger\" role=\"alert\">You need to register a name to create a profile.<br></div>");
		}
	}

	public String decodeIfNotNull(String parameter)
			throws UnsupportedEncodingException {
		return parameter != null ? URLDecoder.decode(parameter, "UTF-8") : null;
	}

	@Path("index/webdirectory.html")
	@GET
	public Response doWebdirectory() {

		try {
			PebbleHelper pebbleHelper = PebbleHelper.getPebbleHelper(
					"web/main.mini.html", request);

			List<Pair<String, String>> websitesByValue = NameUtils
					.getWebsitesByValue(null);
			List<HTMLSearchResult> results = generateHTMLSearchresults(websitesByValue);

			pebbleHelper.getContextMap().put("searchresults", results);

			return Response.ok(pebbleHelper.evaluate(),
					"text/html; charset=utf-8").build();

		} catch (Throwable e) {
			e.printStackTrace();
			return error404(request, null);
		}

	}

	private List<HTMLSearchResult> handleBlogSearch(String blogSearchOpt) {
		List<HTMLSearchResult> results = new ArrayList<>();
		List<BlogProfile> allEnabledBlogs = BlogUtils
				.getEnabledBlogs(blogSearchOpt);
		for (BlogProfile blogProfile : allEnabledBlogs) {
			String name = blogProfile.getProfile().getName().getName();
			String title = blogProfile.getProfile().getBlogTitleOpt();
			String description = blogProfile.getProfile()
					.getBlogDescriptionOpt();

			results.add(new HTMLSearchResult(title, description, name,
					"/index/blog.html?blogname=" + name,
					"/index/blog.html?blogname=" + name,
					"/namestorage:" + name, blogProfile.getFollower()));
		}

		return results;
	}

	public static String selectTitleOpt(Document htmlDoc) {
		String title = selectFirstElementOpt(htmlDoc, "title");

		return title;
	}

	public static String selectFirstElementOpt(Document htmlDoc, String tag) {
		Elements titleElements = htmlDoc.select(tag);
		String title = null;
		if (titleElements.size() > 0) {
			title = titleElements.get(0).text();
		}
		return title;
	}

	public static String selectDescriptionOpt(Document htmlDoc) {
		String result = "";
		Elements descriptions = htmlDoc.select("meta[name=\"description\"]");
		if (descriptions.size() > 0) {
			Element descr = descriptions.get(0);
			if (descr.hasAttr("content")) {
				result = descr.attr("content");
			}
		}

		return result;
	}

	@Path("index/main.html")
	@GET
	public Response handleIndex() {
		return handleDefault();
	}

	@Path("favicon.ico")
	@GET
	public Response favicon() {
		File file = new File("web/favicon.ico");

		if (file.exists()) {
			return Response.ok(file, "image/vnd.microsoft.icon").build();
		} else {
			return error404(request, null);
		}
	}

	@Path("index/favicon.ico")
	@GET
	public Response indexfavicon() {
		File file = new File("web/favicon.ico");

		if (file.exists()) {
			return Response.ok(file, "image/vnd.microsoft.icon").build();
		} else {
			return error404(request, null);
		}
	}

	String[] imgsArray = { "qora.png", "logo_header.png", "qora-user.png",
			"logo_bottom.png", "banner_01.png", "loading.gif",
			"00_generating.png", "01_genesis.jpg", "02_payment_in.png",
			"02_payment_out.png", "03_name_registration.png",
			"04_name_update.png", "05_name_sale.png",
			"06_cancel_name_sale.png", "07_name_purchase_in.png",
			"07_name_purchase_out.png", "08_poll_creation.jpg",
			"09_poll_vote.jpg", "10_arbitrary_transaction.png",
			"11_asset_issue.png", "12_asset_transfer_in.png",
			"12_asset_transfer_out.png", "13_order_creation.png",
			"14_cancel_order.png", "15_multi_payment_in.png",
			"15_multi_payment_out.png", "16_deploy_at.png",
			"17_message_in.png", "17_message_out.png", "asset_trade.png",
			"at_tx_in.png", "at_tx_out.png", "grleft.png", "grright.png",
			"redleft.png", "redright.png", "bar.gif", "bar_left.gif",
			"bar_right.gif" };

	@Path("index/img/{filename}")
	@GET
	public Response image(@PathParam("filename") String filename) {
		ArrayList<String> imgs = new ArrayList<String>();

		imgs.addAll(Arrays.asList(imgsArray));

		int imgnum = imgs.indexOf(filename);

		if (imgnum == -1) {
			return error404(request, null);
		}

		File file = new File("web/img/" + imgs.get(imgnum));
		String type = "";

		switch (getFileExtention(imgs.get(imgnum))) {
		case "png":
			type = "image/png";
			break;
		case "gif":
			type = "image/gif";
			break;
		case "jpg":
			type = "image/jpeg";
			break;
		}

		if (file.exists()) {
			return Response.ok(file, type).build();
		} else {
			return error404(request, null);
		}
	}

	public static String getFileExtention(String filename) {
		int dotPos = filename.lastIndexOf(".") + 1;
		return filename.substring(dotPos);
	}

	@Path("index/libs/css/style.css")
	@GET
	public Response style() {
		File file = new File("web/libs/css/style.css");

		if (file.exists()) {
			return Response.ok(file, "text/css").build();
		} else {
			return error404(request, null);
		}
	}

	@Path("index/libs/css/sidebar.css")
	@GET
	public Response sidebarcss() {
		File file = new File("web/libs/css/sidebar.css");

		if (file.exists()) {
			return Response.ok(file, "text/css").build();
		} else {
			return error404(request, null);
		}
	}

	@Path("index/libs/css/timeline.css")
	@GET
	public Response timelinecss() {
		File file = new File("web/libs/css/timeline.css");

		if (file.exists()) {
			return Response.ok(file, "text/css").build();
		} else {
			return error404(request, null);
		}
	}

	@Path("index/libs/js/sidebar.js")
	@GET
	public Response sidebarjs() {
		File file = new File("web/libs/js/sidebar.js");

		if (file.exists()) {
			return Response.ok(file, "text/javascript").build();
		} else {
			return error404(request, null);
		}
	}

	@Path("index/libs/js/third-party/highlight.pack.js")
	@GET
	public Response highlightpackjs() {
		File file = new File("web/libs/js/third-party/highlight.pack.js");

		if (file.exists()) {
			return Response.ok(file, "text/javascript").build();
		} else {
			return error404(request, null);
		}
	}

	@Path("index/libs/js/third-party/github.css")
	@GET
	public Response highgitcss() {
		File file = new File("web/libs/js/third-party/github.css");

		if (file.exists()) {
			return Response.ok(file, "text/css").build();
		} else {
			return error404(request, null);
		}
	}

	@Path("index/libs/js/third-party/ZeroClipboard.min.js")
	@GET
	public Response ZeroClipboardmin() {
		File file = new File("web/libs/js/third-party/ZeroClipboard.min.js");

		if (file.exists()) {
			return Response.ok(file, "text/javascript").build();
		} else {
			return error404(request, null);
		}
	}

	@Path("index/libs/js/third-party/ZeroClipboard.swf")
	@GET
	public Response ZeroClipboard() {
		File file = new File("web/libs/js/third-party/ZeroClipboard.swf");

		if (file.exists()) {
			return Response.ok(file, "text/javascript").build();
		} else {
			return error404(request, null);
		}
	}

	@Path("index/libs/js/biginteger.js")
	@GET
	public Response biginteger() {
		File file = new File("web/libs/js/biginteger.js");

		if (file.exists()) {
			return Response.ok(file, "text/javascript").build();
		} else {
			return error404(request, null);
		}
	}

	@Path("index/libs/js/converters.js")
	@GET
	public Response converters() {
		File file = new File("web/libs/js/converters.js");

		if (file.exists()) {
			return Response.ok(file, "text/javascript").build();
		} else {
			return error404(request, null);
		}
	}

	@Path("index/libs/js/crypto/curve25519.js")
	@GET
	public Response curve25519() {
		File file = new File("web/libs/js/crypto/curve25519.js");

		if (file.exists()) {
			return Response.ok(file, "text/javascript").build();
		} else {
			return error404(request, null);
		}
	}

	@Path("index/libs/js/crypto/curve25519_.js")
	@GET
	public Response curve25519_() {
		File file = new File("web/libs/js/crypto/curve25519_.js");

		if (file.exists()) {
			return Response.ok(file, "text/javascript").build();
		} else {
			return error404(request, null);
		}
	}

	@Path("index/libs/js/crypto/3rdparty/cryptojs/sha256.js")
	@GET
	public Response sha256() {
		File file = new File("web/libs/js/crypto/3rdparty/cryptojs/sha256.js");

		if (file.exists()) {
			return Response.ok(file, "text/javascript").build();
		} else {
			return error404(request, null);
		}
	}

	@SuppressWarnings("unchecked")
	@POST
	@Path("index/postblogprocessing.html")
	@Consumes("application/x-www-form-urlencoded")
	public Response postBlogProcessing(@Context HttpServletRequest request,
			MultivaluedMap<String, String> form) {
		JSONObject json = new JSONObject();

		String title = form.getFirst(BlogPostResource.TITLE_KEY);
		String creator = form.getFirst("creator");
		String contentparam = form.getFirst("content");
		String preview = form.getFirst("preview");
		String blogname = form.getFirst(BlogPostResource.BLOGNAME_KEY);

		if (StringUtil.isNotBlank(creator)
				&& StringUtil.isNotBlank(contentparam)) {

			JSONObject jsonBlogPost = new JSONObject();

			Pair<Account, NameResult> nameToAdress = NameUtils
					.nameToAdress(creator);

			String authorOpt = null;
			if (nameToAdress.getB() == NameResult.OK) {
				authorOpt = creator;
				jsonBlogPost.put(BlogPostResource.AUTHOR, authorOpt);
				jsonBlogPost.put("creator", nameToAdress.getA().getAddress());
			} else {
				jsonBlogPost.put("creator", creator);
			}

			jsonBlogPost.put("title", title);
			jsonBlogPost.put("body", contentparam);

			if (StringUtils.isNotBlank(preview) && preview.equals("true")) {
				json.put("type", "preview");

				BlogEntry entry = new BlogEntry(title, contentparam, authorOpt,
						new Date().getTime(), creator, "", blogname);

				json.put("previewBlogpost", entry.toJson());

				return Response
						.status(200)
						.header("Content-Type",
								"application/json; charset=utf-8")
						.entity(json.toJSONString()).build();
			}

			try {

				jsonBlogPost.put(
						"fee",
						Controller
								.getInstance()
								.calcRecommendedFeeForArbitraryTransaction(
										jsonBlogPost.toJSONString().getBytes())
								.getA().toPlainString());

				String result = new BlogPostResource().addBlogEntry(
						jsonBlogPost.toJSONString(), blogname);

				json.put("type", "postSuccessful");
				json.put("result", result);

				return Response
						.status(200)
						.header("Content-Type",
								"application/json; charset=utf-8")
						.entity(json.toJSONString()).build();

			} catch (WebApplicationException e) {

				json = new JSONObject();
				json.put("type", "error");
				json.put("error", e.getResponse().getEntity());

				return Response
						.status(200)
						.header("Content-Type",
								"application/json; charset=utf-8")
						.entity(json.toJSONString()).build();
			}
		}

		return Response.status(200)
				.header("Content-Type", "application/json; charset=utf-8")
				.entity(json.toJSONString()).build();
	}

	@Path("index/postblog.html")
	@GET
	public Response postBlog() {

		try {

			PebbleHelper pebbleHelper = PebbleHelper.getPebbleHelper(
					"web/postblog.html", request);

			pebbleHelper.getContextMap().put("errormessage", "");
			pebbleHelper.getContextMap().put("font", "");
			pebbleHelper.getContextMap().put("content", "");
			pebbleHelper.getContextMap().put("option", "");
			pebbleHelper.getContextMap().put("oldtitle", "");
			pebbleHelper.getContextMap().put("oldcreator", "");
			pebbleHelper.getContextMap().put("oldcontent", "");
			pebbleHelper.getContextMap().put("oldfee", "");
			pebbleHelper.getContextMap().put("preview", "");

			String blogname = request
					.getParameter(BlogPostResource.BLOGNAME_KEY);

			BlogBlackWhiteList blogBlackWhiteList = BlogBlackWhiteList
					.getBlogBlackWhiteList(blogname);

			Pair<List<Account>, List<Name>> ownAllowedElements = blogBlackWhiteList
					.getOwnAllowedElements(true);

			List<Account> resultingAccounts = new ArrayList<Account>(
					ownAllowedElements.getA());
			List<Name> resultingNames = ownAllowedElements.getB();

			Collections.sort(resultingAccounts, new AccountBalanceComparator());
			Collections.reverse(resultingAccounts);

			String accountStrings = "";

			for (Name name : resultingNames) {
				accountStrings += "<option value=" + name.getName() + ">"
						+ name.getNameBalanceString() + "</option>";
			}

			for (Account account : resultingAccounts) {
				accountStrings += "<option value=" + account.getAddress() + ">"
						+ account + "</option>";
			}

			// are we allowed to post
			if (resultingNames.size() == 0 && resultingAccounts.size() == 0) {

				pebbleHelper
						.getContextMap()
						.put("errormessage",
								"<div id=\"result\"><div class=\"alert alert-dismissible alert-danger\" role=\"alert\"><button type=\"button\" class=\"close\" data-dismiss=\"alert\">x</button>You can't post to this blog! None of your accounts has balance or the blog owner did not allow your accounts to post!<br></div></div>");

			}

			Profile activeProfileOpt = ProfileHelper.getInstance()
					.getActiveProfileOpt(request);

			if (activeProfileOpt != null
					&& resultingNames.contains(activeProfileOpt.getName())) {
				pebbleHelper.getContextMap().put("primaryname",
						activeProfileOpt.getName().getName());
			}

			pebbleHelper.getContextMap().put("option", accountStrings);

			return Response.ok(pebbleHelper.evaluate(),
					"text/html; charset=utf-8").build();
		} catch (Throwable e) {
			e.printStackTrace();
			return error404(request, null);
		}
	}

	@SuppressWarnings("unchecked")
	@POST
	@Path("index/followblog.html")
	@Consumes("application/x-www-form-urlencoded")
	public Response followBlog(@Context HttpServletRequest request,
			MultivaluedMap<String, String> form) {
		try {

			JSONObject json = new JSONObject();

			String blogname = form.getFirst(BlogPostResource.BLOGNAME_KEY);
			String followString = form.getFirst("follow");
			NameMap nameMap = DBSet.getInstance().getNameMap();
			Profile activeProfileOpt = ProfileHelper.getInstance()
					.getActiveProfileOpt(request);

			if (followString != null && activeProfileOpt != null
					&& blogname != null && nameMap.contains(blogname)) {
				boolean follow = Boolean.valueOf(followString);
				Name name = nameMap.get(blogname);
				Profile profile = Profile.getProfileOpt(name);
				if (activeProfileOpt.isProfileEnabled()) {

					if (follow) {
						if (profile != null && profile.isProfileEnabled()
								&& profile.isBlogEnabled()) {
							String result;

							if (activeProfileOpt.getFollowedBlogs().contains(
									blogname)) {
								result = "<center><div class=\"alert alert-danger\" role=\"alert\">Blog follow not successful<br>"
										+ "You already follow this blog"
										+ "</div></center>";

								json.put("type", "youAlreadyFollowThisBlog");
								json.put("follower", profile.getFollower()
										.size());

								json.put("isFollowing", activeProfileOpt
										.getFollowedBlogs().contains(blogname));

								return Response
										.status(200)
										.header("Content-Type",
												"application/json; charset=utf-8")
										.entity(json.toJSONString()).build();
							}

							// Prevent following of own profiles
							if (Controller.getInstance()
									.getNamesAsListAsString()
									.contains(blogname)) {
								result = "<center><div class=\"alert alert-danger\" role=\"alert\">Blog follow not successful<br>"
										+ "You can't follow your own profiles"
										+ "</div></center>";

								json.put("type", "youCantFollowYourOwnProfiles");
								json.put("follower", profile.getFollower()
										.size());

								json.put("isFollowing", activeProfileOpt
										.getFollowedBlogs().contains(blogname));

								return Response
										.status(200)
										.header("Content-Type",
												"application/json; charset=utf-8")
										.entity(json.toJSONString()).build();
							}

							boolean isFollowing = activeProfileOpt
									.getFollowedBlogs().contains(blogname);

							try {

								activeProfileOpt.addFollowedBlog(blogname);
								result = activeProfileOpt.saveProfile();
								result = "<div class=\"alert alert-success\" role=\"alert\">You follow this blog now<br>"
										+ result + "</div>";

								json.put("type", "YouFollowThisBlogNow");
								json.put("result", result);
								json.put("follower", profile.getFollower()
										.size());
								json.put("isFollowing", activeProfileOpt
										.getFollowedBlogs().contains(blogname));

							} catch (WebApplicationException e) {
								result = "<center><div class=\"alert alert-danger\" role=\"alert\">Blog follow not successful<br>"
										+ e.getResponse().getEntity()
										+ "</div></center>";

								json.put("type", "BlogFollowNotSuccessful");
								json.put("result", e.getResponse().getEntity());
								json.put("follower", profile.getFollower()
										.size());
								json.put("isFollowing", isFollowing);

							}

							return Response
									.status(200)
									.header("Content-Type",
											"application/json; charset=utf-8")
									.entity(json.toJSONString()).build();
						}

					} else {

						boolean isFollowing = activeProfileOpt
								.getFollowedBlogs().contains(blogname);

						if (activeProfileOpt.getFollowedBlogs().contains(
								blogname)) {
							activeProfileOpt.removeFollowedBlog(blogname);
							String result;
							try {
								result = activeProfileOpt.saveProfile();
								result = "<div class=\"alert alert-success\" role=\"alert\">Unfollow successful<br>"
										+ result + "</div>";

								json.put("type", "unfollowSuccessful");
								json.put("result", result);
								json.put("follower", profile.getFollower()
										.size());
								json.put("isFollowing", activeProfileOpt
										.getFollowedBlogs().contains(blogname));
							} catch (WebApplicationException e) {
								result = "<center><div class=\"alert alert-danger\" role=\"alert\">Blog unfollow not successful<br>"
										+ e.getResponse().getEntity()
										+ "</div></center>";

								json.put("type", "blogUnfollowNotSuccessful");
								json.put("result", e.getResponse().getEntity());
								json.put("follower", profile.getFollower()
										.size());
								json.put("isFollowing", isFollowing);
							}

							return Response
									.status(200)
									.header("Content-Type",
											"application/json; charset=utf-8")
									.entity(json.toJSONString()).build();

						}
					}

				}
			}

			return getBlog(null);
		} catch (Throwable e) {
			e.printStackTrace();
			return Response.status(200)
					.header("Content-Type", "application/json; charset=utf-8")
					.entity("{}").build();
		}

	}

	@SuppressWarnings("unchecked")
	@POST
	@Path("index/deletepost.html")
	@Consumes("application/x-www-form-urlencoded")
	public Response deletePost(@Context HttpServletRequest request,
			MultivaluedMap<String, String> form) {

		JSONObject jsonanswer = new JSONObject();
		try {

			String signature = form.getFirst("signature");

			if (signature != null) {

				BlogEntry blogEntryOpt = BlogUtils.getBlogEntryOpt(signature);

				if (blogEntryOpt == null) {
					// TODO put this snippet in method
					jsonanswer.put("type", "deleteError");
					jsonanswer
							.put("errordetail",
									"The blog entry you are trying to delete does not exist!");

					return Response
							.status(200)
							.header("Content-Type",
									"application/json; charset=utf-8")
							.entity(jsonanswer.toJSONString()).build();
				}

				if (!Controller.getInstance().doesWalletDatabaseExists()) {
					jsonanswer.put("type", "deleteError");
					jsonanswer.put("errordetail", "You don't have a wallet!");

					return Response
							.status(200)
							.header("Content-Type",
									"application/json; charset=utf-8")
							.entity(jsonanswer.toJSONString()).build();
				}

				String creator = blogEntryOpt.getCreator();

				Account accountByAddress = Controller.getInstance()
						.getAccountByAddress(creator);
				String blognameOpt = blogEntryOpt.getBlognameOpt();
				// Did I create that blogpost?
				JSONObject jsonBlogPost = new JSONObject();
				jsonBlogPost.put(BlogPostResource.DELETE_KEY, signature);
				jsonBlogPost.put("body", "delete");
				if (accountByAddress != null) {
					// TODO create blogpost json in method --> move to BlogUtils
					// (for every kind delete/share and so on)
					jsonBlogPost.put("creator", creator);
					Pair<BigDecimal, Integer> fee = Controller.getInstance()
							.calcRecommendedFeeForArbitraryTransaction(
									jsonBlogPost.toJSONString().getBytes());
					jsonBlogPost.put("fee", fee.getA().toPlainString());
					// I am not author, but am I the owner of the blog?
				} else if (blognameOpt != null
						&& Controller.getInstance().getNamesAsListAsString()
								.contains(blognameOpt)) {
					Name name = DBSet.getInstance().getNameMap()
							.get(blognameOpt);
					jsonBlogPost.put("creator", name.getOwner().getAddress());
					jsonBlogPost.put(BlogPostResource.AUTHOR, blognameOpt);
				} else {
					jsonanswer.put("type", "deleteError");
					jsonanswer
							.put("errordetail",
									"You are not allowed to delete this post!You need to be owner of the blog or author of the blogpost!");

					return Response
							.status(200)
							.header("Content-Type",
									"application/json; charset=utf-8")
							.entity(jsonanswer.toJSONString()).build();
				}

				try {

					String result = new BlogPostResource().addBlogEntry(
							jsonBlogPost.toJSONString(), null);

					jsonanswer.put("type", "deleteSuccessful");
					jsonanswer.put("result", result);

					return Response
							.status(200)
							.header("Content-Type",
									"application/json; charset=utf-8")
							.entity(jsonanswer.toJSONString()).build();
				} catch (WebApplicationException e) {

					jsonanswer.put("type", "deleteError");
					jsonanswer.put("errordetail", e.getResponse().getEntity());

					return Response
							.status(200)
							.header("Content-Type",
									"application/json; charset=utf-8")
							.entity(jsonanswer.toJSONString()).build();

				}

			}

			jsonanswer.put("type", "deleteError");
			jsonanswer.put("errordetail",
					"the signature parameter must be set!");

			return Response.status(200)
					.header("Content-Type", "application/json; charset=utf-8")
					.entity(jsonanswer.toJSONString()).build();

		} catch (Throwable e) {
			e.printStackTrace();

			jsonanswer.put("type", "deleteError");
			jsonanswer.put("errordetail", e.getMessage());

			return Response.status(200)
					.header("Content-Type", "application/json; charset=utf-8")
					.entity(jsonanswer.toJSONString()).build();
		}

	}

	@SuppressWarnings("unchecked")
	@POST
	@Path("index/sharepost.html")
	@Consumes("application/x-www-form-urlencoded")
	public Response sharePost(@Context HttpServletRequest request,
			MultivaluedMap<String, String> form) {

		JSONObject json = new JSONObject();

		// TODO CHANGE ERROR RETURNING --> less html code! see delete post and
		// also processlike!
		try {

			String signature = form.getFirst("signature");
			String sourceBlog = form.getFirst("blogname");

			Profile activeProfileOpt = ProfileHelper.getInstance()
					.getActiveProfileOpt(request);

			if (activeProfileOpt != null && signature != null
					&& sourceBlog != null) {
				if (activeProfileOpt.isProfileEnabled()) {

					if (!activeProfileOpt.isBlogEnabled()) {
						json.put("type", "BlogIsDisabled");
						return Response
								.status(200)
								.header("Content-Type",
										"application/json; charset=utf-8")
								.entity(json.toJSONString()).build();
					}

					List<String> list = DBSet.getInstance().getSharedPostsMap()
							.get(Base58.decode(signature));
					if (list != null
							&& list.contains(activeProfileOpt.getName()
									.getName())) {
						json.put("type", "YouAlreadySharedThisPost");

						return Response
								.status(200)
								.header("Content-Type",
										"application/json; charset=utf-8")
								.entity(json.toJSONString()).build();
					}

					if (activeProfileOpt.getName().getName().equals(sourceBlog)) {
						json.put("type", "YouCantShareYourOwnPosts");

						return Response
								.status(200)
								.header("Content-Type",
										"application/json; charset=utf-8")
								.entity(json.toJSONString()).build();
					}

					JSONObject jsonBlogPost = new JSONObject();
					String profileName = activeProfileOpt.getName().getName();
					jsonBlogPost.put(BlogPostResource.AUTHOR, profileName);
					jsonBlogPost.put("creator", activeProfileOpt.getName()
							.getOwner().getAddress());
					jsonBlogPost.put(BlogPostResource.SHARE_KEY, signature);
					jsonBlogPost.put("body", "share");

					Pair<BigDecimal, Integer> fee = Controller.getInstance()
							.calcRecommendedFeeForArbitraryTransaction(
									jsonBlogPost.toJSONString().getBytes());
					jsonBlogPost.put("fee", fee.getA().toPlainString());

					try {

						String result = new BlogPostResource().addBlogEntry(
								jsonBlogPost.toJSONString(), profileName);

						json.put("type", "ShareSuccessful");
						json.put("result", result);

					} catch (WebApplicationException e) {

						json.put("type", "ShareNotSuccessful");
						json.put("result", e.getResponse().getEntity());
					}

					return Response
							.status(200)
							.header("Content-Type",
									"application/json; charset=utf-8")
							.entity(json.toJSONString()).build();

				}
			}

		} catch (Throwable e) {
			e.printStackTrace();

			json.put("type", "error");
			json.put("error", e.getMessage());

			return Response.status(200)
					.header("Content-Type", "application/json; charset=utf-8")
					.entity(json.toJSONString()).build();
		}

		return Response.status(200)
				.header("Content-Type", "application/json; charset=utf-8")
				.entity("{}").build();
	}

	@SuppressWarnings("unchecked")
	@POST
	@Path("index/likepost.html")
	@Consumes("application/x-www-form-urlencoded")
	public Response likePost(@Context HttpServletRequest request,
			MultivaluedMap<String, String> form) {

		JSONObject json = new JSONObject();

		try {

			String signature = form.getFirst("signature");
			String likeString = form.getFirst("like");

			Profile activeProfileOpt = ProfileHelper.getInstance()
					.getActiveProfileOpt(request);

			if (likeString != null && activeProfileOpt != null) {
				boolean like = Boolean.valueOf(likeString);
				if (activeProfileOpt.isProfileEnabled()) {

					if (like) {
						String result;

						if (activeProfileOpt.getLikedPosts()
								.contains(signature)) {

							json.put("type", "YouAlreadyLikeThisPost");

							return Response
									.status(200)
									.header("Content-Type",
											"application/json; charset=utf-8")
									.entity(json.toJSONString()).build();
						}

						BlogEntry blogEntryOpt = BlogUtils
								.getBlogEntryOpt((ArbitraryTransaction) Controller
										.getInstance().getTransaction(
												Base58.decode(signature)));

						boolean ownPost = false;
						if (blogEntryOpt != null) {
							if (Controller.getInstance().getAccountByAddress(
									blogEntryOpt.getCreator()) != null) {
								ownPost = true;
							}
						}

						if (ownPost) {

							json.put("type", "YouCantLikeYourOwnPosts");

							return Response
									.status(200)
									.header("Content-Type",
											"application/json; charset=utf-8")
									.entity(json.toJSONString()).build();

						}

						activeProfileOpt.addLikePost(signature);
						try {

							result = activeProfileOpt.saveProfile();

							json.put("type", "LikeSuccessful");
							json.put("result", result);

						} catch (WebApplicationException e) {

							json.put("type", "LikeNotSuccessful");
							json.put("result", e.getResponse().getEntity());
						}

						return Response
								.status(200)
								.header("Content-Type",
										"application/json; charset=utf-8")
								.entity(json.toJSONString()).build();
					} else {
						if (activeProfileOpt.getLikedPosts()
								.contains(signature)) {

							activeProfileOpt.removeLikeProfile(signature);
							String result;
							try {
								result = activeProfileOpt.saveProfile();

								json.put("type", "LikeRemovedSuccessful");
								json.put("result", result);

							} catch (WebApplicationException e) {

								json.put("type", "LikeRemovedNotSuccessful");
								json.put("result", e.getResponse().getEntity());

							}

							return Response
									.status(200)
									.header("Content-Type",
											"application/json; charset=utf-8")
									.entity(json.toJSONString()).build();
						}
					}

				}
			}

		} catch (Throwable e) {
			e.printStackTrace();

			json.put("type", "error");
			json.put("error", e.getMessage());

			return Response.status(200)
					.header("Content-Type", "application/json; charset=utf-8")
					.entity(json.toJSONString()).build();
		}

		return Response.status(200)
				.header("Content-Type", "application/json; charset=utf-8")
				.entity("{}").build();
	}

	@Path("index/showpost.html")
	@GET
	public Response showPost() {
		try {
			String msg = request.getParameter("msg");

			PebbleHelper pebbleHelper = PebbleHelper.getPebbleHelper(
					"web/blog.html", request, NavbarElements.NoNavbar);
			pebbleHelper.getContextMap().put("hideprofile", true);
			pebbleHelper.getContextMap().put("blogenabled", true);

			if (StringUtils.isEmpty(msg)) {
				return Response.ok(pebbleHelper.evaluate(),
						"text/html; charset=utf-8").build();
			}

			if (msg != null) {
				pebbleHelper.getContextMap().put("msg", msg);
			}

			BlogEntry blogEntryOpt = BlogUtils.getBlogEntryOpt(Base58
					.decode(msg));

			if (blogEntryOpt == null) {
				// TODO SHOW NOT FOUND MESSAGE
				return Response.ok(pebbleHelper.evaluate(),
						"text/html; charset=utf-8").build();
			}
			Profile activeProfileOpt = ProfileHelper.getInstance()
					.getActiveProfileOpt(request);

			String signature = blogEntryOpt.getSignature();

			addSharingAndLiking(blogEntryOpt, signature);
			if (activeProfileOpt != null) {
				blogEntryOpt.setLiking(activeProfileOpt.getLikedPosts()
						.contains(signature));
			}

			pebbleHelper.getContextMap().put("blogposts",
					Arrays.asList(blogEntryOpt));

			return Response.ok(pebbleHelper.evaluate(),
					"text/html; charset=utf-8").build();

		} catch (Throwable e) {
			e.printStackTrace();
			return error404(request, null);
		}
	}

	@Path("index/mergedblog.html")
	@GET
	public Response mergedBlog() {

		try {
			PebbleHelper pebbleHelper = PebbleHelper.getPebbleHelper(
					"web/blog.html", request, NavbarElements.BlogNavbar);

			String blogname = request
					.getParameter(BlogPostResource.BLOGNAME_KEY);

			String msg = request.getParameter("msg");

			if (msg != null) {
				pebbleHelper.getContextMap().put("msg", msg);
			}

			Profile profile = null;
			if (blogname == null) {
				Profile activeProfileOpt = ProfileHelper.getInstance()
						.getActiveProfileOpt(request);

				profile = activeProfileOpt;
			} else {
				profile = Profile.getProfileOpt(blogname);
			}

			if (profile == null || !profile.isProfileEnabled()) {

				pebbleHelper = PebbleHelper.getPebbleHelper(
						"web/profiledisabled.html", request);
				return Response.ok(pebbleHelper.evaluate(),
						"text/html; charset=utf-8").build();
			}

			pebbleHelper.getContextMap().put("postblogurl",
					"postblog.html?blogname=" + blogname);

			pebbleHelper.getContextMap().put("blogprofile", profile);
			pebbleHelper.getContextMap().put("blogenabled", true);
			pebbleHelper.getContextMap().put("hideprofile", true);

			List<String> followedBlogs = new ArrayList<String>(
					profile.getFollowedBlogs());
			followedBlogs.add(profile.getName().getName());

			List<BlogEntry> blogPosts = BlogUtils.getBlogPosts(followedBlogs);

			Profile activeProfileOpt = ProfileHelper.getInstance()
					.getActiveProfileOpt(request);
			for (BlogEntry blogEntry : blogPosts) {
				String signature = blogEntry.getSignature();

				addSharingAndLiking(blogEntry, signature);
				if (activeProfileOpt != null) {
					blogEntry.setLiking(activeProfileOpt.getLikedPosts()
							.contains(signature));
				}
			}

			pebbleHelper.getContextMap().put("blogposts", blogPosts);

			return Response.ok(pebbleHelper.evaluate(),
					"text/html; charset=utf-8").build();

		} catch (Throwable e) {
			e.printStackTrace();
			return error404(request, null);
		}

	}

	public void addSharingAndLiking(BlogEntry blogEntry, String signature) {
		List<String> list = DBSet.getInstance().getSharedPostsMap()
				.get(Base58.decode(blogEntry.getSignature()));
		if (list != null) {
			for (String name : list) {
				blogEntry.addSharedUser(name);
			}
		}

		NameStorageMap nameStorageMap = DBSet.getInstance().getNameStorageMap();
		Set<String> keys = nameStorageMap.getKeys();

		for (String name : keys) {
			Profile profileOpt = Profile.getProfileOpt(name);
			if (profileOpt != null) {
				if (profileOpt.getLikedPosts().contains(signature)) {
					blogEntry.addLikingUser(profileOpt.getName().getName());
				}

			}
		}
	}

	@Path("index/hashtag.html")
	@GET
	public Response getHashTagPosts() {
		try {
			String hashtag = request.getParameter("hashtag");
			String msg = request.getParameter("msg");

			PebbleHelper pebbleHelper = PebbleHelper.getPebbleHelper(
					"web/blog.html", request, NavbarElements.Searchnavbar);
			pebbleHelper.getContextMap().put("hideprofile", true);
			pebbleHelper.getContextMap().put("blogenabled", true);
			hashtag = hashtag == null ? "" : hashtag;

			if (StringUtils.isEmpty(hashtag)) {
				return Response.ok(pebbleHelper.evaluate(),
						"text/html; charset=utf-8").build();
			}
			hashtag = hashtag.toLowerCase();

			hashtag = "#" + hashtag;

			if (msg != null) {
				pebbleHelper.getContextMap().put("msg", msg);
			}

			List<BlogEntry> blogPosts = BlogUtils.getHashTagPosts(hashtag);

			Profile activeProfileOpt = ProfileHelper.getInstance()
					.getActiveProfileOpt(request);

			for (BlogEntry blogEntry : blogPosts) {
				String signature = blogEntry.getSignature();

				addSharingAndLiking(blogEntry, signature);
				if (activeProfileOpt != null) {
					blogEntry.setLiking(activeProfileOpt.getLikedPosts()
							.contains(signature));
				}
			}

			pebbleHelper.getContextMap().put("blogposts", blogPosts);

			return Response.ok(pebbleHelper.evaluate(),
					"text/html; charset=utf-8").build();

		} catch (Throwable e) {
			e.printStackTrace();
			return error404(request, null);
		}
	}

	@Path("index/blog.html")
	@GET
	public Response getBlog(@PathParam("messageOpt") String messageOpt) {

		try {
			String blogname = request
					.getParameter(BlogPostResource.BLOGNAME_KEY);
			String switchprofile = request.getParameter("switchprofile");
			String disconnect = request.getParameter("disconnect");
			String msg = request.getParameter("msg");

			if (StringUtils.isNotBlank(disconnect)) {
				ProfileHelper.getInstance().disconnect();
			} else {
				ProfileHelper.getInstance().switchProfileOpt(switchprofile);
			}

			PebbleHelper pebbleHelper = PebbleHelper.getPebbleHelper(
					"web/blog.html", request, NavbarElements.BlogNavbar);
			pebbleHelper.getContextMap().put("namestoragemap",
					NameStorageWebResource.getInstance());
			pebbleHelper.getContextMap().put("postblogurl", "postblog.html");
			pebbleHelper.getContextMap().put("apimessage", messageOpt);

			if (msg != null) {
				pebbleHelper.getContextMap().put("msg", msg);
			}

			NameMap nameMap = DBSet.getInstance().getNameMap();
			if (blogname != null) {
				if (!nameMap.contains(blogname)) {
					return Response.ok(
							PebbleHelper.getPebbleHelper(
									"web/profiledisabled.html", request)
									.evaluate(), "text/html; charset=utf-8")
							.build();
				}

				Name name = nameMap.get(blogname);
				Profile profile = Profile.getProfileOpt(name);

				if (profile == null || !profile.isProfileEnabled()) {
					pebbleHelper = PebbleHelper.getPebbleHelper(
							"web/profiledisabled.html", request);
					if (Controller.getInstance().getAccountByAddress(
							name.getOwner().getAddress()) != null) {
						pebbleHelper.getContextMap().put("ownProfileName",
								blogname);
					}
					return Response.ok(pebbleHelper.evaluate(),
							"text/html; charset=utf-8").build();
				}

				pebbleHelper.getContextMap().put("postblogurl",
						"postblog.html?blogname=" + blogname);

				pebbleHelper.getContextMap().put("blogprofile", profile);
				pebbleHelper.getContextMap().put("blogenabled",
						profile.isBlogEnabled());
				if (Controller.getInstance().doesWalletDatabaseExists()) {
					if (Controller.getInstance().getAccountByAddress(
							name.getOwner().getAddress()) != null) {
						pebbleHelper.getContextMap().put("ownProfileName",
								blogname);
					}
				}
				pebbleHelper.getContextMap().put("follower",
						profile.getFollower());

			} else {
				pebbleHelper.getContextMap().put("hideprofile", true);
				pebbleHelper.getContextMap().put("blogenabled", true);
			}

			Profile activeProfileOpt = ProfileHelper.getInstance()
					.getActiveProfileOpt(request);
			pebbleHelper.getContextMap().put(
					"isFollowing",
					activeProfileOpt != null
							&& activeProfileOpt.getFollowedBlogs().contains(
									blogname));

			pebbleHelper.getContextMap().put(
					"isLikeing",
					activeProfileOpt != null
							&& activeProfileOpt.getLikedPosts().contains(
									blogname));

			List<BlogEntry> blogPosts = BlogUtils.getBlogPosts(blogname);

			for (BlogEntry blogEntry : blogPosts) {
				String signature = blogEntry.getSignature();

				addSharingAndLiking(blogEntry, signature);
				if (activeProfileOpt != null) {
					blogEntry.setLiking(activeProfileOpt.getLikedPosts()
							.contains(signature));
				}
			}

			pebbleHelper.getContextMap().put("blogposts", blogPosts);

			return Response.ok(pebbleHelper.evaluate(),
					"text/html; charset=utf-8").build();
		} catch (Throwable e) {
			e.printStackTrace();
			return error404(request, null);
		}
	}

	@Path("index/libs/js/Base58.js")
	@GET
	public Response Base58js() {
		File file = new File("web/libs/js/Base58.js");

		if (file.exists()) {
			return Response.ok(file, "text/javascript").build();
		} else {
			return error404(request, null);
		}
	}

	@Path("index/libs/js/common.js")
	@GET
	public Response commonjs() {
		File file = new File("web/libs/js/common.js");

		if (file.exists()) {
			return Response.ok(file, "text/javascript").build();
		} else {
			return error404(request, null);
		}
	}

	@Path("/index/libs/third-party/jquery.form.min.js")
	@GET
	public Response getFormMin() {
		File file = new File("web/libs/js/third-party/jquery.form.min.js");

		if (file.exists()) {
			return Response.ok(file, "text/javascript").build();
		} else {
			return error404(request, null);
		}
	}

	@Path("index/libs/jquery/jquery.{version}.js")
	@GET
	public Response jquery(@PathParam("version") String version) {
		File file;
		if (version.equals("1")) {
			file = new File("web/libs/jquery/jquery-1.11.3.min.js");
		} else if (version.equals("2")) {
			file = new File("web/libs/jquery/jquery-2.1.4.min.js");
		} else {
			file = new File("web/libs/jquery/jquery-2.1.4.min.js");
		}

		if (file.exists()) {
			return Response.ok(file, "text/javascript; charset=utf-8").build();
		} else {
			return error404(request, null);
		}
	}

	@Path("index/libs/angular/angular.{version}.js")
	@GET
	public Response angular(@PathParam("version") String version) {
		File file;
		if (version.equals("1.3")) {
			file = new File("web/libs/angular/angular.min.1.3.15.js");
		} else if (version.equals("1.4")) {
			file = new File("web/libs/angular/angular.min.1.4.0.js");
		} else {
			file = new File("web/libs/angular/angular.min.1.3.15.js");
		}

		if (file.exists()) {
			return Response.ok(file, "text/javascript; charset=utf-8").build();
		} else {
			return error404(request, null);
		}
	}

	@Path("index/libs/bootstrap/{version}/{folder}/{filename}")
	@GET
	public Response bootstrap(@PathParam("version") String version,
			@PathParam("folder") String folder,
			@PathParam("filename") String filename) {
		String fullname = "web/libs/bootstrap-3.3.4-dist/";
		String type = "text/html; charset=utf-8";

		switch (folder) {
		case "css": {
			fullname += "css/";
			type = "text/css";
			switch (filename) {
			case "bootstrap.css":

				fullname += "bootstrap.css";
				break;

			case "theme.css":

				fullname += "theme.css";
				break;

			case "bootstrap.css.map":

				fullname += "bootstrap.css.map";
				break;

			case "bootstrap.min.css":

				fullname += "bootstrap.min.css";
				break;

			case "bootstrap-theme.css":

				fullname += "bootstrap-theme.css";
				break;

			case "bootstrap-theme.css.map":

				fullname += "bootstrap-theme.css.mapp";
				break;

			case "bootstrap-theme.min.css":

				fullname += "bootstrap-theme.min.css";
				break;
			}
			break;
		}
		case "fonts": {
			fullname += "fonts/";
			switch (filename) {
			case "glyphicons-halflings-regular.eot":

				fullname += "glyphicons-halflings-regular.eot";
				type = "application/vnd.ms-fontobject";
				break;

			case "glyphicons-halflings-regular.svg":

				fullname += "glyphicons-halflings-regular.svg";
				type = "image/svg+xml";
				break;

			case "glyphicons-halflings-regular.ttf":

				fullname += "glyphicons-halflings-regular.ttf";
				type = "application/x-font-ttf";
				break;

			case "glyphicons-halflings-regular.woff":

				fullname += "glyphicons-halflings-regular.woff";
				type = "application/font-woff";
				break;

			case "glyphicons-halflings-regular.woff2":

				fullname += "glyphicons-halflings-regular.woff2";
				type = "application/font-woff";
				break;
			}
			break;
		}
		case "js": {
			fullname += "js/";
			type = "text/javascript";
			switch (filename) {
			case "bootstrap.js":

				fullname += "bootstrap.js";
				break;

			case "bootstrap.min.js":

				fullname += "bootstrap.js";
				break;

			case "npm.js":

				fullname += "npm.js";
				break;

			}
			break;
		}
		}

		File file = new File(fullname);

		if (file.exists()) {
			return Response.ok(file, type).build();
		} else {
			return error404(request, null);
		}
	}

	public Response error404(HttpServletRequest request, String titleOpt) {

		try {
			PebbleHelper pebbleHelper = PebbleHelper.getPebbleHelper(
					"web/404.html", request);

			pebbleHelper.getContextMap().put(
					"title",
					titleOpt == null ? "Sorry, that page does not exist!"
							: titleOpt);

			return Response.status(404)
					.header("Content-Type", "text/html; charset=utf-8")
					.entity(pebbleHelper.evaluate()).build();
		} catch (PebbleException e) {
			e.printStackTrace();
			return Response.status(404).build();
		}
	}

	public static String readFile(String path, Charset encoding)
			throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}

	@SuppressWarnings("unchecked")
	@Path("namestorage:{name}")
	@GET
	public Response showNamestorage(@PathParam("name") String name) {

		try {
			PebbleHelper pebbleHelper = PebbleHelper.getPebbleHelper(
					"web/main.mini.html", request);

			NameStorageMap nameStorageMap = DBSet.getInstance()
					.getNameStorageMap();
			Map<String, String> map = nameStorageMap.get(name);

			if (map != null) {
				Set<String> keySet = map.keySet();
				JSONObject resultJson = new JSONObject();
				for (String key : keySet) {
					String value = map.get(key);
					resultJson.put(key, value);
				}

				pebbleHelper.getContextMap().put("keyvaluepairs", resultJson);
				pebbleHelper.getContextMap().put("dataname", name);

				return Response.status(200)
						.header("Content-Type", "text/html; charset=utf-8")
						.entity(pebbleHelper.evaluate()).build();

			} else {
				return error404(request,
						"This namestorage does not contain any entries");
			}

		} catch (Throwable e) {
			e.printStackTrace();
			return error404(request, null);
		}

	}

	public String miniIndex() {
		try {
			return readFile("web/main.mini.html", StandardCharsets.UTF_8);

		} catch (IOException e) {
			e.printStackTrace();
			return "ERROR";
		}
	}

	public String jsonToFineSting(String str) {
		Writer writer = new JSonWriter();
		Object jsonResult = JSONValue.parse(str);

		try {
			if (jsonResult instanceof JSONArray) {
				((JSONArray) jsonResult).writeJSONString(writer);
				return writer.toString();
			}
			if (jsonResult instanceof JSONObject) {
				((JSONObject) jsonResult).writeJSONString(writer);
				return writer.toString();
			}
			writer.close();
			return "";
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	}

	@Path("/index/{html}")
	@GET
	public Response getHtml(@PathParam("html") String html) {
		return error404(request, null);
	}

	@Path("{name}/{key}")
	@GET
	public Response getKeyAsWebsite(@PathParam("name") String nameName,
			@PathParam("key") String key) {
		Name name = Controller.getInstance().getName(nameName);

		try {

			// CHECK IF NAME EXISTS
			if (name == null) {
				return error404(request, "This name does not exist!");
			}

			String website = DBSet.getInstance().getNameStorageMap()
					.getOpt(nameName, key);

			if (website == null) {
				try {
					return error404(request, "This key is empty");
				} catch (Throwable e) {
					e.printStackTrace();
					return error404(request, null);
				}

			}

			return enhanceAndShowWebsite(website);
		} catch (Throwable e) {
			e.printStackTrace();
			return error404(request, null);
		}
	}

	@Path("{name}")
	@GET
	public Response getNames(@PathParam("name") String nameName) {
		Name name = Controller.getInstance().getName(nameName);

		try {

			// CHECK IF NAME EXISTS
			if (name == null) {
				return error404(request, null);
			}

			String website = DBSet.getInstance().getNameStorageMap()
					.getOpt(nameName, Qorakeys.WEBSITE.toString());

			if (website == null) {
				try {
					return error404(
							request,
							"This name has currently no <a href=\"/index/namestorage.html\">website<a/>!");
				} catch (Throwable e) {
					e.printStackTrace();
					return error404(request, null);
				}

			}

			return enhanceAndShowWebsite(website);
		} catch (Throwable e) {
			e.printStackTrace();
			return error404(request, null);
		}
	}

	private Response enhanceAndShowWebsite(String website) throws IOException,
			PebbleException {
		website = injectValues(website);

		File tmpFile = File.createTempFile("web", ".site");
		FileUtils.writeStringToFile(tmpFile, website);
		PebbleHelper pebbleHelper = PebbleHelper.getPebbleHelper(
				tmpFile.getAbsolutePath(), request);
		pebbleHelper.getContextMap().put("namestoragemap",
				NameStorageWebResource.getInstance());
		// pebbleHelper.getContextMap().put("atmap",DBSet.getInstance().getATMap());
		// pebbleHelper.getContextMap().put("attxsmap",DBSet.getInstance().getATTransactionMap());
		pebbleHelper.getContextMap().put("ats", ATWebResource.getInstance());
		pebbleHelper.getContextMap().put("controller",
				ControllerWebResource.getInstance());
		pebbleHelper.getContextMap().put("request", this.request);
		tmpFile.delete();

		// SHOW WEB-PAGE
		String evaluate = pebbleHelper.evaluate();

		String pictureRegex = "data.([a-zA-Z]+).([a-zA-Z]+);base64, (.+)";
		if (!evaluate.isEmpty()) {
			if (evaluate.matches(pictureRegex)) {

				String type = evaluate.replaceAll(pictureRegex, "$1");
				String subtype = evaluate.replaceAll(pictureRegex, "$2");
				byte[] dataOfImage = Base64.decode(evaluate.replaceAll(
						pictureRegex, "$3"));
				Response build = Response
						.ok(dataOfImage,
								type + "/" + subtype + "; charset=utf-8")
						.header("X-XSS-Protection", "0").build();
				return build;
			}
		}

		Response build = Response.ok(evaluate, "text/html; charset=utf-8")
				.header("X-XSS-Protection", "0").build();
		return build;
	}

	public static String injectValues(String value) {
		// PROCESSING TAG INJ
		Pattern pattern = Pattern.compile("(?i)(<inj.*>(.*?)</inj>)");
		Matcher matcher = pattern.matcher(value);
		while (matcher.find()) {
			Document doc = Jsoup.parse(matcher.group(1));
			Elements inj = doc.select("inj");
			Element element = inj.get(0);

			NameStorageMap nameMap = DBSet.getInstance().getNameStorageMap();
			String name = matcher.group(2);
			String result = "";
			if (nameMap.contains(name)) {

				if (element.hasAttr("key")) {
					String key = element.attr("key");
					String opt = nameMap.getOpt(name, key);
					result = opt != null ? opt : "";

				}
			}
			value = value.replace(matcher.group(), result);

		}
		return value;
	}
}
