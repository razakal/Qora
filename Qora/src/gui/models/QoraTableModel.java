package gui.models;

import javax.swing.table.AbstractTableModel;

import database.SortableList;

@SuppressWarnings("serial")
public abstract class QoraTableModel<T, U> extends AbstractTableModel  {

	public abstract SortableList<T, U> getSortableList();
	
}
