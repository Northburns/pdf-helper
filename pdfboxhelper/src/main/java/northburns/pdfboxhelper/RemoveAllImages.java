package northburns.pdfboxhelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import northburns.pdfboxhelper.swing.ImageAsker;
import northburns.pdfboxhelper.swing.ImageAsker.ANSWER;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.exceptions.CryptographyException;
import org.apache.pdfbox.exceptions.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectForm;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;

public class RemoveAllImages {

	public static void main(String[] argv) throws COSVisitorException,
			InvalidPasswordException, CryptographyException, IOException {
		RemoveAllImages self = new RemoveAllImages();
		self.main();
	}

	int saveEveryXImage;
	int imagesProcessedSinceLastSave;
	File _targetFile;
	PDDocument document;
	String processedPagesFileName;
	Collection<Integer> processedPages = new ArrayList<Integer>();
	private int answer;

	private void main() throws COSVisitorException, InvalidPasswordException,
			CryptographyException, IOException {

		JFileChooser chooser = new JFileChooser();
		try {
			chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
		} catch (Exception e) {
			// ignored
			e.printStackTrace();
		}
		chooser.showOpenDialog(null);

		document = PDDocument.load(chooser.getSelectedFile());

		if (document.isEncrypted()) {
			document.decrypt("");
		}
		document.setAllSecurityToBeRemoved(true);

		chooser.showSaveDialog(null);
		_targetFile = chooser.getSelectedFile();

		processedPagesFileName = _targetFile.getAbsoluteFile()
				+ ".processedpages";
		try {
			processedPages.clear();
			ObjectInputStream processedPagesInputStream = new ObjectInputStream(
					new FileInputStream(processedPagesFileName));
			Object o = processedPagesInputStream.readObject();
			if (o instanceof Iterable) {
				for (Object oo : (Iterable<?>) o) {
					if (oo instanceof Integer) {
						processedPages.add((Integer) oo);
					}
				}
			}
			processedPagesInputStream.close();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			processedPages = new ArrayList<Integer>();
		}

		saveEveryXImage = Integer
				.valueOf(JOptionPane
						.showInputDialog("Save every X images: (0 == only at the end)"));
		imagesProcessedSinceLastSave = 0;

		this.answer = JOptionPane.showOptionDialog(null, null, "Remove all?",
				JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null,
				new Object[] { "Remove all", "Ask for each" }, null);
		System.out.println("answer = " + answer);
		// showConfirmDialog(null, label,
		// "Retain image?", JOptionPane.YES_NO_CANCEL_OPTION,
		// JOptionPane.QUESTION_MESSAGE, null);

		PDDocumentCatalog catalog = document.getDocumentCatalog();
		@SuppressWarnings("unchecked")
		List<Object> allPages = catalog.getAllPages();
		for (Object pageObj : allPages) {
			int pageNumber = allPages.indexOf(pageObj) + 1;
			if (processedPages.contains(pageNumber)) {
				continue;
			}
			PDPage page = (PDPage) pageObj;
			PDResources resources = page.findResources();
			processResources(resources);
			processedPages.add(pageNumber);
		}
		System.gc();
		if (imagesProcessedSinceLastSave == 0) {
			System.out.println("All done. Not saving, just did.");
		} else {
			System.out.println("All done. Saving file.");
			FileOutputStream targetFile = new FileOutputStream(_targetFile);
			document.save(targetFile);
			targetFile.close();
			
			ObjectOutputStream out = new ObjectOutputStream(
					new FileOutputStream(processedPagesFileName));
			out.writeObject(processedPages);
			out.close();
		}
		document.close();

	}

	private void processResources(PDResources resources)
			throws COSVisitorException, IOException {
		Map<String, PDXObject> xObjects = resources.getXObjects();
		for (Iterator<Entry<String, PDXObject>> imgIterator = xObjects
				.entrySet().iterator(); imgIterator.hasNext();) {
			Entry<String, PDXObject> next = imgIterator.next();
			PDXObject obj = next.getValue();
			if (obj instanceof PDXObjectImage) {
				PDXObjectImage img = (PDXObjectImage) obj;
				System.out.println(img.getHeight());
				if (img.getWidth() > 0 && img.getHeight() > 0) {
					System.gc();
					// Operate only visible images
					ANSWER askAboutImage = answer == 0 ? ANSWER.NO : ImageAsker
							.askAboutImage(img.getRGBImage());
					switch (askAboutImage) {
					case YES:
						break;
					case NO:
						imgIterator.remove();
						break;
					case CANCEL:
						System.out.println("Cancelled by user.");
						throw new RuntimeException("Cancelled by user.");
					}
					img = null;
					System.gc();
					imagesProcessedSinceLastSave++;
					if (saveEveryXImage != 0
							&& imagesProcessedSinceLastSave >= saveEveryXImage) {
						System.out.println("Saving file.");
						resources.setXObjects(xObjects);
						FileOutputStream targetFile = new FileOutputStream(
								_targetFile);
						document.save(targetFile);
						targetFile.close();

						ObjectOutputStream out = new ObjectOutputStream(
								new FileOutputStream(processedPagesFileName));
						out.writeObject(processedPages);
						out.close();

						imagesProcessedSinceLastSave = 0;
					}
				}
			}
			// maybe there are more images embedded in a form object
			else if (obj instanceof PDXObjectForm) {
				PDXObjectForm xObjectForm = (PDXObjectForm) obj;
				PDResources formResources = xObjectForm.getResources();
				processResources(formResources);
			}
		}
		resources.setXObjects(xObjects);
	}
}
