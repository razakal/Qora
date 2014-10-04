package utils;

public class Triplet<T, U, V> extends Pair<T, U> {
	
	private V c;
	
	public Triplet(T a, U b, V c)
	{
		super(a, b);
		this.c = c;
	}
	
	public Triplet()
	{
		super();
	}
	
	public V getC()
	{
		return c;
	}
	
	public void setC(V c)
	{
		this.c = c;
	}
	
}
