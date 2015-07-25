package utils;

import java.io.StringWriter;

public class StrJSonFine{

    public static String convert(String str) {
        int indent = 0;
        
        boolean value = false;
        
    	char[] chars = str.toCharArray();
    	StringWriter output = new StringWriter();
    	
    	char ch;
    	for (int n = 0; n < chars.length; n++) {
    		ch = chars[n];
    		
    		if(ch == '"' && n > 0 && chars[n-1] != '\\' )
    		{
    			value = !value;
    		}

    		if(!value)
    		{
	    		if (ch == '[' || ch == '{') {
	    			output.write(ch);
	    			output.write("\r\n");
	    		    indent++;
	    		    writeIndentation(output, indent);
	    		} else if (ch == ',') {
	    			output.write(ch);
	    			output.write("\r\n");
	    			writeIndentation(output, indent);
	    		} else if (ch == ']' || ch == '}') {
	    			output.write("\r\n");
	    		    indent--;
	    		    writeIndentation(output, indent);
	    		    output.write(ch);
	    		} else {
	    			output.write(ch);
	    		}
    		}
    		else {
    			output.write(ch);
	    	}
    			
		}
    	return output.toString();
    }
	private static void writeIndentation(StringWriter writer, int indent) {
		for (int i = 0; i < indent; i++) {
			writer.write('\t');
		}
    }
}
