package gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;

public class PopupListener extends MouseAdapter {
	
	
	private JPopupMenu popupMenu;
	public PopupListener(JPopupMenu popupMenu)
	{
		this.popupMenu = popupMenu;
	}
	
    public void mousePressed(MouseEvent e) {
      showPopup(e);
    }
    public void mouseReleased(MouseEvent e) {
      showPopup(e);
    }
    private void showPopup(MouseEvent e) {
      if (e.isPopupTrigger()) {
        popupMenu.show(e.getComponent(), e.getX(), e.getY());
      }
    }
  }