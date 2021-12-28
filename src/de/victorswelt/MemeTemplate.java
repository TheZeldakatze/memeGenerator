package de.victorswelt;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class MemeTemplate {
	private String title, altTextTemplate;
	private File imageFile;
	private ImageIcon previewIcon;
	private Image image;
	private MemeTextField textFieldTemplates[];
	
	public MemeTemplate(String title, String altTextTemplate, File imageFile, List<MemeTextField> textFields) throws IOException {
		this.title = title;
		this.altTextTemplate = altTextTemplate;
		this.imageFile = imageFile;
		image = ImageIO.read(imageFile);
		previewIcon = new ImageIcon(image.getScaledInstance(32, 32, Image.SCALE_SMOOTH));
		textFieldTemplates = new MemeTextField[textFields.size()];
		for(int i = 0; i< textFields.size(); i++) textFieldTemplates[i] = textFields.get(i);
		
		unloadImage();
	}
	
	public MemeTextField[] createTextFields() {
		return textFieldTemplates.clone();
	}

	public String getTitle() {
		return title;
	}

	public String getAltTextTemplate() {
		return altTextTemplate;
	}

	public ImageIcon getPreviewIcon() {
		return previewIcon;
	}

	public Image getImage() {
		if(image == null)
			try {
				image = ImageIO.read(imageFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		return image;
	}
	
	public void unloadImage() {
		image.flush();
		image = null;
	}
	
	public String toString() {
		return title;
	}
}
