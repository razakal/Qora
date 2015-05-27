package utils;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;
import javax.swing.JTable;

public class TableMenuPopupUtil {
	// https://github.com/jrwalsh/CycTools/blob/master/src/edu/iastate/cyctools/externalSourceCode/MenuPopupUtil.java
	// http://www.coderanch.com/t/346220/GUI/java/Copy-paste-popup-menu
	public static void installContextMenu(final JTable component, final JPopupMenu menu) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}

			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}

			public void showMenu(final MouseEvent e) {
				if (e.isPopupTrigger()) {
					if(!component.isEnabled())
					{
						return;
					}
				
					Point p = e.getPoint();
				    int row = component.rowAtPoint(p);
				    component.setRowSelectionInterval(row, row);
				      
					menu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});
	}

	public static void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}

			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}

			private void showMenu(MouseEvent e) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}
}