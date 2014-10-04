package gui.models;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.DefaultTableModel;

@SuppressWarnings("serial")
public class CreateOptionsTableModel extends DefaultTableModel {
	
	public CreateOptionsTableModel(Object[] objects, int i) {
		super(objects, i);
		
		this.addRow(new Object[]{""});
	}

	@Override
	public boolean isCellEditable(int row, int column)
	{
		return true;
	}
	
	@Override 
	public void setValueAt(Object aValue, int row, int column)
	{
		//IF STRING
		if(aValue instanceof String)
		{
			//CHECK IF NOT EMPTY
			if(((String) aValue).length() > 0)
			{
				//CHECK IF LAST ROW
				if(row == this.getRowCount()-1)
				{
					this.addRow(new Object[]{""});
				}
				
				super.setValueAt(aValue, row, column);
			}
		}
		else
		{
			super.setValueAt(aValue, row, column);
			
			//CHECK IF LAST ROW
			if(row == this.getRowCount()-1)
			{
				this.addRow(new Object[]{""});
			}
		}
	}
	
	public List<String> getOptions()
	{
		List<String> options = new ArrayList<String>();
		
		for(int i=0; i<this.getRowCount(); i++)
		{
			String option = String.valueOf(this.getValueAt(i, 0));
			
			if(option.length() > 0)
			{
				options.add(option);
			}
		}
		
		return options;
	}
	
}
