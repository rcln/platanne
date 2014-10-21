package fr.lipn.nlptools.utils.testing;

import java.util.ArrayList;

import fr.lipn.nlptools.utils.externprog.*;

public class ExternalProgramTest {

	static final String myText = "Where is Charlie?\nIs he here?\nNo he's not here.\n But where is he?\n Is he there?\n No Charlie's not there.\n But... Yes!\n we found him!\nWe found Charlie!\n";
	
//	static final String[] command = { "ls", "-l", "/users/moreau"}; // normal
//	static final String[] command = { "ls", "-lR", "/users/moreau/wip"}; // grosse sortie
//	static final String[] command = { "mkdir", "/wronglocation"}; //error
//	static final String[] command = { "du", "-hs", "/" }; // long (test timeout)
//	static final String[] command = { "ls", "-l", "/users/moreau", ">", "/tmp/essai"}; // redirection => ne peut pas fonctionner
	static final String[] command = { "grep", "Charlie" }; // test avec stdin 
	static final int maxtime = 20;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ArrayList<String> comm = new ArrayList<String>();
		for (String e: command) {
			comm.add(e);
		}
		System.out.println("Command = "+comm.toString());
		//ExternalProgram myprog=new ExternalProgram(comm, maxtime*1000, null, null);
		// avec stdin:
		System.out.println("Text=\n"+myText);
		ExternalProgram myprog=new ExternalProgram(comm, maxtime*1000, null, null);
		System.out.println("Starting process...");
		try {
		  //int res = myprog.run(null, new StringReaderConsumer(), new StringReaderConsumer());
		  int res = myprog.run(new StringWriterFeeder(myText), new StringReaderConsumer(), new StringReaderConsumer());
		  System.out.println("Process terminated with exit code "+res);
		  System.out.println("Reading output content:\n***\n"+((StringReaderConsumer) myprog.getLastStdoutConsumer()).getString()+"\n***\n");
		  System.out.println("Reading error content:\n***\n"+((StringReaderConsumer) myprog.getLastStderrConsumer()).getString()+"\n***\n");
		} catch (Exception e) {
			System.err.println("Error caught:");
			e.printStackTrace(System.err);
		}
		  

	}

}
