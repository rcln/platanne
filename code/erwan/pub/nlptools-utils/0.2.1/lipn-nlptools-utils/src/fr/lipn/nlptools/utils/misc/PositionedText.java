package fr.lipn.nlptools.utils.misc;

/**
 * Simple class for text+position objects
 * @author moreau
 *
 */
public class PositionedText {
	String text;
	long start;
	long end;
	
	public PositionedText(String text, long start, long end) {
		this.text = text;
		this.start = start;
		this.end = end;
	}

	public long getEnd() {
		return end;
	}

	public void setEnd(long end) {
		this.end = end;
	}

	public long getStart() {
		return start;
	}

	public void setStart(long start) {
		this.start = start;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
}
