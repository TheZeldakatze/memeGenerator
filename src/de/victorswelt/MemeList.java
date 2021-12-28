package de.victorswelt;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class MemeList extends JPanel {
	private static final long serialVersionUID = 1L;
	
	MemeGenerator memeGenerator;
	MemeFrame memeFrame;
	private JList<MemeTemplate> list;
	private DefaultListModel<MemeTemplate> model;
	
	public MemeList(MemeGenerator main, MemeFrame memeFrame, List<MemeTemplate> memeList) {
		memeGenerator = main;
		this.memeFrame = memeFrame;
		list = new JList<MemeTemplate>();
		list.setCellRenderer(new MemeListCellRenderer());
		model = new DefaultListModel<MemeTemplate>();
		list.setModel(model);
		
		for(MemeTemplate mt : memeList) {
			model.addElement(mt);
		}
		
		list.addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				memeFrame.setTemplate(list.getSelectedValue());
			}
		});
		
		JFrame frame = new JFrame("Meme list");
		frame.getContentPane().add(list);
		frame.setSize(200, 360);
		frame.setVisible(true);
		frame.setLocationRelativeTo(memeFrame);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowListener() {
			
			@Override
			public void windowOpened(WindowEvent e) {}
			
			@Override
			public void windowIconified(WindowEvent e) {}
			
			@Override
			public void windowDeiconified(WindowEvent e) {}
			
			@Override
			public void windowDeactivated(WindowEvent e) {}
			
			@Override
			public void windowClosing(WindowEvent e) {
				memeGenerator.requestQuit(null);
			}
			
			@Override
			public void windowClosed(WindowEvent e) {}
			
			@Override
			public void windowActivated(WindowEvent e) {}
		});
	}
}

class MemeListCellRenderer extends DefaultListCellRenderer {
	private static final long serialVersionUID = 1L;
	
	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		MemeTemplate template = (MemeTemplate) value;
		label.setIcon(template.getPreviewIcon());
		label.setVerticalTextPosition(JLabel.CENTER);
		label.setHorizontalTextPosition(JLabel.RIGHT);
		label.setText(template.getTitle());
		label.setForeground(Color.BLACK);
		return label;
	}
}