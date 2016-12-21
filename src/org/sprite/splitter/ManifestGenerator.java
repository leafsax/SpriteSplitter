package org.sprite.splitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;

public class ManifestGenerator {

	private static String start;
	private static String template;
	private static String end;
	static {
		try {
			start = read(ManifestGenerator.class.getResourceAsStream("manifest_starting"));
			template = read(ManifestGenerator.class.getResourceAsStream("manifest_item_template"));
			end = read(ManifestGenerator.class.getResourceAsStream("manifest_end"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	} 
	public interface IdGenerator {
		String next();
	}
	
	static String generate(Image image, List<Rectangle> rects, String filename, IdGenerator generator) {
		StringBuilder builder = new StringBuilder();
		builder.append(start);
		rects.forEach(rect -> {
			builder.append(String.format(template, generator.next(), rect.x, rect.y, rect.width, rect.height));
		});
		builder.append(String.format(end, filename, image.getBounds().width, image.getBounds().height));
		return builder.toString();
	}
	
	private static String read(InputStream resourceAsStream) throws IOException {
		BufferedReader buf = new BufferedReader(new InputStreamReader(resourceAsStream));
		String line = buf.readLine();
		StringBuilder sb = new StringBuilder();
		while(line != null){
			sb.append(line).append("\n");
			line = buf.readLine();
		}
		String fileAsString = sb.toString();

		return fileAsString;
	}

}
