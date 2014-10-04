package database.serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

import org.mapdb.Serializer;

import qora.assets.Order;

public class OrderSerializer implements Serializer<Order>, Serializable
{
	private static final long serialVersionUID = -6538913048331349777L;

	@Override
	public void serialize(DataOutput out, Order value) throws IOException 
	{
		out.writeInt(value.getDataLength());
        out.write(value.toBytes());
    }

    @Override
    public Order deserialize(DataInput in, int available) throws IOException 
    {
    	int length = in.readInt();
        byte[] bytes = new byte[length];
        in.readFully(bytes);
        try 
        {
        	return Order.parse(bytes);
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
