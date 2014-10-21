package fr.lipn.nlptools.utils.misc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class FileUtil {

	
	/**
	 * Creates a temporary directory (the safe way). Works exactly like <code>File.createTempFile</code>, see Java API.
	 * @param prefix dir name prefix
	 * @param suffix dir name suffix
	 * @param inThisDir directory where this temp dir will be created, or null to create in the system standard temp location.
	 * @return the directory.
	 * @throws IOException
	 */
	public static File createTempDir(String prefix, String suffix, File inThisDir) throws IOException {
		File temp = File.createTempFile(prefix, suffix, inThisDir);

	    if(!(temp.delete())) {
	        throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
	    }

	    if(!(temp.mkdir())) {
	        throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
	    }
	    return temp;

	}
	
	// Copied from http://www.rgagnon.com/javadetails/java-0064.html
	public static void copyFile(File in, File out) throws IOException {
		FileChannel inChannel = new FileInputStream(in).getChannel();
		FileChannel outChannel = new FileOutputStream(out).getChannel();
		try {
			inChannel.transferTo(0, inChannel.size(), outChannel);
		} 
		catch (IOException e) {
			throw e;
		}
		finally {
			if (inChannel != null) inChannel.close();
			if (outChannel != null) outChannel.close();
		}
	}


	public static boolean recDeleteDir(File path)  {
		if( path.exists() ) {
			File[] files = path.listFiles();
			for(int i=0; i<files.length; i++) {
				if(files[i].isDirectory()) {
					recDeleteDir(files[i]);
				}
				else {
					files[i].delete();
				}
			}
		}
		return( path.delete() );
	}

	
}
