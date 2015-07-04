package webserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
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
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
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
import qora.crypto.Crypto;
import qora.naming.Name;
import qora.transaction.Transaction;
import qora.web.BlogBlackWhiteList;
import qora.web.HTMLSearchResult;
import qora.web.Profile;
import qora.web.ProfileHelper;
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
import utils.StrJSonFine;
import utils.Triplet;
import api.ATResource;
import api.AddressesResource;
import api.ApiErrorFactory;
import api.BlocksResource;
import api.BlogPostResource;
import api.NameSalesResource;
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
		return Response.status(302).header("Location", "index/index.html").build();
	}

	public Response handleDefault() {
		try {

			String searchValue = request.getParameter("search");

			PebbleHelper pebbleHelper = PebbleHelper
					.getPebbleHelper("web/index.mini.html");

			if (searchValue == null) {

				return Response.ok(
						PebbleHelper.getPebbleHelper("web/index.html")
								.evaluate(), "text/html; charset=utf-8")
						.build();
			} else {
				List<Pair<String, String>> searchResults;
				searchResults = NameUtils.getWebsitesByValue(searchValue);

				List<HTMLSearchResult> results = generateHTMLSearchresults(searchResults);

				pebbleHelper.getContextMap().put("searchresults", results);

			}

			return Response.ok(pebbleHelper.evaluate(),
					"text/html; charset=utf-8").build();
		} catch (Throwable e) {
			e.printStackTrace();
			return error404(request);
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
					+ name, "/" + name, "/namepairs:" + name));

		}
		return results;
	}

	
	@SuppressWarnings("rawtypes")
	@Path("index/blockexplorer.json")
	@GET
	public Response jsonQueryMain(@Context UriInfo info)
	{		
		Map output = BlockExplorer.getInstance().jsonQueryMain(info);
		
		return Response.status(200)
				.header("Content-Type", "application/json; charset=utf-8")
				.entity(StrJSonFine.StrJSonFine(JSONValue.toJSONString(output)))
				.build();
	}
	
	@Path("index/blockexplorer")
	@GET
	public Response blockexplorer()
	{
		return blockexplorerhtml();
	}
	
	@Path("index/blockexplorer.html")
	@GET
	public Response blockexplorerhtml()
	{
		try {
			String content = readFile("web/blockexplorer.html", StandardCharsets.UTF_8);
		
			return Response.ok(content, "text/html; charset=utf-8").build();
		} catch (IOException e) {
			e.printStackTrace();
			return error404(request);
		}
	}
	
	@Path("index/blogsearch.html")
	@GET
	public Response doBlogSearch() {

		String searchValue = request.getParameter("search");
		try {
			PebbleHelper pebbleHelper = PebbleHelper
					.getPebbleHelper("web/index.mini.html");
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
			return error404(request);
		}

	}

	@Path("index/blogdirectory.html")
	@GET
	public Response doBlogdirectory() {

		try {
			PebbleHelper pebbleHelper = PebbleHelper
					.getPebbleHelper("web/index.mini.html");

			List<HTMLSearchResult> results = handleBlogSearch(null);
			pebbleHelper.getContextMap().put("searchresults", results);
			return Response.ok(pebbleHelper.evaluate(),
					"text/html; charset=utf-8").build();
		} catch (Throwable e) {
			e.printStackTrace();
			return error404(request);
		}

	}

	@Path("index/settingssave.html")
	@GET
	public Response saveProfileSettings() {

		try {

			PebbleHelper pebbleHelper = PebbleHelper
					.getPebbleHelper("web/settings.html");

			Map<String, String[]> parameterMap = request.getParameterMap();
			String profileName = request.getParameter("profilename");

			if (!isCompleteSubmit(parameterMap)) {
				return error404(request);
			}

			Name name = null;
			name = Controller.getInstance().getName(profileName);

			if (name == null || !Profile.isAllowedProfileName(profileName)) {
				return error404(request);
			}

			boolean blogenable = Boolean.valueOf(request
					.getParameter(Qorakeys.BLOGENABLE.toString()));
			boolean profileenable = Boolean.valueOf(request
					.getParameter(Qorakeys.PROFILEENABLE.toString()));
			String titleOpt = request.getParameter(Qorakeys.BLOGTITLE
					.toString());
			titleOpt = decodeIfNotNull(titleOpt);
			String blogDescrOpt = request.getParameter(Qorakeys.BLOGDESCRIPTION
					.toString());
			blogDescrOpt = decodeIfNotNull(blogDescrOpt);
			String profileAvatarOpt = request
					.getParameter(Qorakeys.PROFILEAVATAR.toString());
			profileAvatarOpt = decodeIfNotNull(profileAvatarOpt);

			Profile profile = Profile.getProfileOpt(name);
			profile.saveAvatarTitle(profileAvatarOpt);
			profile.saveBlogDescription(blogDescrOpt);
			profile.saveBlogTitle(titleOpt);
			profile.setBlogEnabled(blogenable);
			profile.setProfileEnabled(profileenable);

			try {

				pebbleHelper.getContextMap().put(
						"result",
						"<center><div class=\"alert alert-success\" role=\"alert\">Settings saved<br>"
								+ profile.saveProfile() + "</div></center>");
			} catch (WebApplicationException e) {
				pebbleHelper
						.getContextMap()
						.put("result",
								"<center><div class=\"alert alert-danger\" role=\"alert\">Settings not saved<br>"
										+ e.getResponse().getEntity()
										+ "</div></center>");
			}
			pebbleHelper.getContextMap().put("profile", profile);
			pebbleHelper.getContextMap().put("name", name);

			List<Name> namesAsList = Controller.getInstance().getNamesAsList();
			pebbleHelper.getContextMap().put("names", namesAsList);

			return Response.ok(pebbleHelper.evaluate(),
					"text/html; charset=utf-8").build();
		} catch (Throwable e) {
			e.printStackTrace();
			return error404(request);
		}

	}

	@Path("index/settings.html")
	@GET
	public Response doProfileSettings() {

		try {

			PebbleHelper pebbleHelper = PebbleHelper
					.getPebbleHelper("web/settings.html");

			String profileName = request.getParameter("profilename");

			List<Name> namesAsList = new CopyOnWriteArrayList<Name>( Controller.getInstance().getNamesAsList());

			for (Name name : namesAsList) {
				if(!Profile.isAllowedProfileName(name.getName()))
				{
					namesAsList.remove(name);
				}
			}
			
			
			pebbleHelper.getContextMap().put("names", namesAsList);

			Name name = null;
			if (profileName != null) {
				name = Controller.getInstance().getName(profileName);
			}

			if (namesAsList.size() > 0) {

				if (name == null) {
					Profile activeProfileOpt = ProfileHelper.getInstance().getActiveProfileOpt();
					if(activeProfileOpt != null)
					{
						name = activeProfileOpt.getName();
					}else
					{
						name = namesAsList.get(0);
					}
				}

				// WE HAVE HERE ONLY ALLOWED NAMES SO PROFILE CAN'T BE NULL HERE
				Profile profile = Profile.getProfileOpt(name);
				
				pebbleHelper.getContextMap().put("profile", profile);
				pebbleHelper.getContextMap().put("name", name);

			} else {
				// no name no chance
			}

			return Response.ok(pebbleHelper.evaluate(),
					"text/html; charset=utf-8").build();
		} catch (Throwable e) {
			e.printStackTrace();
			return error404(request);
		}

	}

	public String decodeIfNotNull(String parameter)
			throws UnsupportedEncodingException {
		return parameter != null ? URLDecoder.decode(parameter, "UTF-8") : null;
	}

	private boolean isCompleteSubmit(Map<String, String[]> parameterMap) {

		if (!parameterMap.containsKey("profilename")) {
			return false;
		}

		List<Qorakeys> list = Arrays.asList(Qorakeys.BLOGENABLE,
				Qorakeys.PROFILEENABLE, Qorakeys.BLOGTITLE,
				Qorakeys.PROFILEAVATAR, Qorakeys.BLOGDESCRIPTION);
		boolean result = true;

		for (Qorakeys qorakey : list) {
			if (!parameterMap.containsKey(qorakey.toString())) {
				result = false;
				break;
			}

		}

		return result;
	}

	@Path("index/webdirectory.html")
	@GET
	public Response doWebdirectory() {

		try {
			PebbleHelper pebbleHelper = PebbleHelper
					.getPebbleHelper("web/index.mini.html");

			List<Pair<String, String>> websitesByValue = NameUtils
					.getWebsitesByValue(null);
			List<HTMLSearchResult> results = generateHTMLSearchresults(websitesByValue);

			pebbleHelper.getContextMap().put("searchresults", results);

			return Response.ok(pebbleHelper.evaluate(),
					"text/html; charset=utf-8").build();

		} catch (Throwable e) {
			e.printStackTrace();
			return error404(request);
		}

	}

	private List<HTMLSearchResult> handleBlogSearch(String blogSearchOpt) {
		List<HTMLSearchResult> results = new ArrayList<>();
		List<Triplet<String, String, String>> allEnabledBlogs = BlogUtils
				.getEnabledBlogs(blogSearchOpt);
		for (Triplet<String, String, String> triplet : allEnabledBlogs) {
			String name = triplet.getA();
			String title = triplet.getB();
			String description = triplet.getC();
			description = StringUtils.abbreviate(description, 150);

			results.add(new HTMLSearchResult(title, description, name,
					"/index/blog.html?blogname=" + name, "/index/blog.html?blogname="
							+ name, "/namepairs:" + name));
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

	@Path("index/index.html")
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
			return error404(request);
		}
	}

	
	String[] imgsArray = {"qora.png", "logo_header.png", "qora-user.png", "logo_bottom.png", "loading.gif",
			"00_generating.png", "01_genesis.jpg", "02_payment_in.png", 
			"02_payment_out.png", "03_name_registration.png",  "04_name_update.png", 
			"05_name_sale.png", "06_cancel_name_sale.png", "07_name_purchase_in.png", "07_name_purchase_out.png",
			"08_poll_creation.jpg", "09_poll_vote.jpg", "10_arbitrary_transaction.png", "11_asset_issue.png",
			"12_asset_transfer_in.png", "12_asset_transfer_out.png", "13_order_creation.png", "14_cancel_order.png", 
			"15_multi_payment_in.png", "15_multi_payment_out.png", "16_deploy_at.png", 
			"17_message_in.png", "17_message_out.png", "asset_trade.png", "at_tx_in.png", 
			"at_tx_out.png", "grleft.png", "grright.png", "redleft.png", "redright.png",
			"bar.gif", "bar_left.gif", "bar_right.gif"
			};
	
	@Path("index/img/{filename}")
	@GET
	public Response image(@PathParam("filename") String filename)
	{
		ArrayList<String> imgs = new ArrayList<String>();

		imgs.addAll(Arrays.asList(imgsArray));

		int imgnum = imgs.indexOf(filename);
		
		if(imgnum == -1)
		{
			return error404(request);
		}
		
		File file = new File("web/img/"+ imgs.get(imgnum));
		String type = "";
		
		switch(getFileExtention(imgs.get(imgnum)))
		{
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
		
		if(file.exists()){
			return Response.ok(file, type).build();
		}
		else
		{
			return error404(request);
		}
	}
	
	public static String getFileExtention(String filename){
		int dotPos = filename.lastIndexOf(".") + 1;
		return filename.substring(dotPos);
	}
	
	@SuppressWarnings("unchecked")
	@Path("index/API.html")
	@GET
	public Response handleAPICall() {

		try {
			PebbleHelper pebbleHelper = PebbleHelper
					.getPebbleHelper("web/apianswer.html");
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

		return error404(request);

	}

	@Path("index/libs/css/style.css")
	@GET
	public Response style() {
		File file = new File("web/libs/css/style.css");

		if (file.exists()) {
			return Response.ok(file, "text/css").build();
		} else {
			return error404(request);
		}
	}

	@Path("index/libs/css/sidebar.css")
	@GET
	public Response sidebarcss() {
		File file = new File("web/libs/css/sidebar.css");

		if (file.exists()) {
			return Response.ok(file, "text/css").build();
		} else {
			return error404(request);
		}
	}

	@Path("index/libs/css/timeline.css")
	@GET
	public Response timelinecss() {
		File file = new File("web/libs/css/timeline.css");

		if (file.exists()) {
			return Response.ok(file, "text/css").build();
		} else {
			return error404(request);
		}
	}

	@Path("index/libs/js/sidebar.js")
	@GET
	public Response sidebarjs() {
		File file = new File("web/libs/js/sidebar.js");

		if (file.exists()) {
			return Response.ok(file, "text/javascript").build();
		} else {
			return error404(request);
		}
	}

	@SuppressWarnings("unchecked")
	@Path("index/postblog.html")
	@GET
	public Response postBlog() {

		try {

			PebbleHelper pebbleHelper = PebbleHelper
					.getPebbleHelper("web/postblog.html");

			pebbleHelper.getContextMap().put("errormessage", "");
			pebbleHelper.getContextMap().put("font", "");
			pebbleHelper.getContextMap().put("content", "");
			pebbleHelper.getContextMap().put("option", "");
			pebbleHelper.getContextMap().put("oldtitle", "");
			pebbleHelper.getContextMap().put("oldcreator", "");
			pebbleHelper.getContextMap().put("oldcontent", "");
			pebbleHelper.getContextMap().put("oldfee", "");
			pebbleHelper.getContextMap().put("preview", "");

			String title = request.getParameter(BlogPostResource.TITLE_KEY);
			String creator = request.getParameter("creator");
			String contentparam = request.getParameter("content");
			String fee = request.getParameter("fee");
			String preview = request.getParameter("preview");
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
								"<div class=\"alert alert-danger\" role=\"alert\">You can't post to this blog! None of your accounts has balance or the blogowner did not allow your accounts to post!<br></div>");

			}
			
			Profile activeProfileOpt = ProfileHelper.getInstance().getActiveProfileOpt();

			if(activeProfileOpt != null && resultingNames.contains(activeProfileOpt.getName()))
			{
				pebbleHelper.getContextMap().put("primaryname", activeProfileOpt.getName().getName());
			}
			
			pebbleHelper.getContextMap().put("option", accountStrings);

			if (StringUtil.isNotBlank(creator)
					&& StringUtil.isNotBlank(contentparam)
					&& StringUtil.isNotBlank(fee)) {
				JSONObject json = new JSONObject();

				title = URLDecoder.decode(title, "UTF-8");
				contentparam = URLDecoder.decode(contentparam, "UTF-8");

				Pair<Account, NameResult> nameToAdress = NameUtils
						.nameToAdress(creator);

				String authorOpt = null;
				if (nameToAdress.getB() == NameResult.OK) {
					authorOpt = creator;
					json.put(BlogPostResource.AUTHOR, authorOpt);
					json.put("creator", nameToAdress.getA().getAddress());
				} else {
					json.put("creator", creator);
				}

				json.put("fee", fee);
				json.put("title", title);
				json.put("body", contentparam);

				if (StringUtils.isNotBlank(preview)) {
					pebbleHelper.getContextMap().put("oldtitle", title);
					pebbleHelper.getContextMap().put("oldfee", fee);
					pebbleHelper.getContextMap().put("oldcontent",
							contentparam.replaceAll("\\n", "\\\\n"));
					pebbleHelper.getContextMap().put("oldcreator", creator);
					BlogEntry entry = new BlogEntry(title, contentparam,
							authorOpt, new Date().getTime(), creator);

					pebbleHelper.getContextMap().put("blogposts",
							Arrays.asList(entry));

					return Response.ok(pebbleHelper.evaluate(),
							"text/html; charset=utf-8").build();
				}

				try {
					String result = new BlogPostResource().addBlogEntry(
							json.toJSONString(), blogname);

					pebbleHelper
							.getContextMap()
							.put("font",
									"<div class=\"alert alert-success\" role=\"alert\">Your post was successful<br>"
											+ result + "</div>");

				} catch (WebApplicationException e) {

					pebbleHelper.getContextMap().put("oldtitle", title);
					pebbleHelper.getContextMap().put("oldfee", fee);
					pebbleHelper.getContextMap().put("oldcontent",
							contentparam.replaceAll("\\n", "\\\\n"));
					pebbleHelper.getContextMap().put("oldcreator", creator);

					pebbleHelper
							.getContextMap()
							.put("font",
									"<div class=\"alert alert-danger\" role=\"alert\">Your post was NOT successful<br>"
											+ e.getResponse().getEntity()
											+ "</div>");

				}

			}

			return Response.ok(pebbleHelper.evaluate(),
					"text/html; charset=utf-8").build();
		} catch (Throwable e) {
			e.printStackTrace();
			return error404(request);
		}
	}

	@Path("index/followblog.html")
	@GET
	public Response followBlog() {
		try {
			String blogname = request
					.getParameter(BlogPostResource.BLOGNAME_KEY);
			String followString = request.getParameter("follow");
			NameMap nameMap = DBSet.getInstance().getNameMap();
			Profile activeProfileOpt = ProfileHelper.getInstance()
					.getActiveProfileOpt();

			if (followString != null && activeProfileOpt != null && blogname != null
					&& nameMap.contains(blogname)) {
				boolean follow = Boolean.valueOf(followString);
				Name name = nameMap.get(blogname);
				Profile profile = Profile.getProfileOpt(name);
				if (activeProfileOpt.isProfileEnabled()) {

					if (follow) {
						if (profile != null && profile.isProfileEnabled()
								&& profile.isBlogEnabled()
								&& !activeProfileOpt.getFollowedBlogs()
										.contains(blogname)) {
							activeProfileOpt.addFollowedBlog(blogname);
							String result;
							try {
								
								result = activeProfileOpt.saveProfile();
								result = "<div class=\"alert alert-success\" role=\"alert\">You follow this blog now<br>"
										+ result + "</div>";
							} catch (WebApplicationException e) {
								result =
										"<center><div class=\"alert alert-danger\" role=\"alert\">Blog follow not successful<br>"
												+ e.getResponse().getEntity()
												+ "</div></center>";
							}
							
							return getBlog(result);
						}
							
					}else
					{
						if(activeProfileOpt.getFollowedBlogs()
										.contains(blogname))
						{
							activeProfileOpt.removeFollowedBlog(blogname);
							String result;
							try {
								result = activeProfileOpt.saveProfile();
								result = "<div class=\"alert alert-success\" role=\"alert\">You follow this blog now<br>"
										+ result + "</div>";
							} catch (WebApplicationException e) {
								result =
										"<center><div class=\"alert alert-danger\" role=\"alert\">Blog unfollow not successful<br>"
												+ e.getResponse().getEntity()
												+ "</div></center>";
							}
							
							return getBlog(result);
							
						}
					}
					

				}
			}

			return getBlog(null);
		} catch (Throwable e) {
			e.printStackTrace();
			return error404(request);
		}

	}

	@Path("index/blog.html")
	@GET
	public Response getBlog(String messageOpt) {

		try {
			String blogname = request
					.getParameter(BlogPostResource.BLOGNAME_KEY);
			String switchprofile = request.getParameter("switchprofile");

			ProfileHelper.getInstance().switchProfileOpt(switchprofile);

			PebbleHelper pebbleHelper = PebbleHelper
					.getPebbleHelper("web/blog.html");
			pebbleHelper.getContextMap().put("postblogurl", "postblog.html");
			pebbleHelper.getContextMap().put("apimessage", messageOpt);

			NameMap nameMap = DBSet.getInstance().getNameMap();
			if (blogname != null) {
				if (!nameMap.contains(blogname)) {
					return Response.ok(
							PebbleHelper.getPebbleHelper(
									"web/blogdisabled.html").evaluate(),
							"text/html; charset=utf-8").build();
				}

				Name name = nameMap.get(blogname);
				Profile profile = Profile.getProfileOpt(name);

				if (profile == null || !profile.isProfileEnabled() || !profile.isBlogEnabled()) {
					pebbleHelper = PebbleHelper
							.getPebbleHelper("web/blogdisabled.html");
					if (Controller.getInstance().getAccountByAddress(
							name.getOwner().getAddress()) != null) {
						String resultcall = "/settings.html?profilename="
								+ blogname;
						String template = readFile("web/blogenabletemplate",
								StandardCharsets.UTF_8);
						template = template.replace("!TEXT!", "here");
						template = template.replace("!LINK!", resultcall);
						pebbleHelper.getContextMap().put(
								"enableblog",
								"You can activate the blog by clicking "
										+ template);
					}
					return Response.ok(pebbleHelper.evaluate(),
							"text/html; charset=utf-8").build();
				}

				pebbleHelper.getContextMap().put("postblogurl",
						"postblog.html?blogname=" + blogname);
			}

			Profile activeProfileOpt = ProfileHelper.getInstance().getActiveProfileOpt();
				pebbleHelper.getContextMap().put("isFollowing", activeProfileOpt != null && !activeProfileOpt.getFollowedBlogs().contains(blogname));
			
			List<BlogEntry> blogPosts = BlogUtils.getBlogPosts(blogname);

			pebbleHelper.getContextMap().put("blogposts", blogPosts);

			return Response.ok(pebbleHelper.evaluate(),
					"text/html; charset=utf-8").build();
		} catch (Throwable e) {
			e.printStackTrace();
			return error404(request);
		}
	}

	@Path("index/libs/Base58.js")
	@GET
	public Response Base58js()
	{
		File file = new File("web/libs/js/Base58.js");
		
		if(file.exists()){
			return Response.ok(file, "text/javascript").build();
		}
		else
		{
			return error404(request);
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
			return error404(request);
		}
	}

	@Path("index/libs/angular/angular.min.{version}.js")
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
			return error404(request);
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
			return error404(request);
		}
	}

	public Response error404(HttpServletRequest request) {

		try {
			PebbleHelper pebbleHelper = PebbleHelper
					.getPebbleHelper("web/404.html");
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
			PebbleHelper pebbleHelper = PebbleHelper
					.getPebbleHelper("web/index.mini.html");
			NameMap nameMap = DBSet.getInstance().getNameMap();

			if (nameMap.contains(name)) {
				Name nameObj = nameMap.get(name);
				String value = nameObj.getValue();

				JSONObject resultJson = NameUtils.getJsonForNameOpt(nameObj);

				// BAD FORMAT --> NO KEYVALUE
				if (resultJson == null) {
					resultJson = new JSONObject();
					resultJson.put(Qorakeys.DEFAULT.toString(), value);

					value = jsonToFineSting(resultJson.toJSONString());
				}

				pebbleHelper.getContextMap().put("keyvaluepairs", resultJson);
				pebbleHelper.getContextMap().put("dataname", name);

				return Response.status(200)
						.header("Content-Type", "text/html; charset=utf-8")
						.entity(pebbleHelper.evaluate()).build();

			} else {
				return error404(request);
			}
		} catch (Throwable e) {
			e.printStackTrace();
			return error404(request);
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
			return readFile("web/index.mini.html", StandardCharsets.UTF_8);

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
			return error404(request);
		}

		String value = name.getValue();

		// REDIRECT
		if (value.toLowerCase().startsWith("http://")
				|| value.toLowerCase().startsWith("https://")) {
			return Response.status(302).header("Location", value).build();
		}

		JSONObject jsonObject = NameUtils.getJsonForNameOpt(name);

		String website = null;
		if (jsonObject != null
				&& jsonObject.containsKey(Qorakeys.WEBSITE.getKeyname())) {
			website = (String) jsonObject.get(Qorakeys.WEBSITE.getKeyname());
		}

		if (website == null) {
			try {
				PebbleHelper pebbleHelper = PebbleHelper
						.getPebbleHelper("web/websitenotfound.html");
				pebbleHelper.getContextMap().put("name",
						nameName.replaceAll(" ", "%20"));

				return Response.ok(pebbleHelper.evaluate(),
						"text/html; charset=utf-8").build();
			} catch (Throwable e) {
				e.printStackTrace();
				return error404(request);
			}

		}

		website = injectValues(website);

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
