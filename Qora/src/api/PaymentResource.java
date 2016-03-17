package api;


import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import utils.APIUtils;

@Path("payment")
@Produces(MediaType.APPLICATION_JSON)
public class PaymentResource 
{
	
	private static final Logger LOGGER = Logger
			.getLogger(PaymentResource.class);
	@Context
	HttpServletRequest request;
	
	@POST
	@Consumes(MediaType.WILDCARD)
	public String createPayment(String x)
	{
		try
		{
			//READ JSON
			JSONObject jsonObject = (JSONObject) JSONValue.parse(x);
			String assetKey = (String) jsonObject.get("asset");
			String amount = (String) jsonObject.get("amount");
			String fee = (String) jsonObject.get("fee");
			String sender = (String) jsonObject.get("sender");
			String recipient = (String) jsonObject.get("recipient");
			
			return APIUtils.processPayment(assetKey, amount, fee, sender, recipient,x, request );
		}
		catch(NullPointerException| ClassCastException e)
		{
			//JSON EXCEPTION
			LOGGER.info(e);
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_JSON);
		}
	}

	
	
	
	
}
