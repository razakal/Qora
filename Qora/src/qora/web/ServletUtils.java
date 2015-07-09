package qora.web;

import javax.servlet.http.HttpServletRequest;

public class ServletUtils {
	
	
	public static boolean isRemoteRequest(HttpServletRequest servletRequestOpt)
	{
		if(servletRequestOpt != null)
		{
			String ipAdress = servletRequestOpt.getHeader("X-FORWARDED-FOR");
			
			if (ipAdress == null) {
				ipAdress = servletRequestOpt.getRemoteAddr();
			}
			
			if (!ipAdress.equals("127.0.0.1")) {
				return true;
			}
		}
		
		return false;
	}
	
}
