import qora.assets.Order;
import database.DBSet;


public class Orders {
	
	public static void main(String args[])
	{
		for(Order order: DBSet.getInstance().getOrderMap().getValues()) {
			if(order.getHave() == 1) {
			System.out.println(order.getHave() + " - " + order.getWant());
			}
		}
		
		for(Order order: DBSet.getInstance().getCompletedOrderMap().getValues()) {
			System.out.println(order.getHave() + " - " + order.getWant());
		}
	}

}
