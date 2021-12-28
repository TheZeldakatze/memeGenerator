package de.victorswelt;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class MemeFrame extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	MemeGenerator memeGenerator;
	OptionPanel optionPanel;
	Logger logger;
	JFrame frame;
	MemeTemplate currentTemplate;
	BufferedImage offscreen;
	Image background;
	private MemeTextField textFields[];
	private boolean showTextFieldBorders;
	
	public MemeFrame(MemeGenerator main, Logger l) {
		memeGenerator = main;
		logger = l;
		frame = new JFrame("Meme Editor");
		frame.getContentPane().add(this);
		frame.setSize(640, 480);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.setVisible(true);
		frame.setLocationRelativeTo(null);
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
	
	public void setTemplate(MemeTemplate mt) {
		if(currentTemplate == mt)
			return;
		
		if(currentTemplate != null)
			currentTemplate.unloadImage();
		currentTemplate = mt;
		
		// clear the old offscreen
		if(offscreen != null)
			offscreen.flush();
		offscreen = null;
		
		background = currentTemplate.getImage();
		offscreen = new BufferedImage(background.getWidth(null), background.getHeight(null), BufferedImage.TYPE_INT_RGB);
		textFields = currentTemplate.createTextFields();
		frame.setSize(offscreen.getWidth(null), offscreen.getHeight(null));
		repaint();
		
		// refresh the option panel
		if(optionPanel != null)
			optionPanel.refresh();
	}
	
	public MemeTemplate getTemplate() {
		return currentTemplate;
	}
	
	public void setOptionPanel(OptionPanel op) {
		optionPanel = op;
	}
	
	public MemeTextField[] getTextFields() {
		return textFields;
	}
	
	public void showTextFieldBorders(boolean b) {
		showTextFieldBorders = b;
		repaint();
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		// check that there even is an offscreen
		if(offscreen != null) {
			Graphics2D offscreenGraphics = (Graphics2D) offscreen.getGraphics();
			
			// draw the background
			offscreenGraphics.drawImage(background, 0, 0, null);
			
			// draw the text fields
			AffineTransform oldTransform = offscreenGraphics.getTransform();
			for(MemeTextField mtf : textFields) {
				
				// rotate if necessary
				int x;
				int y;
				if(mtf.getRotation() != 0) {
					AffineTransform at = new AffineTransform(oldTransform);
					at.translate(mtf.getX(), mtf.getY());
					AffineTransform at2 = new AffineTransform();
					at2.translate(mtf.getWidth()/2, mtf.getHeight()/2);
					at2.rotate(Math.toRadians(mtf.rotation));
					at2.translate(-mtf.getWidth()/2, -mtf.getHeight()/2);
					at.concatenate(at2);
					offscreenGraphics.setTransform(at);
					x = 0;
					y = 0;
				}
				else {
					x = mtf.getX();
					y = mtf.getY();
				}
				
				// draw a rectangle to show the size of the field
				if(showTextFieldBorders) {
					offscreenGraphics.setColor(Color.RED);
					offscreenGraphics.drawRect(x, y, mtf.getWidth(), mtf.getHeight());
				}
				
				// draw the memeTextField
				mtf.draw(offscreenGraphics, x, y);
				
				// reset the rotation
				if(mtf.getRotation() != 0)
					offscreenGraphics.setTransform(oldTransform);
			}
			
			// dispose of the graphics
			offscreenGraphics.dispose();
			
			// draw the offscreen
			g.drawImage(offscreen, 0, 0, getWidth(), getHeight(), null);
		}
	}
	
	public MemeTextField getTextFieldAt(int id) {
		if(id<0 || id >= textFields.length)
			return null;
		return textFields[id];
	}
	
	public BufferedImage getOffscreen() {
		return offscreen;
	}
}
