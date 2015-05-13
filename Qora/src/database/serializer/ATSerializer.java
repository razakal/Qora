package database.serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

import org.mapdb.Serializer;

import at.AT;

public class ATSerializer implements Serializer<AT>, Serializable
{
	private static final long serialVersionUID = -6538913048331349777L;

	@Override
	public void serialize(DataOutput out, AT value) throws IOException 
	{
		out.writeInt(value.getCreationLength());
        out.write(value.toBytes(true));
    }

    @Override
    public AT deserialize(DataInput in, int available) throws IOException 
    {
    	int length = in.readInt();
        byte[] bytes = new byte[length];
        in.readFully(bytes);
        try 
        {
        	return AT.parse(bytes);
		} 
        catch (Exception e) 
        {
        	e.printStackTrace();
		}
		return null;
    }

    @Override
    public int fixedSize() 
    {
    	return -1;
    }
}
