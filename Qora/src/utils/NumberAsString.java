package utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class NumberAsString {
	private static NumberAsString instance;
	private DecimalFormat decimalFormat; 
	
	public static NumberAsString getInstance()
	{
		if(instance == null)
		{
			instance = new NumberAsString();
		}
		
		return instance;
	}
	
	public NumberAsString()
	{
		Locale locale = new Locale("en", "US");
		DecimalFormatSymbols symbols = new DecimalFormatSymbols(locale);
		symbols.setDecimalSeparator('.');
		symbols.setGroupingSeparator(',');
		
		decimalFormat = new DecimalFormat("###,##0.00000000", symbols);
	}
	
	public String numberAsString(Object amount) {
		return decimalFormat.format(amount);
	}

}
