package de.victorswelt;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class OptionPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private OptionPanel self = this;
	
	MemeGenerator memeGenerator;
	MemeFrame memeFrame;
	Logger logger;
	
	JButton exportButton, saveButton, loadButton;
	JCheckBox showTextFieldBoxes;
	JFrame frame;
	
	ArrayList<TextPropertyField> textPropertyFields;
	private JPanel textPropertyFieldContainer;
	
	public OptionPanel(MemeGenerator main, MemeFrame memeFrame, Logger logger) {
		memeGenerator = main;
		this.memeFrame = memeFrame;
		this.logger = logger;
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		textPropertyFields = new ArrayList<TextPropertyField>();
		textPropertyFieldContainer = new JPanel();
		textPropertyFieldContainer.setLayout(new BoxLayout(textPropertyFieldContainer, BoxLayout.Y_AXIS));
		add(textPropertyFieldContainer);
		
		showTextFieldBoxes = new JCheckBox("Show text field borders");
		
		loadButton = new JButton("Load meme");
		saveButton = new JButton("Save meme");
		
		exportButton = new JButton("Export meme");
		exportButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean sucessful = false;
				while(!sucessful) {
					String s = (String) JOptionPane.showInputDialog(self, "Please enter a name for the export location (make sure it is not used yet!)", 
							"Export meme", JOptionPane.PLAIN_MESSAGE, null, null, memeFrame.getTemplate().getTitle().replace(" ", "_") + "_");
					if(s == null) return;
					
					sucessful = memeGenerator.exportCurrent(s);
					if(!sucessful)
						JOptionPane.showMessageDialog(self, "Could not export meme! Does a folder already exist?", "Error exporting meme!", JOptionPane.ERROR_MESSAGE);
				}
				
			}
		});
		
		saveButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean sucessful = false;
				while(!sucessful) {
					String s = (String) JOptionPane.showInputDialog(self, "Please enter a name for the save location (make sure it is not used yet!)", 
							"Save meme", JOptionPane.PLAIN_MESSAGE, null, null, memeFrame.getTemplate().getTitle().replace(" ", "_") + "_");
					if(s == null) return;
					
					sucessful = memeGenerator.exportCurrent(s);
					if(!sucessful)
						JOptionPane.showMessageDialog(self, "Could not save meme! Does a file already exist?", "Error save meme!", JOptionPane.ERROR_MESSAGE);
				}
				
			}
		});
		
		showTextFieldBoxes.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				memeFrame.showTextFieldBorders(showTextFieldBoxes.isSelected());
			}
		});
		
		add(saveButton);
		add(exportButton);
		add(showTextFieldBoxes);
		
		// create a frame to show everything
		frame = new JFrame("Meme settings");
		frame.getContentPane().add(this);
		frame.setSize(100, 320);
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
	
		// register it at the MemeFrame
		memeFrame.setOptionPanel(this);
		
		frame.pack();
	}



	public void refresh() {
		MemeTextField memeTextFields[] = memeFrame.getTextFields();
		
		// first, remove all entries from the textPropertyFieldContainer
		textPropertyFieldContainer.removeAll();
		
		// create new TextPropertyFields if necessary
		int required = textPropertyFields.size() - memeTextFields.length;
		while(required < 0) {
			textPropertyFields.add(new TextPropertyField(textPropertyFields.size(), memeFrame));
			required++;
		}
		
		// now add as many as necessary (old ones are kept to preserve any text inside them)
		for(int i = 0; i<memeTextFields.length; i++) {
			textPropertyFieldContainer.add(textPropertyFields.get(i));
			textPropertyFields.get(i).change();
		}
		
		// revalidate and resize
		textPropertyFieldContainer.revalidate();
		frame.pack();
	}
}

class TextPropertyField extends JPanel {
	private static final long serialVersionUID = 1L;
	int id;
	JTextField text;
	MemeFrame memeFrame;
	
	public TextPropertyField(int id, MemeFrame memeFrame) {
		this.id = id;
		this.memeFrame = memeFrame;
		
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		setBorder(BorderFactory.createTitledBorder("Text Field " + id));
		text = new JTextField();
		add(text);
		
		text.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				change();
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				change();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				change();
			}
		});
		text.addComponentListener(new ComponentListener() {
			
			@Override
			public void componentShown(ComponentEvent e) {
				change();
			}
			
			@Override
			public void componentResized(ComponentEvent e) {}
			
			@Override
			public void componentMoved(ComponentEvent e) {}
			
			@Override
			public void componentHidden(ComponentEvent e) {}
		});
	}
	
	public void change() {
		MemeTextField mtf = memeFrame.getTextFieldAt(id);
		if(mtf != null) {
			mtf.text = text.getText();
			memeFrame.repaint();
		}
	}
}