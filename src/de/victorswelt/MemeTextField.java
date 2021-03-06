package de.victorswelt;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;

public class MemeTextField {
	int x1, x2, y1, y2, rotation, width, height, fontSize, borderWidth, shadowSize;
	String text;
	Font font;
	Color foreground, border, shadow;
	
	
	public int getX() {
		return x1;
	}

	public int getX2() {
		return x2;
	}

	public int getY() {
		return y1;
	}

	public int getY2() {
		return y2;
	}

	public int getRotation() {
		return rotation;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public String getText() {
		return text;
	}

	public Font getFont() {
		return font;
	}

	public MemeTextField(int x1, int y1, int x2, int y2, int rotation, String text, Font font, int fontSize, int borderWidth, int shadowSize, Color foreground, Color border, Color shadow) {
		this.x1 = x1;
		this.x2 = x2;
		this.y1 = y1;
		this.y2 = y2;
		this.rotation = rotation;
		this.text = text;
		this.font = font;
		this.fontSize = fontSize;
		this.borderWidth = borderWidth;
		this.shadowSize = shadowSize;
		this.foreground = foreground;
		this.border = border;
		this.shadow = shadow;
		
		width = x2 - x1;
		height = y2 - y1;
	}
	
	public void draw(Graphics2D g, int xoff, int yoff) {
		Font derived = font.deriveFont(fontSize * 1f).deriveFont(Font.PLAIN);
		if(text.isEmpty())
			text = " ";
		AttributedString attributedText = new AttributedString(text);
		attributedText.addAttribute(TextAttribute.FONT, derived);
		//attributedText.addAttribute(TextAttribute.SIZE, fontSize);
		AttributedCharacterIterator aci = attributedText.getIterator();
		LineBreakMeasurer lineMeasurer = new LineBreakMeasurer(aci, g.getFontRenderContext());
		int beginIndex = aci.getBeginIndex(),
				endIndex = aci.getEndIndex(), yLineOff = 0;
		lineMeasurer.setPosition(beginIndex);
		
		g.setColor(foreground);
		
		while(lineMeasurer.getPosition() < endIndex) {
			TextLayout layout = lineMeasurer.nextLayout(width);
			yLineOff += layout.getAscent();
			layout.draw(g, xoff, yoff + yLineOff);
			yLineOff += layout.getAscent() + layout.getLeading();
		}
	}
	
}
