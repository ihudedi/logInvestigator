package com.loomsystem;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LogLine {

	private boolean finished = false;
	
	private String line = null;

	private String[] relevantWordsToCheck = new String[0];

	public LogLine(String line,String[] relevantWordsToCheck) {
		this.line = line;
		this.relevantWordsToCheck = relevantWordsToCheck;
	}

	public boolean isFinished() {
		return finished;
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
	}

	public String[] getRelevantWordsToCheck() {
		return relevantWordsToCheck;
	}

	public void setRelevantWordsToCheck(String[] relevantWordsToCheck) {
		this.relevantWordsToCheck = relevantWordsToCheck;
	}

	public String getLine() {
		return line;
	}

	public void setLine(String line) {
		this.line = line;
	}

	@Override
	public String toString() {
		return String.format("finished = %s , line = %s , relevantWordsToCheck = %s ", finished,line,Stream.of(relevantWordsToCheck).collect(Collectors.toList()));
	}
	
	

}
