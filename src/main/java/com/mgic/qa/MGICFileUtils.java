package com.mgic.qa;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Collection;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.Validate;
import com.sun.mail.util.BASE64DecoderStream;

/**
 * Provides helper methods to work with files on the local filesystem.
 * 
 * @author Meadows <Ben_Meadows@mgic.com>
 */

public final class MGICFileUtils {

	// list of extensions of files that are used. If file's extension isn't in
	// the list file will not be handled
	private static final String[] EXTENSIONS = new String[] { "txt", "xml", "xls", "xlsx", "doc", "docx", "pdf", "exe",
			"log", "dat" };

	private MGICFileUtils() {
	}

	/**
	 * 
	 * Returns a string of the contents of the file.
	 * <p>
	 * 
	 * @param filePath
	 *            accepts a String to a File's location, to read and return
	 * @return String
	 * @throws IOException
	 */
	public static String readFileToString(String filePath) throws IOException {
		Validate.notNull(filePath);
		return FileUtils.readFileToString(new File(filePath));
	}

	/**
	 * 
	 * Takes a filepath and opens an inputstream from that file
	 * <p>
	 * you can use the inputstream to funnel data where it is needed
	 * <p>
	 * Make sure to always call .close() on the stream you create
	 * 
	 * @param filePath
	 *            takes a Files location as a string, and opens an InputStream
	 *            to it
	 * @return InputStream (open)
	 * @throws IOException
	 *             in case the file does not exist
	 */
	// Make sure to always call .close() on the stream you create
	public static InputStream readFileToInputStream(String filePath) throws IOException {
		Validate.notNull(filePath);
		return FileUtils.openInputStream(new File(filePath));
	}

	/**
	 * 
	 * takes input from an InputStream and returns the data within it, in String
	 * form
	 * <p>
	 * 
	 * @param stream
	 *            accepts an InputStream (directed/attached to a file)
	 * @return String
	 * @throws IOException
	 */
	public static String readInputStreamToString(InputStream stream) throws IOException {
		Validate.notNull(stream);
		StringWriter writer = new StringWriter();
		IOUtils.copy(stream, writer);
		stream.close();
		return writer.toString();
	}

	/**
	 * 
	 * Takes input from a FileInputStream (example from a file as part of a
	 * JavaMail Message BodyPart) and returns the data encoded in a Base64
	 * String form. Used together with writeBase64StringToFile to decode the
	 * string and save to file.
	 * <p>
	 * 
	 * @param stream
	 *            accepts a FileInputStream
	 * @return String
	 * @throws IOException
	 */
	public static String readFileInputStreamToBase64String(FileInputStream stream) throws IOException {
		Validate.notNull(stream);
		return Base64.encodeBase64String(IOUtils.toByteArray(stream));
	}

	/**
	 * 
	 * Takes an outputStream, returns whats in it.
	 * <p>
	 * 
	 * @param stream
	 *            an OutputStream (attached to a file) for writing to the file
	 * @return String representation of what should be pushed into the file via
	 *         the stream
	 * @throws IOException
	 */
	public static String readOutputStreamToString(OutputStream stream) throws IOException {
		Validate.notNull(stream);
		ByteArrayOutputStream os = (ByteArrayOutputStream) stream;
		return new String(os.toByteArray());
	}

	/**
	 * 
	 * Takes a BASE64DecoderStream, returns whats in it.
	 * <p>
	 * 
	 * @param stream
	 *            a BASE64DecoderStream containg an encoded image or other data
	 * @return a BASE64 String representation of that data.
	 * @throws IOException
	 */
	public static String readBase64DecoderStreamToBase64String(BASE64DecoderStream stream) throws IOException {
		Validate.notNull(stream);
		byte[] byteArray = IOUtils.toByteArray(stream);
		byte[] encodeBase64 = Base64.encodeBase64(byteArray);
		return new String(encodeBase64, "UTF-8");
	}

	/**
	 * 
	 * Creates a directory
	 * <p>
	 * 
	 * @param filePath
	 *            that is a soon-to-be-created directory (folder)
	 * @throws IOException
	 *             (in case the path cannot b written to/ect)
	 */
	public static void makeDirectory(String filePath) throws IOException {
		Validate.notNull(filePath);
		FileUtils.forceMkdir(new File(filePath));
	}

	/**
	 *
	 * Deletes everything from directory
	 * <p>
	 *
	 * @param filePath
	 *            that is a directory (folder) that will be cleaned up
	 * @throws IOException
	 *             (in case the path cannot be written to/ect)
	 */
	public static void cleanDirectory(String filePath) throws IOException {
		Validate.notNull(filePath);
		FileUtils.cleanDirectory(new File(filePath));
	}

	/**
	 *
	 * Deletes a directory even if it is not empty
	 * <p>
	 *
	 * @param filePath
	 *            that is a directory (folder) that will be deleted
	 * @throws IOException
	 *             (in case the path cannot be written to/ect)
	 */
	public static void deleteDirectory(String filePath) throws IOException {
		Validate.notNull(filePath);
		FileUtils.deleteDirectory(new File(filePath));
	}

	/**
	 * 
	 * Takes a string, puts it into a file of your choosing
	 * <p>
	 * 
	 * @param file
	 *            a File that you want to write to
	 * @param data
	 *            a string of what you want to output to the file
	 * @throws IOException
	 *             (in case the directory to be written to doesn't exist)
	 */
	public static void writeStringOutputToFile(File file, String data) throws IOException {
		Validate.notNull(file);
		FileUtils.writeStringToFile(file, data);
	}

	/**
	 *
	 * Takes a string, appends/rewrite it into a file of your choosing
	 *
	 * @param file
	 *            a File that you want to write to
	 * @param data
	 *            a string of what you want to output to the file
	 * @param append
	 *            a bool variable(true/false) which indicates possibility to
	 *            append to file
	 * @throws IOException
	 *             (in case the directory to be written to doesn't exist)
	 */
	public static void writeStringOutputToFile(File file, String data, boolean append) throws IOException {
		Validate.notNull(file);
		FileUtils.writeStringToFile(file, data, append);
	}

	/**
	 * 
	 * Takes a String decoded to BASE64 and a file, saves the data to that file.
	 * <p>
	 * 
	 * @param file
	 *            a File that you want to write to
	 * @param data
	 *            a string of what you want to ouput to the file
	 * @throws IOException
	 */
	public static void writeBase64StringToFile(File file, String data) throws IOException {
		Validate.notNull(file);
		Validate.notNull(data);
		byte[] dearr = Base64.decodeBase64(data);
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(dearr);
		fos.close();
	}

	/**
	 * 
	 * takes 2 parameters, first is a file you want to copy, second is the file
	 * you want to put the copy in
	 * <p>
	 * 
	 * @param file
	 *            the file you want to copy (from)
	 * @param file2
	 *            a file you want to paste to (copy of 'file')
	 * @throws IOException
	 */
	public static void copyFile(File file, File file2) throws IOException {
		Validate.notNull(file);
		Validate.notNull(file2);
		FileUtils.copyFile(file, file2);
	}

	/**
	 * Converts given string into ByteArrayInputStream.
	 * 
	 * @param stringToConvert
	 *            String to convert to ByteArrayInputStream
	 * @return
	 */
	public static ByteArrayInputStream convertStringToInputStream(String stringToConvert) {
		Validate.notNull(stringToConvert);
		return new ByteArrayInputStream(Charset.forName("UTF-8").encode(stringToConvert).array());
	}

	/**
	 *
	 * Takes a filepath to a file and converts it to a FileOutputStream
	 * <p>
	 *
	 * @param filePath
	 *            that is a path to the file we need
	 * @return Collection<File>
	 * @throws FileOutputStream
	 * @throws IOException
	 *             (in case the path cannot be written to/ect)
	 */
	public static FileOutputStream getFileOutputStream(String filePath) throws FileNotFoundException {
		return new FileOutputStream(new File(filePath));
	}

	/**
	 *
	 * Takes a filepath to a folder where we are looking for files
	 * <p>
	 *
	 * @param filePath
	 *            that is a directory (folder) from where we take files
	 * @return Collection<File>
	 * @throws IOException
	 *             (in case the path cannot be written to/ect)
	 */
	public static Collection<File> getFiles(String filePath) throws IOException {
		Validate.notNull(filePath);
		return FileUtils.listFiles(new File(filePath), EXTENSIONS, false);
	}

	/**
	 *
	 * Takes a filepath to a folder where we are looking for files
	 * <p>
	 *
	 * @param filePath
	 *            that is a directory (folder) from where we take files
	 * @param extension
	 *            is a downloaded file's extension
	 * @return Collection<File>
	 * @throws IOException
	 *             (in case the path cannot be written to/ect)
	 */
	public static Collection<File> getFiles(String filePath, String extension) throws IOException {
		Validate.notNull(filePath);
		String[] extensions = new String[] { extension };
		return FileUtils.listFiles(new File(filePath), extensions, false);
	}

	/**
	 * Saves image to given directory. Image is saved with random name
	 * 
	 * @param image Image to save
	 * @param folderPath Path where to save the image
	 * @return File with the saved image
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static File saveImage(BufferedImage image, String folderPath) throws IOException, InterruptedException {
		File file = new File(folderPath + File.separator + System.currentTimeMillis() + ".png");
		ImageIO.write(image, "png", file);
		return file;
	}

	/**
	 * Helper method that closes any object, that inherits from Closeable (most
	 * often applied to data streams)
	 * 
	 * @param closeable
	 *            Closeable object
	 * @throws IOException
	 */
	public static void close(Closeable closeable) throws IOException {
		if (closeable != null) {
			closeable.close();
		}
	}

	public static String addTrailingSlash(String path) {
		String result;
		if (path.endsWith("/")) {
			result = path;
		} else {
			result = path + "/";
		}
		return result;
	}
}