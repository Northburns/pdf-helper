package northburns.pdfboxhelper.swing;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

public class ImageAsker {
	public enum ANSWER {
		YES, NO, CANCEL
	};

	private static int[] getScaledDimensions(BufferedImage forImage) {
		final int largerDimension = 100;

		int width = forImage.getWidth() > forImage.getHeight() ? largerDimension
				: (int) (forImage.getWidth() * (((double) largerDimension) / ((double) forImage
						.getHeight())));
		int height = forImage.getHeight() > forImage.getWidth() ? largerDimension
				: (int) (forImage.getHeight() * (((double) largerDimension) / ((double) forImage
						.getWidth())));

		width = width > 0 ? width : 1;
		height = height > 0 ? height : 1;

		return new int[] { width, height };
	}

	public static ANSWER askAboutImage(BufferedImage img) {
		int[] dim = getScaledDimensions(img);
		Image rimg = img.getScaledInstance(dim[0], dim[1],
				BufferedImage.SCALE_FAST);
		ImageIcon icon = new ImageIcon(rimg);
		JLabel label = new JLabel(icon);
		int answer = JOptionPane.showConfirmDialog(null, label,
				"Retain image?", JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE, null);
		switch (answer) {
		case JOptionPane.YES_OPTION:
			return ANSWER.YES;
		case JOptionPane.NO_OPTION:
			return ANSWER.NO;
		case JOptionPane.CANCEL_OPTION:
			return ANSWER.CANCEL;
		}
		return null;
	}
	
	public static void main(String[] args) {
		try {
			BufferedImage img = ImageIO.read(new File(args[0]));
			Image rimg = img.getScaledInstance(50, 500,
					BufferedImage.SCALE_FAST);
			ImageIcon icon = new ImageIcon(rimg);
			JLabel label = new JLabel(icon);
			int answer = JOptionPane.showConfirmDialog(null, label);
			switch (answer) {
			case JOptionPane.YES_OPTION:
				System.out.println("YES");
				break;
			case JOptionPane.NO_OPTION:
				System.out.println("NO");
				break;
			case JOptionPane.CANCEL_OPTION:
				System.out.println("CANCEL");
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
