package jrtr.swrenderer;

import java.awt.image.BufferedImage;
import java.io.*;

import javax.imageio.ImageIO;

import jrtr.Texture;

/**
 * Manages textures for the software renderer. Not implemented here.
 */
public class SWTexture implements Texture {
	public BufferedImage texture;
	
	public void load(String fileName) throws IOException {
		File f = new File(fileName);
		texture = ImageIO.read(f);
	}
	
	public BufferedImage getBufferedImage() {
		return texture;
	}
}
