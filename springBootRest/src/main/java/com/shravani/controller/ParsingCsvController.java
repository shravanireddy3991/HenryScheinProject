package com.shravani.controller;


import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController

public class ParsingCsvController {

	
	@GetMapping()
	@RequestMapping(produces="text/html", consumes="application/x-www-form-urlencoded", method=RequestMethod.POST, value="/csv/convert")
	public ResponseEntity convertCSVToString(@RequestParam("csv") String csvBody) {		
		if(StringUtils.isEmpty(csvBody)) {
			return new ResponseEntity<>("Invalid CSV/format",HttpStatus.BAD_REQUEST);
		}else {
			String newLine = System.getProperty("line.separator");
			
			StringBuilder formattedOut = new StringBuilder();
			String[] cols;
			String[] rows = csvBody.split(newLine);
			for(String row:rows) {
				cols = extractCols(row);
				for(String col:cols) {
					formattedOut.append("[").append(col).append("] ");
				}
				formattedOut.append(newLine);
			}
			
			return new ResponseEntity<>(formattedOut.toString(),HttpStatus.OK);
		}
	}
	
	public static String[] extractCols(String row) {
		String[] cols=null;
		char[] chars=row.trim().toCharArray();
		char letter=' ';
		char valType=' ';
		boolean captureStart = false;
		boolean doCapture=false;
		StringBuilder sb = new StringBuilder();
		List<String> colList = new ArrayList<>();
		for(int i=0;i<chars.length;i++) {
			letter = chars[i];
			
			doCapture=true;
			if(letter == '\"') {
				if(i==0) {//Handle first character of the line
					valType='S';
					captureStart = true;
					doCapture=false;
				}else if(captureStart){ //Handle if value capture already started then close capture
					colList.add(sb.toString());
					captureStart = false;
					doCapture=false;
				}else { //Handle New string value capture going to start
					sb = new StringBuilder();
					captureStart=true;
					doCapture=false;
					valType='S';
				}
			}else if(letter == ',') {
				if(valType == 'S' && captureStart) {
					doCapture=true;
				}
				else if(valType == 'N') {
					colList.add(sb.toString());
					doCapture=false;
					captureStart=false;
				}
			}else if(Character.isDigit(letter) && !captureStart) {
				valType='N';
				captureStart=true;
				sb = new StringBuilder();
				doCapture=true;
			}
			
			if(doCapture) {
				sb.append(letter);
			}
		}
		
		if(doCapture && captureStart) {
			if(valType == 'N') {
				sb.append(letter);
			}
			colList.add(sb.toString());
		}
		cols = colList.toArray(new String[colList.size()]);
		return cols;
	}

	}
