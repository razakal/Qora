package webserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
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
import java.util.HashMap;
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
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.util.StringUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import qora.account.Account;
import qora.block.Block;
import qora.blockexplorer.BlockExplorer;
import qora.crypto.Base58;
import qora.crypto.Crypto;
import qora.naming.Name;
import qora.transaction.ArbitraryTransaction;
import qora.transaction.Transaction;
import qora.web.BlogBlackWhiteList;
import qora.web.BlogProfile;
import qora.web.HTMLSearchResult;
import qora.web.NameStorageMap;
import qora.web.Profile;
import qora.web.ProfileHelper;
import qora.web.ServletUtils;
import qora.web.blog.BlogEntry;
import settings.Settings;
import utils.AccountBalanceComparator;
import utils.BlogUtils;
import utils.GZIP;
import utils.JSonWriter;
import utils.NameUtils;
import utils.NameUtils.NameResult;
import utils.Pair;
import utils.PebbleHelper;
import utils.Qorakeys;
import utils.StorageUtils;
import utils.StrJSonFine;
import api.ATResource;
import api.AddressesResource;
import api.ApiErrorFactory;
import api.BlocksResource;
import api.BlogPostResource;
import api.NameSalesResource;
import api.NameStorageResource;
import api.NamesResource;
import api.TransactionsResource;

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
					+ name, "/" + name, "/namepairs:" + name, null));

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

	@Path("index/websitecreation.html")
	@GET
	public Response doWebsiteCreation() {

		String name = request.getParameter("name");
		try {
			PebbleHelper pebbleHelper = PebbleHelper.getPebbleHelper(
					"web/websitecreation.html", request);

			List<Name> namesAsList = new CopyOnWriteArrayList<Name>(Controller
					.getInstance().getNamesAsList());

			pebbleHelper.getContextMap().put("names", namesAsList);

			Name nameobj = null;
			if (name != null) {
				nameobj = Controller.getInstance().getName(name);
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

				String websiteOpt = DBSet.getInstance().getNameStorageMap()
						.getOpt(nameobj.getName(), Qorakeys.WEBSITE.toString());

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

	@SuppressWarnings("unchecked")
	@POST
	@Path("index/websitesave.html")
	@Consumes("application/x-www-form-urlencoded")
	public Response saveWebsite(@Context HttpServletRequest request,
			MultivaluedMap<String, String> form) {

		String name = form.getFirst("name");
		String website = form.getFirst("website");

		JSONObject json = new JSONObject();

		if (StringUtils.isBlank(name)) {
			json.put("type", "parametersMissing");

			return Response.status(200)
					.header("Content-Type", "application/json; charset=utf-8")
					.entity(json.toJSONString()).build();
		}
		Pair<String, String> websitepair = new Pair<String, String>(
				Qorakeys.WEBSITE.toString(), website);
		JSONObject storageJsonObject = StorageUtils.getStorageJsonObject(
				Collections.singletonList(websitepair), null, null, null, null);

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
					"/index/blog.html?blogname=" + name, "/namepairs:" + name,
					blogProfile.getFollower()));
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

	@SuppressWarnings("unchecked")
	@Path("index/API.html")
	@GET
	public Response handleAPICall() {

		try {
			PebbleHelper pebbleHelper = PebbleHelper.getPebbleHelper(
					"web/apianswer.html", request);
			// EXAMPLE POST/GET/DELETE
			String type = request.getParameter("type");
			// EXAMPLE /names/key/MyName
			String url = request.getParameter("apiurl");
			String okmsg = request.getParameter("okmsg");
			String errormsg = request.getParameter("errormsg");

			if (StringUtils.isBlank(type)
					|| (!type.equalsIgnoreCase("get")
							&& !type.equalsIgnoreCase("post") && !type
								.equalsIgnoreCase("delete"))) {

				pebbleHelper.getContextMap().put("title",
						"An Api error occured");
				pebbleHelper
						.getContextMap()
						.put("apicall",
								"You need a type parameter with value GET/POST or DELETE ");

				return Response.ok(pebbleHelper.evaluate(),
						"text/html; charset=utf-8").build();
			}

			if (StringUtils.isBlank(url)) {
				pebbleHelper.getContextMap().put("title",
						"An Api error occured");
				pebbleHelper.getContextMap().put("apicall",
						"You need to provide an apiurl parameter");
				return Response.ok(pebbleHelper.evaluate(),
						"text/html; charset=utf-8").build();
			}
			url = url.startsWith("/") ? url.substring(1) : url;

			Map<String, String[]> parameterMap = new HashMap<String, String[]>(
					request.getParameterMap());

			parameterMap.remove("type");
			parameterMap.remove("apiurl");
			parameterMap.remove("okmsg");
			parameterMap.remove("errormsg");

			Set<String> keySet = parameterMap.keySet();

			JSONObject json = new JSONObject();

			for (String key : keySet) {
				String[] value = parameterMap.get(key);
				json.put(key, value[0]);
			}

			try {
				// CREATE CONNECTION
				URL urlToCall = new URL("http://127.0.0.1:"
						+ Settings.getInstance().getRpcPort() + "/" + url);
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

				InputStreamReader isReader = new InputStreamReader(stream,
						"UTF-8");
				BufferedReader br = new BufferedReader(isReader);
				String result = br.readLine();

				if (result.contains("message") && result.contains("error")) {
					if (StringUtils.isNotBlank(errormsg)) {

						pebbleHelper.getContextMap().put("customtext",
								"<font color=red>" + errormsg + "</font>");
					}
					pebbleHelper.getContextMap().put("title",
							"An Api error occured");
					pebbleHelper.getContextMap().put(
							"apicall",
							"apicall: "
									+ type.toUpperCase()
									+ " "
									+ url
									+ (json.size() > 0 ? json.toJSONString()
											: ""));
					pebbleHelper.getContextMap().put("errormessage",
							"Result:" + result);
					return Response.ok(pebbleHelper.evaluate(),
							"text/html; charset=utf-8").build();
				} else {
					if (StringUtils.isNotBlank(okmsg)) {
						pebbleHelper.getContextMap().put("customtext",
								"<font color=green>" + okmsg + "</font>");
					}
					pebbleHelper.getContextMap().put("title",
							"The API Call was successful");
					pebbleHelper.getContextMap().put(
							"apicall",
							"Submitted Api call: "
									+ type.toUpperCase()
									+ " "
									+ url
									+ (json.size() > 0 ? json.toJSONString()
											: ""));
					pebbleHelper.getContextMap().put("errormessage",
							"Result:" + result);
					return Response.ok(pebbleHelper.evaluate(),
							"text/html; charset=utf-8").build();
				}

			} catch (IOException e) {
				e.printStackTrace();
				String additionalHelp = "";
				if (e instanceof FileNotFoundException) {
					additionalHelp = "The apicall with the following apiurl is not existing: ";
				}
				pebbleHelper.getContextMap().put("title",
						"An Api error occured");
				pebbleHelper.getContextMap().put(
						"apicall",
						"You tried to submit the following apicall: "
								+ type.toUpperCase() + " " + url
								+ (json.size() > 0 ? json.toJSONString() : ""));
				pebbleHelper.getContextMap().put("errormessage",
						additionalHelp + e.getMessage());
				return Response.ok(pebbleHelper.evaluate(),
						"text/html; charset=utf-8").build();
			}

		} catch (Throwable e) {
			e.printStackTrace();
		}

		return error404(request, null);

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
						new Date().getTime(), creator, "");

				json.put("previewBlogpost", entry.toJson());

				return Response
						.status(200)
						.header("Content-Type",
								"application/json; charset=utf-8")
						.entity(json.toJSONString()).build();
			}

			try {

				jsonBlogPost.put("fee", Controller.getInstance().calcRecommendedFeeForArbitraryTransaction(jsonBlogPost.toJSONString().getBytes()).getA().toPlainString());
				
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
	@Path("index/sharepost.html")
	@Consumes("application/x-www-form-urlencoded")
	public Response sharePost(@Context HttpServletRequest request,
			MultivaluedMap<String, String> form) {

		JSONObject json = new JSONObject();

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

					if (sourceBlog.equals(activeProfileOpt.getName().getName())) {
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

	@Path("index/mergedblog.html")
	@GET
	public Response mergedBlog() {

		try {
			PebbleHelper pebbleHelper = PebbleHelper.getPebbleHelper(
					"web/blog.html", request);

			String blogname = request
					.getParameter(BlogPostResource.BLOGNAME_KEY);

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

			List<String> followedBlogs = new ArrayList<String>(
					profile.getFollowedBlogs());
			followedBlogs.add(profile.getName().getName());

			List<BlogEntry> blogPosts = BlogUtils.getBlogPosts(followedBlogs);

			Profile activeProfileOpt = ProfileHelper.getInstance()
					.getActiveProfileOpt(request);
			List<Profile> enabledProfiles = Profile.getEnabledProfiles();
			for (BlogEntry blogEntry : blogPosts) {
				String signature = blogEntry.getSignature();

				addSharingAndLiking(enabledProfiles, blogEntry, signature);
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

	public void addSharingAndLiking(List<Profile> enabledProfiles,
			BlogEntry blogEntry, String signature) {
		List<String> list = DBSet.getInstance().getSharedPostsMap()
				.get(Base58.decode(blogEntry.getSignature()));
		if (list != null) {
			for (String name : list) {
				blogEntry.addSharedUser(name);
			}
		}

		for (Profile enabledProfile : enabledProfiles) {
			if (enabledProfile.getLikedPosts().contains(signature)) {
				blogEntry.addLikingUser(enabledProfile.getName().getName());
			}

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

			if (StringUtils.isNotBlank(disconnect)) {
				ProfileHelper.getInstance().disconnect();
			} else {
				ProfileHelper.getInstance().switchProfileOpt(switchprofile);
			}

			PebbleHelper pebbleHelper = PebbleHelper.getPebbleHelper(
					"web/blog.html", request);
			pebbleHelper.getContextMap().put("postblogurl", "postblog.html");
			pebbleHelper.getContextMap().put("apimessage", messageOpt);

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
				pebbleHelper.getContextMap().put("likes", profile.getLikes());

			} else {
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

			List<Profile> enabledProfiles = Profile.getEnabledProfiles();

			for (BlogEntry blogEntry : blogPosts) {
				String signature = blogEntry.getSignature();

				addSharingAndLiking(enabledProfiles, blogEntry, signature);
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
	@Path("namepairs:{name}")
	@GET
	public Response showNamepairs(@PathParam("name") String name) {

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

	@Path("block:{block}")
	@GET
	public Response getBlock(@PathParam("block") String strBlock) {
		String str;
		try {
			if (strBlock.matches("\\d+")) {
				str = BlocksResource.getbyHeight(Integer.valueOf(strBlock));
			} else if (strBlock.equals("last")) {
				str = BlocksResource.getLastBlock();
			} else {
				str = BlocksResource.getBlock(strBlock);
			}

			str = jsonToFineSting(str);

		} catch (Exception e1) {
			str = "<h2>Block does not exist!</h2";
		}

		return Response
				.status(200)
				.header("Content-Type", "text/html; charset=utf-8")
				.entity(miniIndex()
						.replace(
								"<jsonresults></jsonresults>",
								"<br><div ng-app=\"myApp\" ng-controller=\"AppController\"><div class=\"panel panel-default\">" //
										+ "<div class=\"panel-heading\">Block : "
										+ strBlock
										+ "</div><table class=\"table\">" //
										+ "<tr ng-repeat=\"(key,value) in steps\"><td>{{ key }}</td><td>{{ value }}</td></tr></table></div></div>")
						.replace("$scope.steps = {}", "$scope.steps = " + str))
				.build();
	}

	@SuppressWarnings("unchecked")
	@Path("tx:{tx}")
	@GET
	public Response getTx(@PathParam("tx") String strTx) {

		String str = null;
		try {

			if (Crypto.getInstance().isValidAddress(strTx)) {
				Account account = new Account(strTx);

				Pair<Block, List<Transaction>> result = Controller
						.getInstance().scanTransactions(null, -1, -1, -1, -1,
								account);

				JSONObject json = new JSONObject();
				JSONArray transactions = new JSONArray();
				for (Transaction transaction : result.getB()) {
					transactions.add(transaction.toJson());
				}
				json.put(strTx, transactions);
				str = json.toJSONString();
			} else {
				str = TransactionsResource.getTransactionsBySignature(strTx);
			}
			str = jsonToFineSting(str);
		} catch (Exception e1) {
			str = "<h2>Transaction does not exist!</h2>";
		}

		return Response
				.status(200)
				.header("Content-Type", "text/html; charset=utf-8")
				.entity(miniIndex()
						.replace(
								"<jsonresults></jsonresults>",
								"<br><div ng-app=\"myApp\" ng-controller=\"AppController\"><div class=\"panel panel-default\">" //
										+ "<div class=\"panel-heading\">Transaction : "
										+ strTx
										+ "</div><table class=\"table\">" //
										+ "<tr ng-repeat=\"(key,value) in steps\"><td>{{ key }}</td><td>{{ value }}</td></tr></table></div></div>")
						.replace("$scope.steps = {}", "$scope.steps = " + str))
				.build();
	}

	@Path("balance:{address}")
	@GET
	public Response getBalance(@PathParam("address") String address) {
		String str = null;
		try {

			String addressreal = "";

			if (!Crypto.getInstance().isValidAddress(address)) {
				Pair<Account, NameResult> nameToAdress = NameUtils
						.nameToAdress(address);

				if (nameToAdress.getB() == NameResult.OK) {
					addressreal = nameToAdress.getA().getAddress();
				} else {
					throw ApiErrorFactory.getInstance().createError(
							nameToAdress.getB().getErrorCode());
				}
			} else {
				addressreal = address;
			}

			str = AddressesResource.getGeneratingBalance(addressreal);

		} catch (Exception e1) {
			str = "<h2>Address does not exist!</h2>";
		}

		return Response
				.status(200)
				.header("Content-Type", "text/html; charset=utf-8")
				.entity(miniIndex()
						.replace(
								"<jsonresults></jsonresults>",
								"<br><div ng-app=\"myApp\" ng-controller=\"AppController\"><div class=\"panel panel-default\">" //
										+ "<div class=\"panel-heading\">Balance of "
										+ address
										+ "</div><table class=\"table\">" //
										+ "<tr ng-repeat=\"(key,value) in steps\"><td>{{ key }}</td><td>{{ value }}</td></tr></table></div></div>")
						.replace("$scope.steps = {}",
								"$scope.steps = {\"amount\":" + str + "}"))
				.build();
	}

	@Path("balance:{address}:{confirmations}")
	@GET
	public Response getBalance(@PathParam("address") String address,
			@PathParam("confirmations") int confirmations) {

		String str = null;
		try {

			str = AddressesResource
					.getGeneratingBalance(address, confirmations);

		} catch (Exception e1) {
			str = "<h2>Address does not exist!</h2>";
		}

		return Response
				.status(200)
				.header("Content-Type", "text/html; charset=utf-8")
				.entity(miniIndex()
						.replace(
								"<jsonresults></jsonresults>",
								"<br><div ng-app=\"myApp\" ng-controller=\"AppController\"><div class=\"panel panel-default\">" //
										+ "<div class=\"panel-heading\">Balance of "
										+ address
										+ ":"
										+ confirmations
										+ "</div><table class=\"table\">" //
										+ "<tr ng-repeat=\"(key,value) in steps\"><td>{{ key }}</td><td>{{ value }}</td></tr></table></div></div>")
						.replace("$scope.steps = {}",
								"$scope.steps = {\"amount\":" + str + "}"))
				.build();
	}

	@Path("name:{name}")
	@GET
	public Response getName(@PathParam("name") String strName) {

		String str = null;
		String strNameSale = null;
		try {
			str = NamesResource.getName(strName);

			if (DBSet.getInstance().getNameExchangeMap().contains(strName)) {
				strNameSale = NameSalesResource.getNameSale(strName);
			}

			str = jsonToFineSting(str);

			if (strNameSale != null) {
				str += "\n\n" + jsonToFineSting(strNameSale);
			}

		} catch (Exception e1) {
			str = "<h2>Name does not exist!</h2>";
		}

		return Response
				.status(200)
				.header("Content-Type", "text/html; charset=utf-8")
				.entity(miniIndex()
						.replace(
								"<jsonresults></jsonresults>",
								"<br><div ng-app=\"myApp\" ng-controller=\"AppController\"><div class=\"panel panel-default\">" //
										+ "<div class=\"panel-heading\">Name : "
										+ strName
										+ "</div><table class=\"table\">" //
										+ "<tr ng-repeat=\"(key,value) in steps\"><td>{{ key }}</td><td>{{ value }}</td></tr></table></div></div>")
						.replace("$scope.steps = {}", "$scope.steps = " + str))
				.build();
	}

	@Path("at:{at}")
	@GET
	public Response getAt(@PathParam("at") String strAt) {

		String str = null;
		try {
			str = ATResource.getAT(strAt);
			str = jsonToFineSting(str);
		} catch (Exception e1) {
			str = "<h2>AT does not exist!</h2>";
		}

		return Response
				.status(200)
				.header("Content-Type", "text/html; charset=utf-8")
				.entity(miniIndex()
						.replace(
								"<jsonresults></jsonresults>",
								"<br><div ng-app=\"myApp\" ng-controller=\"AppController\"><div class=\"panel panel-default\">" //
										+ "<div class=\"panel-heading\">AT : "
										+ strAt
										+ "</div><table class=\"table\">" //
										+ "<tr ng-repeat=\"(key,value) in steps\"><td>{{ key }}</td><td>{{ value }}</td></tr></table></div></div>")
						.replace("$scope.steps = {}", "$scope.steps = " + str))
				.build();
	}

	@Path("atbysender:{atbysender}")
	@GET
	public Response getAtTx(@PathParam("atbysender") String strAtbySender) {

		String str = null;
		try {
			str = ATResource.getATTransactionsBySender(strAtbySender);
			str = jsonToFineSting(str);
		} catch (Exception e1) {
			str = "<h2>AT does not exist!</h2>";
		}

		return Response
				.status(200)
				.header("Content-Type", "text/html; charset=utf-8")
				.entity(miniIndex()
						.replace(
								"<jsonresults></jsonresults>",
								"<br><div ng-app=\"myApp\" ng-controller=\"AppController\"><div class=\"panel panel-default\">" //
										+ "<div class=\"panel-heading\">AT Sender : "
										+ strAtbySender
										+ "</div><table class=\"table\">" //
										+ "<tr ng-repeat=\"(key,value) in steps\"><td>{{ key }}</td><td>{{ value }}</td></tr></table></div></div>")
						.replace("$scope.steps = {}", "$scope.steps = " + str))
				.build();
	}

	@Path("atbycreator:{atbycreator}")
	@GET
	public Response getAtbyCreator(
			@PathParam("atbycreator") String strAtbyCreator) {

		String str = null;
		try {
			str = ATResource.getATsByCreator(strAtbyCreator);
			str = jsonToFineSting(str);
		} catch (Exception e1) {
			str = "<h2>AT does not exist!</h2>";
		}

		return Response
				.status(200)
				.header("Content-Type", "text/html; charset=utf-8")
				.entity(miniIndex()
						.replace(
								"<jsonresults></jsonresults>",
								"<br><div ng-app=\"myApp\" ng-controller=\"AppController\"><div class=\"panel panel-default\">" //
										+ "<div class=\"panel-heading\">AT Creator : "
										+ strAtbyCreator
										+ "</div><table class=\"table\">" //
										+ "<tr ng-repeat=\"(key,value) in steps\"><td>{{ key }}</td><td>{{ value }}</td></tr></table></div></div>")
						.replace("$scope.steps = {}", "$scope.steps = " + str))
				.build();
	}

	@Path("attxbyrecipient:{attxbyrecipient}")
	@GET
	public Response getATTransactionsByRecipient(
			@PathParam("attxbyrecipient") String StrAtTxbyRecipient) {

		String str = null;
		try {
			str = ATResource.getATTransactionsByRecipient(StrAtTxbyRecipient);
			str = jsonToFineSting(str);
		} catch (Exception e1) {
			str = "<h2>AT does not exist!</h2>";
		}

		return Response
				.status(200)
				.header("Content-Type", "text/html; charset=utf-8")
				.entity(miniIndex()
						.replace(
								"<jsonresults></jsonresults>",
								"<br><div ng-app=\"myApp\" ng-controller=\"AppController\"><div class=\"panel panel-default\">" //
										+ "<div class=\"panel-heading\">AT TX by Recipient : "
										+ StrAtTxbyRecipient
										+ "</div><table class=\"table\">" //
										+ "<tr ng-repeat=\"(key,value) in steps\"><td>{{ key }}</td><td>{{ value }}</td></tr></table></div></div>")
						.replace("$scope.steps = {}", "$scope.steps = " + str))
				.build();
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

	@Path("{name}")
	@GET
	public Response getNames(@PathParam("name") String nameName) {
		Name name = Controller.getInstance().getName(nameName);

		// CHECK IF NAME EXISTS
		if (name == null) {
			return error404(request, null);
		}

		String website = DBSet.getInstance().getNameStorageMap()
				.getOpt(nameName, Qorakeys.WEBSITE.toString());

		if (website == null) {
			try {
				PebbleHelper pebbleHelper = PebbleHelper.getPebbleHelper(
						"web/websitenotfound.html", request);
				pebbleHelper.getContextMap().put("name",
						nameName.replaceAll(" ", "%20"));

				return Response.ok(pebbleHelper.evaluate(),
						"text/html; charset=utf-8").build();
			} catch (Throwable e) {
				e.printStackTrace();
				return error404(request, null);
			}

		}

		// SHOW WEB-PAGE
		return Response.status(200)
				.header("Content-Type", "text/html; charset=utf-8")
				.entity(website).build();
	}

	public static String injectValues(String value) {
		// PROCESSING TAG INJ
		Pattern pattern = Pattern.compile("(?i)(<inj.*>(.*?)</inj>)");
		Matcher matcher = pattern.matcher(value);
		while (matcher.find()) {
			Document doc = Jsoup.parse(matcher.group(1));
			Elements inj = doc.select("inj");
			Element element = inj.get(0);

			NameMap nameMap = DBSet.getInstance().getNameMap();
			String name = matcher.group(2);
			if (nameMap.contains(name)) {

				Name nameinj = nameMap.get(name);
				String result = GZIP.webDecompress(nameinj.getValue()
						.toString());
				if (element.hasAttr("key")) {
					String key = element.attr("key");
					try {

						JSONObject jsonObject = (JSONObject) JSONValue
								.parse(result);
						if (jsonObject != null) {
							// Looks like valid jSon
							if (jsonObject.containsKey(key)) {
								result = (String) jsonObject.get(key);
							}

						}

					} catch (Exception e) {
						// no json probably
					}
				}
				value = value.replace(matcher.group(), result);
			}

		}
		return value;
	}
}
