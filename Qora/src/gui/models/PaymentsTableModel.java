package gui.models;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import controller.Controller;
import qora.assets.Asset;
import qora.payment.Payment;

@SuppressWarnings("serial")
public class PaymentsTableModel extends AbstractTableModel
{
	private static final int COLUMN_ACCOUNT = 0;
	public static final int COLUMN_ASSET = 1;
	public static final int COLUMN_AMOUNT = 2;
	
	private String[] columnNames = {"Account", "Asset", "Amount"};
	private List<Payment> payments;
	
	public PaymentsTableModel(List<Payment> payments)
	{
		this.payments = payments;
	}
	
	@Override
	public int getColumnCount() 
	{
		return this.columnNames.length;
	}
	
	@Override
	public String getColumnName(int index) 
	{
		return this.columnNames[index];
	}

	@Override
	public int getRowCount() 
	{
		 return this.payments.size();
	}

	@Override
	public Object getValueAt(int row, int column) 
	{
		if(this.payments == null || row > this.payments.size() - 1 )
		{
			return null;
		}
		
		Payment payment = this.payments.get(row);
		
		switch(column)
		{
		case COLUMN_ACCOUNT:
			
			return payment.getRecipient().getAddress();
		
		case COLUMN_ASSET:
			
			Asset asset = Controller.getInstance().getAsset(payment.getAsset());
			return asset.toString();
			
		case COLUMN_AMOUNT:
			
			return payment.getAmount().toPlainString();
			
		}
		
		return null;
	}
}
