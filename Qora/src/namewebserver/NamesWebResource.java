package namewebserver;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import utils.GZIP;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Context;
import javax.servlet.http.HttpServletRequest;

import qora.naming.Name;
import controller.Controller;


@Path("/")
public class NamesWebResource 
{
    @Context HttpServletRequest request;

	@GET
	public Response Default()
	{
		return Response.status(200)
				.header("Content-Type", "text/html; charset=utf-8")
				.entity("<html><head><title>Qora web-server</title>" //		
				+"<script type='text/javascript'>" //
				+ "function key(event) {return ('which' in event) ? event.which : event.keyCode;}" //
				+ "function goto() {" //
				+ "url = document.getElementById(\"name\").value;" //
				+ "document.location.href = '/'+url;" //
				+ "return 0;" //
				+ "}" //
				+ "</script>" //
				+ "</head>" //
				+ "<body>" //
				+ getWarning(request)
				+ "<center><h1 style='font-family: Verdana;'>Qora web-server</h1>" //
				+ "<img src='data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAIAAAACACAMAAAD04JH5AAAABGdBTUEAAL" //
				+ "GPC/xhBQAAAAFzUkdCAK7OHOkAAACoUExURQAAAJOT7Zub7bSz7piW5UM/wLW08K+u6qGg6ain70ZCwTo2vWJ" //
				+ "l/3t9/4WC1lJPxUM/wD46vm1r0zo2vXBtz4iF12dq/05KxD46vmJl/zo2vVNQxXR3/2JfylRRxnJ0/2pnzUpG" //
				+ "wl5h/2xpzkZCwWBj/56d52Nm/3Fz/VRRxnBt0Gtozl1ZyWlmzVRRxoSC1nx//3t9/1VT2ERAwGRn/0A8vzk0v" //
				+ "F1g/wdafsQAAAAzdFJOUwAkORoM9RECBi3o2bhPW4uzwBrlaUuGmsvy8XdgRMx0d9vOi6PfSaE2XTGdw7Owc4" //
				+ "y64OTa8VkAAAmVSURBVHja7Vtpl5rKFpUCi6mhGIVmEBBFRRuTZTD//5+9UlsmKUCk7113rXc+JbEju8+4z8B" //
				+ "s9n8ZIQwPZCOMkbXGYiEUGnLCQ/YfeTSkUt/35cPX8biMvmV5PH4dYtn30xRyP/dsltE0KjnFlqIo6iXHcv6W" //
				+ "658vF1tRrPgk81BjfkYXELjItQRhfpXzs1z/WRCizHUT+AOqT7HaI0F0Kr/4s1w14YhRvPfTSTEwNC8fMtXpe" //
				+ "HQNhqhmB5nXmMkA+OFaaFc7QfAP2wgl0zgk9Peu4lzy80uS57pwkH36bc+n+dOXqornMeKo2YnX3lMDJVvC+Q" //
				+ "2xUai9p31Lz/Pxz88dG7nmWDtolPyl6uc3xba/5HSMFlhNRnZ+nkD0tSXTr3sC76LBgd8XlMLS4LlXy10s2vP" //
				+ "zVHKxQ/CKGViYIP08qdjIfCEgofle8LWZQXUNarD+TRx8EwM4O7Y1NB5hYunnH5CrFQYRBX5y/X9bwXblATpg" //
				+ "eTS9/h9WQGYvTeAAcs4/Jbkdm33UlUfC+Qelzwpsehim/ysJrcuwmoWtALiu/O8Oqfy5I9ypeCGYoi8jQdeHW" //
				+ "CHkO+qfgdQh7qwgZJiAp+GVf7MMpHlgGq5lKUOSt20QuSJtor78n+e2ncUJpWkMV1Ely+GeIU1Pmar3cjdnaZ" //
				+ "BCASC1+/m5Hn0ZezLRo3w/PGLu3oNg7TPt9M8Qe6iuYPXSG9rEphB6+EG7G3DysTsD2OohwZrv7d/8WOn2RkE" //
				+ "x255PuUJ3SY/36aCCyvnmIRK7jOkcnvkJNsBa71K/GiaDOx0Wd5Fd7pQLiHpKiEARupSWyfCFTovT5LgroHXU" //
				+ "9CWOjjscQFTcV3sczYxVp6MwHtIGB5GX5PgV1q7/csfPgbDLCva+PkSAodgRNrE/or9i+FDtYGghqJoUykuHr" //
				+ "P/YH9VocxiBQ/aqkKmRoHlO4vSKa47sLzut4NQCARB/UFhb+9ETHyZxiVYQUTnIYeGJ2APN1/4b7S3m96Tonk" //
				+ "cxVeaNLxKdcJbhW4MWak9m2AooY1AlKQDTh/cGDBqR4s1V82FbSCqDcxWZ3HszPw43mX0VgUsyneQA8ttDnpm" //
				+ "pkCpC9M2NtFAgWEBA6ftDLiomhbju3uOAXxNiULTkCcaNXEJKR6J7TwWAxCXxD0wx6aMQ4fud7DZJZBNCFprb" //
				+ "xiRDZ0gqdLlwumqYlIXmSmxOAoClCZUuF24zPOqQEwBMEAJ3kW0CgBsrSL9IANKJnj8DWU7ocPzrJLr901ywJ" //
				+ "gNAVvIOf7pfEtLESZsKACO3d1xzxcS8aN9uoEuUTLf+AWunPc5ONDsjFILLEky38eDbU02eHygiAB3R0wFIrf" //
				+ "Zsn38Bbha3VyIxnHDro8VRO4AjpnvHCyEPTwngRACw3HOzZTuAyJ1u54TDIGp/ChkAxjYhAA5kDuHX/GcAzEj" //
				+ "loAPAUZ4SANMFgOSfUwJgzYOY/5saYKG5Fl70gUkB4N4jXov/ohPOWCYNs+e5DRnAG3mAZbiqlBto97kiXPPA" //
				+ "cWoAkK9J2YL6SmsmJNWCsQBYPvhdlYB/9OFQVi5NV/c5UjUcXQswgNXfimwW0mNtyZiNhHirhkQA/EhOzPDex" //
				+ "5+KbIKkqOxmrQXAfAA/hDChd45jGRHLgKCK4HOzAA9t0oZ6qQHAjIjECYXxnBBiBJ8VCKsFoL/VSctVK+gn3H" //
				+ "z7RwIrjsezYlpqWGGRPBCwlW55fptR7H6gL+BoabGtIygGHVAurHAHQCbt5huciJeCj03NCvwjrGlZ+bbC/NZ" //
				+ "6wNOcACAEb/SGlNeMBVDq4J4SsZW1256A1B2r73THnOat/tQQ/KLKyfxtKPXw8475AM32B/2uLuVIASekGoKt" //
				+ "VCCgjKsVnOy+AUiJExLUfxQGk2Bbk1/lDJgOql7wuQ2kxwkBxxvqfI5z3e3vaUyYEeWVWSKZaUirP59V2e7Kp" //
				+ "LyoIfjwPKoyR1ZUdN/jwo4p2YBAoL3V37qmd1zxmMWqhiAAdGE6Wd6nbPcEA7thPCAQoFRLex+LYMcUCGpW+L" //
				+ "MNQHHxCMvLGsYg3uvYRr8f3qxQDfkg2BULzQaCD0966IAtv5gzjzlxzTmEGsKaFT5XC6mopAxo+EEAYMsUKSZ" //
				+ "OlOfWkG0F7TWKj1REYwuClotPs2NfEJtDJsJewwpFzM/oZNGwAnj+lYBCvoJSjH5mcqUANX+/FuBiI+o1YsF7" //
				+ "XhrSJ/LOSM+G3GFB/okCwEI9UhPB8+yDdru2ZqchCJKGHwSlpjGCuhWkp0sSaEQde9O10T8vYjEFqBGxVcCX3" //
				+ "XnQ0IEEn7zo0LE5xlYYEAuU1LBCUGia2dWt8PcJAcuZXbvj6+6S4foRNKzglclu5y021c+2u0YwsnzYtT0X1s" //
				+ "jge4PxqoO6FagZ+9BB3Qpbr6kDJu284BPFZWim/TpYVHXwd1WQEIyglipWqycrMMZy3n2AYxla303gkx9I5f8" //
				+ "A2886QdkxzVWv3XMSiFBfSuCooGYFTAG0MhZqRPlz4Ulcww+t7iuauSgsD7KfdgUEl0pVAJuPSlKGUk0Hq623" //
				+ "g401Z+8hWX6xlThJr2dEpPxM/aoTsUVhBZwqagg2i6BeFljNjQZchEWWhQyTp8tsxrK1eKqnvW1JAXBSbrQrX" //
				+ "p3wsPyQW75cF26XZMb+IYZs7H1YOlMNwQaXv5KIBXUdbCW+OdgfdE13O6YT7UJEUTyYBQVoktFtSUIgqOeqzd" //
				+ "Z72rePvCcUrdOjyrIQNK1QkBBcsxpWaPK7sRetuWgZaaUwNQj5QwcspJJfVQlabg5G3pSKmfGIUNwdb1ckKgi" //
				+ "pivC7tquH0VYI04cVgES0Qn9hl0daYR5ZMlUOihpWkIafQtByNs4K2A/kRx/BAK9hBW/4uIE3xluhQMBJTQow" //
				+ "/BqOo8ZaASN4tJNskwJ8/t69sPAGljPWCkV2xQiafdnwwRsLUCSMesNBR0WF4aTfm02Vqi92w28SMT2w1HFWQ" //
				+ "HzxJcmiGgvX4vPKsi89Hce84pSXCGZw520/Slks2nrTjmjYu+sxAalXr7Skinie9xKAGevH6xHhkEcuwdvY2Y" //
				+ "uDN4YCsSLmr7uBy88mEpbyjaX+4gtfgqqYs+mENqz1iyE5V6cEwGppcsrU4e+d5RfVmvD84Z6c/X2YCfol77Y" //
				+ "F/tgRhehg+vRscgEGQsv7S7eE6+vbe7cZQiE/+wnh7q8dZ4qq6Ld3Oqpyub53rCpZfLq+fPBzbz9ztxevzdPh" //
				+ "cHux47jEcry97HF789r3UxrOfl44mgeJLBtGeBPDkOUE8DQz+0/K/wD+Q5sY7PMaLQAAAABJRU5ErkJggg=='>" //
				+ "<br><br><span style='font-family: Verdana;'>Enter the name:<br>" //
				+ "<input size=55 id=name value=\"\" onkeyup=\"if(key(event)==13){document.getElementById('button').click();}\">" //
				+ "<input id=button type=button value=\"Search\" style='font-family: Verdana;' onclick=javascript:goto()><br>" //
				+ "<br>© <a href=http://qora.org>Qora</a></span></center>" //
				+ "<body></html>")
				.build();		
	}
	
	@Path("{name}")
	@GET
	public Response getNames(@PathParam("name") String nameName)
	{
		Name name = Controller.getInstance().getName(nameName);
		
		//CHECK IF NAME EXISTS
		if(name == null)
		{
			return Response.status(404)
					.header("Content-Type", "text/html; charset=utf-8")
					.entity("<html><title>404 Not Found</title><body><h1>name does not exist</h1><hr>© <a href=http://www.qora.org>Qora</a></body></html>")
					.build();
		}
		
		String Value = name.getValue().toString();
	
		//REDIRECT
		if(Value.startsWith("http://") || Value.startsWith("https://"))
		{
			return Response.status(302)
					.header("Location", Value)
					.build();
		}
		
		//WEBPAGE GZIP DECOMPRESSOR
		Value = GZIP.webDecompress(Value);
        
        //PROCESSING TAG INJ
        Pattern pattern = Pattern.compile("(?i)<inj>(.*?)</inj>");
        Matcher matcher = pattern.matcher(Value);
        while (matcher.find()) {
        	Name nameinj = Controller.getInstance().getName(matcher.group(1));
        	Value = Value.replace( matcher.group(), GZIP.webDecompress(nameinj.getValue().toString()));
        }
     
		//SHOW WEB-PAGE
		return Response.status(200)
				.header("Content-Type", "text/html; charset=utf-8")
				.entity(getWarning(request)+Value)
				.build();
	}
	
	private String getWarning(HttpServletRequest request)
	{
		if(Controller.getInstance().isWalletUnlocked())
		{	
			String ipAddress  = request.getHeader("X-FORWARDED-FOR");  
		    if(ipAddress == null)  
		    {  
		    	ipAddress = request.getRemoteAddr();  
		    }  
			
			if(ipAddress.equals("127.0.0.1"))
			{
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
				+ "<div id=message_box>Wallet is unlock!</div>" //
				+ "<div style='margin-top:50px'>";
			}
		}
		return "";	
	}
}