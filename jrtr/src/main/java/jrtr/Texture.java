package jrtr;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Declares the functionality to manage textures.
 */
public interface Texture {

	public void load(String fileName) throws IOException;

	public BufferedImage getBufferedImage();
}
