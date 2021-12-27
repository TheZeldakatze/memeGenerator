
package de.victorswelt;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

public class MemeGenerator {
	public static final String DATA_DIRECTORY = "data";
	
	ArrayList<MemeTemplate> memeTemplateList;
	HashMap<String, Font> fontList;
	Logger logger;
	
	File dataDirectory, templateDirectory, outputDirectory, fontDirectory, saveDirectory;
	
	MemeFrame memeFrame;
	MemeList memeList;
	OptionPanel optionPanel;
	
	
	public MemeGenerator() {
		logger = Logger.getGlobal();
		
		// initialize the folder paths
		// TODO This should be in the executable directory
		dataDirectory = getOrCreateFolder("");
		templateDirectory = getOrCreateFolder("templates");
		fontDirectory = getOrCreateFolder("fonts");
		outputDirectory = getOrCreateFolder("output");
		saveDirectory = getOrCreateFolder("saves");
		
		// load the fonts
		fontList = loadFonts();
		
		// load the meme templates
		memeTemplateList = loadMemeTemplates();
		
		// initialize the memeFrame
		memeFrame = new MemeFrame(this, logger);
		optionPanel = new OptionPanel(this, memeFrame, logger);
		
		memeList = new MemeList(this, memeFrame, memeTemplateList);
		

		memeFrame.setTemplate(memeTemplateList.get(0));
	}
	
	public boolean exportCurrent(String name) {
		File folder = new File(outputDirectory, name);
		
		// don't overwrite stuff
		if(folder.exists())
			return false;
		folder.mkdir();
		File image = new File(folder, name + ".png"),
				altText = new File(folder, name + ".txt");
		
		// write the image
		try {
			ImageIO.write(memeFrame.getOffscreen(), "png", image);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		// write the alt text
		String altString = memeFrame.getTemplate().getAltTextTemplate();
		MemeTextField textFields[] = memeFrame.getTextFields();
		for(int i = 0; i<textFields.length; i++) {
			altString = altString.replace("$" + (i + 1), "\"" + textFields[i].getText() + "\"");
		}
		
		try {
			if(altText.createNewFile()) {
				writeTextFile(altText, altString);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public void requestQuit(Component parent) {
		int i = JOptionPane.showConfirmDialog(parent, "Do you really want to close the meme generator? Any work that was not saved will be lost!", "Really quit?", JOptionPane.YES_NO_OPTION);
		if(i == JOptionPane.YES_OPTION)
			System.exit(0);
	}
	
	public ArrayList<MemeTemplate> loadMemeTemplates() {
		ArrayList<MemeTemplate> list = new ArrayList<MemeTemplate>();
		
		// in the template folder, get every child folder
		for(File memeFolder : templateDirectory.listFiles()) {
			
			// make sure it's a directory
			if(memeFolder.isDirectory()) {
				String memeName = memeFolder.getName();
				File imageFile = new File(memeFolder, "image.png"),
						altTextFile = new File(memeFolder, "altText_de.txt"),
						fieldsTextFile = new File(memeFolder, "fields.txt");
				ArrayList<MemeTextField> textFields = new ArrayList<MemeTextField>();
				String altText;
				
				// read the field positions
				try {
					// read the field positions
					Scanner scanner = new Scanner(fieldsTextFile);
					int lineNum = 0;
					while(scanner.hasNextLine()) {
						try {
							String line = scanner.nextLine();
							lineNum++;
							
							// ignore comments
							if(line.startsWith("#"))
								continue;
							
							// each line should consist of five parts (x1, y1, x2, y2, roation)
							String split[] = line.split(" ");
							if(split.length == 12) {
								int x1 = Integer.parseInt(split[0]),
										y1 = Integer.parseInt(split[1]),
										x2 = Integer.parseInt(split[2]),
										y2 = Integer.parseInt(split[3]),
										rotation = Integer.parseInt(split[4]);
								Font font = fontList.get(split[5]);
								int fontSize = Integer.parseInt(split[6]),
										borderSize = Integer.parseInt(split[7]),
										shadowSize = Integer.parseInt(split[8]);
								Color foreground = getColor(split[9]),
										border = getColor(split[10]),
										shadow = getColor(split[11]);
								if(font != null) {
									// create the text field template
									textFields.add(new MemeTextField(x1, y1, x2, y2, rotation, "", font, fontSize, borderSize, shadowSize, foreground, border, shadow));
								}
								else
									logger.warning(fieldsTextFile.getAbsolutePath() + ":" + lineNum + ": could not find font \"" + split[5] + "\", skipping field");
								
							}
							else
								logger.warning(fieldsTextFile.getAbsolutePath() + ":" + lineNum + ": Invalid field description! skipping");
							
						} catch(NumberFormatException e) {
							logger.warning(fieldsTextFile.getAbsolutePath() + ":" + lineNum + ": not a number! skipping");
						}
						
					}
					
					// close the scanner
					scanner.close();
				} catch(IOException e) {
					logger.warning(fieldsTextFile.getAbsolutePath() + ": could not read file");
					e.printStackTrace();
				}
				
				// read the alt text
				if(!textFields.isEmpty()) {
					try {
						altText = readTextFile(altTextFile);
						
						try {
							list.add(new MemeTemplate(memeName, altText, imageFile, textFields));
							logger.info("Loaded template \"" + memeName + "\"");
						} catch(IOException e) {
							logger.warning(imageFile.getAbsolutePath() + ": error loading image! Skipping template " + memeName);
							e.printStackTrace();
						}
						
						
					} catch (FileNotFoundException e) {
						logger.warning(altTextFile.getAbsolutePath() + ": file not found");
					} catch (IOException e) {
						logger.warning(altTextFile.getAbsolutePath() + ": could not read file");
					}
				}
				else
					logger.warning(memeName + " has no text fields! skipping template");
			}
			else
				logger.warning(memeFolder.getAbsolutePath() + ": not a template folder! Please remove it from the template folder");
		}
		
		
		
		return list;
	}
	
	public Color getColor(String name) {
		try {
			Field f = Color.class.getField(name);
			return (Color) f.get(null);
		} catch (NoSuchFieldException e) {
			logger.warning("color \"" + name + "\" is not known!");
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		return Color.WHITE;
	}
	
	public HashMap<String, Font> loadFonts() {
		HashMap<String, Font> map = new HashMap<String, Font>();
		
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		for(File fontFile : fontDirectory.listFiles()) {
			try {
				String fontFileName = fontFile.getName();
				
				// get the suffix position
				int suffixPosition = fontFileName.length();
				try {suffixPosition = fontFileName.lastIndexOf(".");} catch(Exception e) {}
				
				// split the string
				String fontName = fontFileName.substring(0, suffixPosition);
				String fontSuffix = fontFileName.substring(suffixPosition, fontFileName.length());
				
				int fontType = Font.TRUETYPE_FONT;
				
				// TODO detect other font types based on the ending
				FileInputStream fis = new FileInputStream(fontFile);
				Font f = Font.createFont(Font.TRUETYPE_FONT, fis);
				fis.close();
				map.put(fontName, f);
				ge.registerFont(f);
				logger.info("loaded font " + fontName);
				
			} catch (FontFormatException e) {
				logger.warning(fontFile.getAbsolutePath() + ": Fonts have to be in trueType format for now, skipping");
			} catch (IOException e) {
				logger.warning(fontFile.getAbsolutePath() + ": could not read font");
				e.printStackTrace();
			}
		}
		
		for(String s : ge.getAvailableFontFamilyNames()) {
			//logger.info(s);
		}
		
		return map;
	}
	
	
	/**
	 * Convinience method that takes care of all the folder initialization
	 * */
	private File getOrCreateFolder(String folder) {
		String path = (folder.isEmpty() ? DATA_DIRECTORY : DATA_DIRECTORY + File.separator + folder);
		File f = new File(path);
		
		// check that the data directory is existent
		if(!f.exists()) {
			logger.severe("directory " + path + " did not exist, creating one");
			f.mkdirs();
		}
		
		// exit if it is not a directory
		if(!f.isDirectory()) {
			logger.severe(path + " is not a directory");
			System.exit(1);
		}
		
		return f;
	}
	
	private String readTextFile(File f) throws FileNotFoundException, IOException {
		String out = "";
		FileInputStream fis = new FileInputStream(f);
		while(fis.available() > 0) {
			int in = fis.read();
			if(in == -1)
				break;
			out = out + ((char) in);
		}
		fis.close();
		
		return out;
	}
	
	
	private void writeTextFile(File f, String s) throws FileNotFoundException, IOException {
		FileOutputStream fos = new FileOutputStream(f);
		fos.write(s.getBytes());
		fos.close();
	}
	
	public static void main(String args[]) {
		new MemeGenerator();
	}
}
