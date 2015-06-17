package namewebserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.util.StringUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import qora.account.Account;
import qora.block.Block;
import qora.crypto.Crypto;
import qora.naming.Name;
import qora.transaction.Transaction;
import qora.web.BlogBlackWhiteList;
import qora.web.blog.BlogEntry;
import settings.Settings;
import utils.AccountBalanceComparator;
import utils.BlogUtils;
import utils.GZIP;
import utils.JSonWriter;
import utils.NameUtils;
import utils.NameUtils.NameResult;
import utils.Pair;
import utils.Qorakeys;
import utils.Triplet;
import api.ATResource;
import api.AddressesResource;
import api.ApiErrorFactory;
import api.BlocksResource;
import api.BlogPostResource;
import api.NameSalesResource;
import api.NamesResource;
import api.TransactionsResource;
import controller.Controller;
import database.DBSet;
import database.NameMap;

@Path("/")
public class NamesWebResource {
	@Context
	HttpServletRequest request;

	@GET
	public Response Default() {
		return handleDefault();
	}

	public Response handleDefault() {
		try {

			String searchValue = request.getParameter("search");
			String webDirectory = request.getParameter("webdirectory");
			String blogDirectory = request.getParameter("blogdirectory");
			String content = readFile("web/index.html", StandardCharsets.UTF_8);

			if (searchValue == null
					&& webDirectory == null
					&& blogDirectory == null ) {

				content = replaceWarning(content);
				return Response.ok(content, "text/html; charset=utf-8").build();
			} else if (searchValue != null || webDirectory != null
					|| blogDirectory != null ) {
				List<Pair<String, String>> searchResults;
				content = readFile("web/index.mini.html",
						StandardCharsets.UTF_8);
				String searchResultTemplate = readFile("web/searchresult",
						StandardCharsets.UTF_8);
				if (webDirectory != null) {
					searchResults = NameUtils.getWebsitesByValue(null);
				} else if (blogDirectory != null) {
					return handleBlogSearch(content, searchResultTemplate, null);
				} else {
					searchResults = NameUtils.getWebsitesByValue(searchValue);
				} 

				String results = "";
				for (Pair<String, String> result : searchResults) {
					String name = result.getA();
					String websitecontent = result.getB();
					Document htmlDoc = Jsoup.parse(websitecontent);
					String title = selectTitleOpt(htmlDoc);
					title = title == null ? "" : title;
					String description = selectDescriptionOpt(htmlDoc);
					description = description == null ? "" : description;

					results += searchResultTemplate.replace("!Name!", name)
							.replace("!Title!", title)
							.replace("!Description!", description)
							.replace("!Titlelink!", "/" + name)
							.replace("!Namelink!", "/" + name)
							.replace("!keyslink!", "/namepairs:" + name);

				}

				content = content.replace("<resultlist></resultlist>", results);

			}

			return Response.ok(content, "text/html; charset=utf-8").build();
		} catch (IOException e) {
			e.printStackTrace();
			return error404(request);
		}
	}
	
	
	@Path("blogsearch.html")
	@GET
	public Response doBlogSearch() {
		
		String searchValue = request.getParameter("search");
		String content;
		try {
			content = readFile("web/index.mini.html",
					StandardCharsets.UTF_8);
			if(StringUtil.isBlank(searchValue)) {
				
				content = replaceWarning(content);
				return Response.ok(content, "text/html; charset=utf-8").build();
			}
			
			String searchResultTemplate = readFile("web/searchresult",
					StandardCharsets.UTF_8);
			return handleBlogSearch(content, searchResultTemplate, searchValue);
			
			
		} catch (IOException e) {
			e.printStackTrace();
			return error404(request);
		}
		
	}
	
	

	private Response handleBlogSearch(String content,
			String searchResultTemplate, String blogSearchOpt) {
		String results = "";
		List<Triplet<String, String, String>> allEnabledBlogs = BlogUtils
				.getEnabledBlogs(blogSearchOpt);
		for (Triplet<String, String, String> triplet : allEnabledBlogs) {
			String name = triplet.getA();
			String title = triplet.getB();
			String description = triplet.getC();

			results += searchResultTemplate.replace("!Name!", name)
					.replace("!Title!", title)
					.replace("!Description!", description)
					.replace("!Titlelink!", "/blog.html?blogname=" + name)
					.replace("!Namelink!", "/blog.html?blogname=" + name)
					.replace("!keyslink!", "/namepairs:" + name);
		}
		content = content.replace("<resultlist></resultlist>", results);
		
		return Response.ok(content, "text/html; charset=utf-8").build();
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

	private String replaceWarning(String content) {
		content = content.replace("<warning></warning>", getWarning(request));
		return content;
	}

	@Path("index.html")
	@GET
	public Response handleIndex() {
		return handleDefault();
	}

	@Path("favicon.ico")
	@GET
	public Response favicon() {
		File file = new File("web/img/favicon.ico");

		if (file.exists()) {
			return Response.ok(file, "image/vnd.microsoft.icon").build();
		} else {
			return error404(request);
		}
	}

	@SuppressWarnings("unchecked")
	@Path("/API.html")
	@GET
	public Response handleAPICall() {

		try {
			String content = readFile("web/apianswer.html",
					StandardCharsets.UTF_8);
			// EXAMPLE POST/GET/DELETE
			String type = request.getParameter("type");
			// EXAMPLE /names/key/MyName
			String url = request.getParameter("apiurl");
			String okmsg = request.getParameter("okmsg");
			String errormsg = request.getParameter("errormsg");
			
			
			
			
			if (StringUtils.isBlank(type) ||( !type.equalsIgnoreCase("get")
					&& !type.equalsIgnoreCase("post")
					&& !type.equalsIgnoreCase("delete"))) {
				content = content.replace("!title!", "An Api error occured");
				content = content.replace("!apicall!", "You need a type parameter with value GET/POST or DELETE ");
				content = content.replace("!errormessage!", "");
				return Response.ok(content, "text/html; charset=utf-8").build();
			}
			
			if (StringUtils.isBlank(url)) {
				content = content.replace("!title!", "An Api error occured");
				content = content.replace("!apicall!", "You need to provide an apiurl parameter");
				content = content.replace("!errormessage!", "");
				return Response.ok(content, "text/html; charset=utf-8").build();
			}
			url = url.startsWith("/") ? url.substring(1) : url;
			
			Map<String, String[]> parameterMap = new HashMap<String, String[]>(request.getParameterMap());
			
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
				//CREATE CONNECTION
				URL urlToCall = new URL("http://127.0.0.1:" + Settings.getInstance().getRpcPort() + "/" + url);
				HttpURLConnection connection = (HttpURLConnection) urlToCall.openConnection();
				
				//EXECUTE
				connection.setRequestMethod(type.toUpperCase());
				
				if(type.equalsIgnoreCase("POST"))
				{
					connection.setDoOutput(true);
					connection.getOutputStream().write(json.toJSONString().getBytes("UTF-8"));
					connection.getOutputStream().flush();
					connection.getOutputStream().close();
				}
				
				//READ RESULT
				InputStream stream;
				if(connection.getResponseCode() == 400)
				{
					stream = connection.getErrorStream();
				}
				else
				{
					stream = connection.getInputStream();
				}
				
				InputStreamReader isReader = new InputStreamReader(stream, "UTF-8"); 
				BufferedReader br = new BufferedReader(isReader);
				String result = br.readLine(); 
				
				
				if(result.contains("message") && result.contains("error"))
				{
					if(StringUtils.isNotBlank(errormsg))
					{
						content = content.replace("<customtext></customtext>", "<font color=red>" + errormsg + "</font>");
					}
					content = content.replace("!title!", "An Api error occured");
					content = content.replace("!apicall!", "apicall: " +  type.toUpperCase() + " " + url + (json.size() > 0 ? json.toJSONString() : ""));
					content = content.replace("!errormessage!", "Result:" + result);
					return Response.ok(content, "text/html; charset=utf-8").build();
				}else
				{
					if(StringUtils.isNotBlank(okmsg))
					{
					content = content.replace("<customtext></customtext>", "<font color=green>" + okmsg + "</font>");
					}
					content = content.replace("!title!", "The API Call was successful");
					content = content.replace("!apicall!", "Submitted Api call: " +  type.toUpperCase() + " " + url + (json.size() > 0 ? json.toJSONString() : ""));
					content = content.replace("!errormessage!", "Result:" + result);
					return Response.ok(content, "text/html; charset=utf-8").build();
				}
				
			} catch (IOException e) {
				e.printStackTrace();
				String additionalHelp ="";
				if(e instanceof FileNotFoundException)
				{
					additionalHelp = "The apicall with the following apiurl is not existing: " ;
				}
				content = content.replace("!title!", "An Api error occured");
				content = content.replace("!apicall!", "You tried to submit the following apicall: " +  type.toUpperCase() + " " + url + (json.size() > 0 ? json.toJSONString() : ""));
				content = content.replace("!errormessage!", additionalHelp + e.getMessage() );
				return Response.ok(content, "text/html; charset=utf-8").build();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		return error404(request);
		
		

	}

	@Path("img/qora.png")
	@GET
	public Response qorapng() {
		File file = new File("web/img/qora.png");

		if (file.exists()) {
			return Response.ok(file, "image/png").build();
		} else {
			return error404(request);
		}
	}

	@Path("img/logo_header.png")
	@GET
	public Response logo_header() {
		File file = new File("web/img/logo_header.png");

		if (file.exists()) {
			return Response.ok(file, "image/png").build();
		} else {
			return error404(request);
		}
	}

	@Path("img/qora-user.png")
	@GET
	public Response qorauserpng() {
		File file = new File("web/img/qora-user.png");

		if (file.exists()) {
			return Response.ok(file, "image/png").build();
		} else {
			return error404(request);
		}
	}

	@Path("libs/css/style.css")
	@GET
	public Response style() {
		File file = new File("web/libs/css/style.css");

		if (file.exists()) {
			return Response.ok(file, "text/css").build();
		} else {
			return error404(request);
		}
	}

	@Path("libs/css/sidebar.css")
	@GET
	public Response sidebarcss() {
		File file = new File("web/libs/css/sidebar.css");

		if (file.exists()) {
			return Response.ok(file, "text/css").build();
		} else {
			return error404(request);
		}
	}

	@Path("libs/js/sidebar.js")
	@GET
	public Response sidebarjs() {
		File file = new File("web/libs/js/sidebar.js");

		if (file.exists()) {
			return Response.ok(file, "text/javascript").build();
		} else {
			return error404(request);
		}
	}

	@Path("webdirectory.html")
	@GET
	public Response websites() {

		try {
			String content = readFile("web/webdirectory.html",
					StandardCharsets.UTF_8);

			content = replaceWarning(content);

			List<Pair<String, String>> namesContainingWebsites = NameUtils
					.getNamesContainingWebsites();
			String linksAsHtml = "";
			for (Pair<String, String> websitepair : namesContainingWebsites) {
				String name = websitepair.getA();
				linksAsHtml += "<a href=/" + name.replaceAll(" ", "%20") + ">"
						+ name + "</a><br>";
			}

			content = content.replace("!linkstoreplace!", linksAsHtml);

			return Response.ok(content, "text/html; charset=utf-8").build();
		} catch (IOException e) {
			e.printStackTrace();
			return error404(request);
		}
	}

	@SuppressWarnings("unchecked")
	@Path("postblog.html")
	@GET
	public Response postBlog() {

		try {
			String content = readFile("web/postblog.html",
					StandardCharsets.UTF_8);

			String title = request.getParameter(BlogPostResource.TITLE_KEY);
			String creator = request.getParameter("creator");
			String contentparam = request.getParameter("content");
			String fee = request.getParameter("fee");
			String blogname = request
					.getParameter(BlogPostResource.BLOGNAME_KEY);

			
			
			BlogBlackWhiteList blogBlackWhiteList = BlogBlackWhiteList.getBlogBlackWhiteList(blogname);
			
			Pair<List<Account>, List<Name>> ownAllowedElements = blogBlackWhiteList.getOwnAllowedElements(true);
			
			List<Account> resultingAccounts = new ArrayList<Account>(ownAllowedElements.getA());
			List<Name> resultingNames = ownAllowedElements.getB();
			

			Collections.sort( resultingAccounts, new AccountBalanceComparator());
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
			
			//are we allowed to post
			if(resultingNames.size() == 0 && resultingAccounts.size() == 0)
			{
				content = content.replaceAll("<errormessage></errormessage>", "<font color=red>You can't post to this blog! None of your accounts has balance or the blogowner did not allow your accounts to post!</font><br>");
			}
			
			

			content = content.replaceAll("<option></option>", accountStrings);

			
			
			
			if (StringUtil.isNotBlank(creator)
					&& StringUtil.isNotBlank(contentparam)
					&& StringUtil.isNotBlank(fee)) {
				JSONObject json = new JSONObject();
				
				Pair<Account, NameResult> nameToAdress = NameUtils.nameToAdress(creator);
				
				if(nameToAdress.getB() == NameResult.OK)
				{
					json.put(BlogPostResource.AUTHOR, creator);
					json.put("creator", nameToAdress.getA().getAddress());
				}else
				{
					json.put("creator", creator);
				}
				
				json.put("fee", fee);
				json.put("title", title);
				json.put("body", contentparam);

				try {
					String result = new BlogPostResource().addBlogEntry(
							json.toJSONString(), blogname);

					content = content
							.replaceAll(
									"<font></font>",
									"<div class=\"alert alert-success\" role=\"alert\">Your post was successful<br>"
											+ result + "</div>");

				} catch (WebApplicationException e) {
					content = content
							.replaceAll(
									"<font></font>",
									"<div class=\"alert alert-danger\" role=\"alert\">Your post was NOT successful<br>"
											+ e.getResponse().getEntity()
											+ "</div>");

				}

			}

			return Response.ok(content, "text/html; charset=utf-8").build();
		} catch (IOException e) {
			e.printStackTrace();
			return error404(request);
		}
	}

	public String transformURLIntoLinks(String text) {
		String urlValidationRegex = "(https?|ftp)://(www\\d?|[a-zA-Z0-9]+)?.[a-zA-Z0-9-]+(\\:|.)([a-zA-Z0-9.]+|(\\d+)?)([/?:].*)?";
		Pattern p = Pattern.compile(urlValidationRegex);
		Matcher m = p.matcher(text);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			String found = m.group(0);
			m.appendReplacement(sb, "<a href='" + found + "'>" + found + "</a>");
		}
		m.appendTail(sb);
		return sb.toString();
	}

	private ArrayList<String> getAllLinks(String text) {
		ArrayList<String> links = new ArrayList<>();

		String regex = "\\(?\\b(http(s?)://|www[.])[-A-Za-z0-9+&amp;@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&amp;@#/%=~_()|]";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(text);
		while (m.find()) {
			String urlStr = m.group();
			if (urlStr.startsWith("(") && urlStr.endsWith(")")) {
				urlStr = urlStr.substring(1, urlStr.length() - 1);
			}
			links.add(urlStr);
		}
		return links;
	}

	@Path("blog.html")
	@GET
	public Response getBlog() {

		try {
			String blogname = request
					.getParameter(BlogPostResource.BLOGNAME_KEY);
			String content = readFile("web/blog.html", StandardCharsets.UTF_8);

			content = replaceWarning(content);

			NameMap nameMap = DBSet.getInstance().getNameMap();
			if (blogname != null) {
				if (!nameMap.contains(blogname)) {
					content = readFile("web/blogdisabled.html",
							StandardCharsets.UTF_8);
					return Response.ok(content, "text/html; charset=utf-8")
							.build();
				}

				Name name = nameMap.get(blogname);
				String value = GZIP.webDecompress(name.getValue());

				JSONObject jsonObject = null;
				try {
					jsonObject = (JSONObject) JSONValue.parse(value);
				} catch (Exception e) {
					// no valid json
				}
				if (jsonObject == null
						|| !jsonObject
								.containsKey(BlogPostResource.BLOGENABLE_KEY)) {
					content = readFile("web/blogdisabled.html",
							StandardCharsets.UTF_8);
					if(Controller.getInstance().getAccountByAddress(name.getOwner().getAddress()) != null)
					{
						String apiurl = "/names/key/" + name.getName();
						String type = "POST";
						String resultcall = "/API.html?"+ "apiurl=" + apiurl + "&type=" + type + "&key=" + BlogPostResource.BLOGENABLE_KEY + "&value=true&update=false&fee=1&okmsg=The blog will be available after the next block!";
						String template = readFile("web/blogenabletemplate",
								StandardCharsets.UTF_8);
						template = template.replace("!TEXT!", "here");
						template = template.replace("!LINK!", resultcall);
						
						content =content.replace("<enableblog></enableblog>", "You can activate the blog by clicking " + template);
					}
					return Response.ok(content, "text/html; charset=utf-8")
							.build();
				}

				content = content.replace("postblog.html",
						"postblog.html?blogname=" + blogname);
			}

			List<BlogEntry> blogPosts = BlogUtils
					.getBlogPosts(blogname);

			String results = "<br>";

			String entryTemplate = "<li><div class=\"timeline-badge primary\"><a>"
					+ "<i class=\"glyphicon glyphicon-record\" rel=\"tooltip\" title=\"POST TIME\" id=\"\">"
					+ "</i></a></div>"
					+ "<div class=\"timeline-panel\"><div class=\"timeline-heading\">"
					+ "<div class=\"media\"><div class=\"media-left media-middle\">"
					+ "<a href=\"#\"><img class=\"media-object\" src=\"img/qora-user.png\" alt=\"\"></a></div>"
					+ "<div class=\"media-body\">"
					+ "<h6 class=\"media-heading\"><b>Name</b></h6>Time</div></div>"
					+ "</div>"
					+ "<div class=\"timeline-body\"><p class=\"post-header\"><b>TITLE</b></p><p class=\"post-content\">CONTENT</p></div></div></li>";
					
			for (BlogEntry blogentry : blogPosts) {

				String converted = entryTemplate;
				String body = blogentry.getDescription();

				body = Jsoup.clean(body, Whitelist.basic());
				List<Pair<String, String>> linkList = createHtmlLinks(getAllLinks(body));

				for (Pair<String, String> link : linkList) {
					String originalLink = link.getA();
					String newLink = link.getB();

					body = body.replace(originalLink, newLink);
				}
				String nameOpt = blogentry.getNameOpt();
				if(nameOpt != null)
				{
					converted = converted.replaceAll("Name", blogentry.getNameOpt());
				}else
				{
					converted = converted.replaceAll("Name", blogentry.getCreator());
				}
				converted = converted.replaceAll("TITLE", blogentry.getTitleOpt());
				converted = converted.replaceAll("CONTENT", body);
				converted = converted.replaceAll("Time", blogentry.getCreationTime());

				results += converted;
			}

			content = content.replace("!blogposts!", results);

			return Response.ok(content, "text/html; charset=utf-8").build();
		} catch (IOException e) {
			e.printStackTrace();
			return error404(request);
		}
	}

	private List<Pair<String, String>> createHtmlLinks(List<String> links) {
		List<Pair<String, String>> result = new ArrayList<>();
		for (String link : links) {
			String refurbishedlink = StringEscapeUtils.unescapeHtml4(link);
			String youtubeWatchRegex = Pattern
					.quote("https://www.youtube.com/watch?v=")
					+ "([a-zA-Z0-9_-]+).*";
			String youTubeSlashRegex = Pattern.quote("https://youtu.be/")
					+ "([a-zA-Z0-9_-]+).*";
			if (refurbishedlink.toLowerCase().matches(youtubeWatchRegex)) {
				String vid = refurbishedlink
						.replaceAll(youtubeWatchRegex, "$1");

				result.add(new Pair<String, String>(link,
						getYoutubeEmbedHtml(vid)));
			} else if (refurbishedlink.toLowerCase().matches(youTubeSlashRegex)) {
				String vid = refurbishedlink
						.replaceAll(youTubeSlashRegex, "$1");

				result.add(new Pair<String, String>(link,
						getYoutubeEmbedHtml(vid)));
			} else {
				refurbishedlink = transformURLIntoLinks(refurbishedlink);
				result.add(new Pair<String, String>(link, refurbishedlink));
			}

		}

		return result;
	}

	private String getYoutubeEmbedHtml(String vid) {
		return "<iframe width=\"320\" height=\"215\" src=\"https://www.youtube.com/embed/"
				+ vid + "\" frameborder=\"0\" allowfullscreen></iframe>";
	}

	@Path("libs/jquery/jquery.{version}.js")
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

	@Path("libs/angular/angular.min.{version}.js")
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

	@Path("libs/bootstrap/{version}/{folder}/{filename}")
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
		String pathInfo = request.getPathInfo();
		pathInfo = pathInfo.substring(1, pathInfo.length());

		return Response
				.status(404)
				.header("Content-Type", "text/html; charset=utf-8")
				.entity(miniIndex().replace("<data></data>", pathInfo)
						+ "<h1>name \""
						+ pathInfo
						+ "\" does not exist</h1><hr>� <a href=http://www.qora.org>Qora</a></body></html>")
				.build();
	}

	private String getWarning(HttpServletRequest request) {
		if (Controller.getInstance().isWalletUnlocked()) {
			String ipAddress = request.getHeader("X-FORWARDED-FOR");
			if (ipAddress == null) {
				ipAddress = request.getRemoteAddr();
			}

			if (ipAddress.equals("127.0.0.1")) {
				return "<style type=\"text/css\">" //
						+ "#message_box {" //
						+ "position:absolute;" //
						+ "left:0; right:0; top:0;" //
						+ "z-index: 10;" //
						+ "background:#ffc;" //
						+ "padding:5px;" //
						+ "border:1px solid #CCCCCC;" //
						+ "text-align:center;" //
						+ "font-weight:bold;" //
						+ "width:auto;" //
						+ "}" //
						+ "</STYLE>" //
						+ "<div id=message_box>Wallet is unlocked!</div>" //
						+ "<div style='margin-top:50px'>";
			}
		}
		return "";
	}

	static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}

	@SuppressWarnings("unchecked")
	@Path("namepairs:{name}")
	@GET
	public Response showNamepairs(@PathParam("name") String name) {

		NameMap nameMap = DBSet.getInstance().getNameMap();

		if (nameMap.contains(name)) {
			String value = nameMap.get(name).getValue();

			JSONObject resultJson = null;
			try {

				// WEBPAGE GZIP DECOMPRESSOR
				value = GZIP.webDecompress(value);

				JSONObject jsonObject = (JSONObject) JSONValue.parse(value);
				if (jsonObject != null) {
					// Looks like valid jSon
					resultJson = jsonObject;

				}

			} catch (Exception e) {
				// no json probably
			}

			// BAD FORMAT --> NO KEYVALUE
			if (resultJson == null) {
				resultJson = new JSONObject();
				resultJson.put(Qorakeys.DEFAULT.toString(), value);

				value = jsonToFineSting(resultJson.toJSONString());
			}

			return Response
					.status(200)
					.header("Content-Type", "text/html; charset=utf-8")
					.entity(miniIndex()
							.replace("<data></data>", "namepairs:" + name)
							.replace(
									"<jsonresults></jsonresults>",
									"<br><div ng-app=\"myApp\" ng-controller=\"AppController\"><div class=\"panel panel-default\">" //
											+ "<div class=\"panel-heading\">Results on :"
											+ name
											+ "</div><table class=\"table\">" //
											+ "<tr ng-repeat=\"(key,value) in steps\"><td>{{ key }}</td><td>{{ value }}</td></tr></table></div></div>")
							.replace("$scope.steps = {}",
									"$scope.steps = " + value)).build();

		} else {
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

		String Value = name.getValue().toString();

		// REDIRECT
		if (Value.startsWith("http://") || Value.startsWith("https://")) {
			return Response.status(302).header("Location", Value).build();
		}

		// WEBPAGE GZIP DECOMPRESSOR
		Value = GZIP.webDecompress(Value);

		String website = null;
		try {
			JSONObject jsonObject = (JSONObject) JSONValue.parse(Value);
			if (jsonObject != null
					&& jsonObject.containsKey(Qorakeys.WEBSITE.getKeyname())) {
				website = (String) jsonObject
						.get(Qorakeys.WEBSITE.getKeyname());
			}

		} catch (Exception e) {
			// no json probably
		}

		if (website == null) {
			try {
				String content = readFile("web/websitenotfound.html",
						StandardCharsets.UTF_8);
				content = content
						.replaceAll(
								"!websitenotfoundcontent!",
								"<br><font color=\"red\">If this is your name, please do a name update for the name <a href=/name:"
										+ nameName.replaceAll(" ", "%20")
										+ ">"
										+ nameName
										+ "</a> and add the key \"website\" with webcontent as value to make this work.</font><br>You can see the current value <a href=/name:"
										+ nameName.replaceAll(" ", "%20")
										+ ">here</a>");

				Value = injectValues(Value);

				return Response.ok(content, "text/html; charset=utf-8").build();
			} catch (IOException e) {
				return error404(request);
			}

		}

		website = injectValues(website);

		// SHOW WEB-PAGE
		return Response.status(200)
				.header("Content-Type", "text/html; charset=utf-8")
				.entity(getWarning(request) + website).build();
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
