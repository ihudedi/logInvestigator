package com.loomsystem;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LogInvestigation {

	public static String logFileName = "/logFile";

	private Map<Integer, List<LogLine>> logLines = null;
	
	private static int SENTENCE_NOT_SIMILAR  = -1;

	private boolean alreadyPrintedSimilarSentence = false;

	public static void main(String[] args) throws Exception {
		LogInvestigation logInvestigation = new LogInvestigation();
		logInvestigation.extactAllLogLines();
		logInvestigation.printAllSimilarSentences();
	}

	
	/**
	 * This method all similar sentences
	 */
	private void printAllSimilarSentences() {
		if (logLines.size() > 0)
			System.out.println("=====");
		logLines.keySet().stream().forEach(x -> printSimilarSenetencesForWordCount(x));
		if (logLines.size() > 0)
			System.out.println("=====");
	}

	
	/**
	 * This methos print the similar sentences for same numOfWords
	 * @param numOfWords - num of words in a sentence - without the date format
	 */
	private void printSimilarSenetencesForWordCount(Integer numOfWords) {

		List<LogLine> logLinesForNumWords = logLines.get(numOfWords);
		int currentlineNo = 0;
		int workingLineNo = 0;
		int indexDiffer = -1;
		int[] lineNoAndWorkingLineNo = fetchNextLineNoAndWorkingLineNo(logLinesForNumWords, currentlineNo, workingLineNo);
		if(lineNoAndWorkingLineNo== null)
			return;
		
		while (true) {
			LogLine currentLogLine = logLinesForNumWords.get(currentlineNo);
			LogLine workingLogLine = logLinesForNumWords.get(workingLineNo);
			indexDiffer = checkSimiliarSenetece(currentLogLine.getRelevantWordsToCheck(),
					workingLogLine.getRelevantWordsToCheck());
			if (indexDiffer > SENTENCE_NOT_SIMILAR) {
				if (alreadyPrintedSimilarSentence) {
					System.out.println("\n");
				}
				System.out.println(currentLogLine.getLine());
				System.out.println(workingLogLine.getLine());
				System.out.println(String.format("The changing word was: %s, %s",
						         currentLogLine.getRelevantWordsToCheck()[indexDiffer],
								workingLogLine.getRelevantWordsToCheck()[indexDiffer]));
				currentLogLine.setFinished(true);
				workingLogLine.setFinished(true);
				alreadyPrintedSimilarSentence = true;
			}
			lineNoAndWorkingLineNo = fetchNextLineNoAndWorkingLineNo(logLinesForNumWords, currentlineNo, workingLineNo);
			if (lineNoAndWorkingLineNo != null) {
				currentlineNo = lineNoAndWorkingLineNo[0];
				workingLineNo = lineNoAndWorkingLineNo[1];
			} else
				break;

		}
	}

	/**
	 * This method return the next lineNo and next workingLineNo to check sentences
	 * @param logLinesForNumWords - list all lines with numOfWords
	 * @param currentLineNo
	 * @param workingLineNo
	 * @return
	 */
	private int[] fetchNextLineNoAndWorkingLineNo(List<LogLine> logLinesForNumWords, int currentLineNo, int workingLineNo) {

		workingLineNo++;
		//According to the output you sent me after I found 2 sentences similar I start search from the last line +1 but I should
		//restart from the last sentence that wasn't used.
		//According to your input the result should be 1+3,2+5 and not 1+3,4+5
		//In case you want this uncomment out those lines.
		if (logLinesForNumWords.get(currentLineNo).isFinished()) {
			currentLineNo = workingLineNo;
			workingLineNo++;
		}
		int numOfLines = logLinesForNumWords.size();
		while (true) {
			
			// if we didn't arrived to the last line of the group and the line is already finished we move to the next line
			while (currentLineNo < numOfLines-1 && logLinesForNumWords.get(currentLineNo).isFinished()) {
				currentLineNo++;
				workingLineNo = currentLineNo + 1;
			}

			// if we arrived to the last line or beyond means we don't have any line to check with , return null
			if (currentLineNo >= numOfLines-1)
				return null;

			
			// if we didn't arrive to the last line and the line is finished move to the next line for line to be checked with the lineNo
			while (workingLineNo < numOfLines && logLinesForNumWords.get(workingLineNo).isFinished()) {
				workingLineNo = workingLineNo + 1;
			}

			// if we didn't pass the size we return both lineNo and workingLineNo
			if (workingLineNo < numOfLines)
				return new int[] { currentLineNo, workingLineNo };

			//if we passed the last line we jump to next lineNo
			if (workingLineNo >= numOfLines) {
				currentLineNo++;
				workingLineNo = currentLineNo + 1;
			}

		}

	}

	
	/**
	 * This method check if 2 lines are similar by one word change.
	 * @param lineWords
	 * @param workingLineWords
	 * @return
	 */
	private int checkSimiliarSenetece(String[] lineWords, String[] workingLineWords) {
		int indexDiffer = -1;
		int countDiffer = 0;
		for (int i = 0; i < lineWords.length; i++) {
			if (!lineWords[i].equals(workingLineWords[i])) {
				indexDiffer = i;
				countDiffer++;
			}
			if (countDiffer > 1)
				break;
		}
		if (countDiffer == 1)
			return indexDiffer;
		else
			return SENTENCE_NOT_SIMILAR;
	}

	
	/**
	 * This method extract all lines as raw data from given file and build a map of numOfWords->list of all lines parsed by the words themselves
	 */
	private void extactAllLogLines() {

		String line = null;
		List<String> lines = new ArrayList<String>();
		
		//first get all lines as raw data from the logFile
		try (InputStream is = getClass().getResourceAsStream(logFileName);
				BufferedReader br = new BufferedReader(new InputStreamReader(is));) {
			while (null != (line = br.readLine())) {
				lines.add(line);
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	      
	      
	    // This code is working only by running here not from jar outside  
		/*
		 File file = null;
		 try {
			//uri = this.getClass().getResource(logFileName).toURI();
			file = new File(this.getClass().getResource(logFileName).getFile());
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			lines = Files.readAllLines(file.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}*/

		
		 /*System.out.println(lines.stream() .map(s -> convertLineToLogLine(s))
		                                   .sorted(Comparator.comparing(c ->
		                                   c.getRelevantWordsToCheck().length)).collect(Collectors.groupingBy(g-> g.getRelevantWordsToCheck().length))
		                                  .entrySet().stream().filter(f-> f.getValue().size()>1).collect(Collectors.toMap(e ->
		                                   e.getKey(),e->e.getValue())));*/
		
		
		//build the logLines as map of <Integer,List<LogLine>
		logLines = lines.stream().map(s -> convertLineToLogLine(s))
				.sorted(Comparator.comparing(c -> c.getRelevantWordsToCheck().length))
				.collect(Collectors.groupingBy(g -> g.getRelevantWordsToCheck().length)).entrySet().stream()
				.filter(f -> f.getValue().size() > 1).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
		
		// If we want to do it parallel but the order of the sentences and the output will be different we will use as follows:
		/*logLines = lines.stream().map(s -> convertLineToLogLine(s))
				.sorted(Comparator.comparing(c -> c.getRelevantWordsToCheck().length))
				.collect(Collectors.groupingByConcurrent(g -> g.getRelevantWordsToCheck().length)).entrySet().stream()
				.filter(f -> f.getValue().size() > 1).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));*/
		
	}

	private LogLine convertLineToLogLine(String line) {

		Pattern pattern = Pattern.compile("^\\S* \\S* (.*)$");
		Matcher matcher = pattern.matcher(line);
		if (matcher.matches())
			return new LogLine(line, matcher.group(1).split(" "));
		return new LogLine(null, new String[] {});
	}

}
