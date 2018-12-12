package com.mgic.qa;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.Build;
import com.offbytwo.jenkins.model.BuildWithDetails;
import com.offbytwo.jenkins.model.Job;
import com.offbytwo.jenkins.model.JobWithDetails;
import com.offbytwo.jenkins.model.TestChildReport;
import com.offbytwo.jenkins.model.TestReport;
import com.offbytwo.jenkins.model.TestResult;

//import com.mgic.qa.utilities.APIUtilities;

/**
 * Provides methods for gathering statistics from Jenkins instance. Compiles
 * gathered data into Excel file
 * 
 * @author Ben Meadows <ben_meadows@mgic.com>
 * @version 1.0 Created Date: 12/05/18
 */
public class JenkinsReporting {
	private static String jenkinsUrl;
	private static String username;
	private static String password;
	private static String excludeJobs;
	private JenkinsServer jenkins;
	private Map<String, Job> jobs;
	private List<Integer> rowsTableForJobsTable;
	private List<String> columnsTablesForJobsTable;
	private Table<Integer,String,Object> jobsTable;
	private ExcelPOI excel;
	private static final int CELLNUM0 = 0, CELLNUM1 = 1, CELLNUM2 = 2, CELLNUM3 = 3, CELLNUM4 = 4, CELLNUM5 = 5, CELLNUM6 = 6,
			CELLNUM7 = 7, CELLNUM8 = 8, CELLNUM9 = 9, CELLNUM10 = 10, CELLNUM11 = 11, CELLNUM35 = 35;
	private static final int NUM_COLUMNS_TO_AUTOSIZE = 15, DOUBLE_DIGIT_NUMBERS = 10, CLEANUP_OFFSET = 1;
	private static final Integer NUMBER_OF_BUILDS_FILTER = 20;
	private static final int TOTAL_WORKBOOK_PAGES_HARDCODED = 828, TOTAL_LINES_IN_FIRST_SHEET_HARDCODED = 830;
	private static long lastTime = -1, currentTime = -1, passedTime = -1, buildTimestamp = -1;
	private static final int CONVERT_TIME_BY_1000 = 1000, CONVERT_TIME_BY_60 = 60, CONVERT_TIME_BY_24 = 24;
	private static int successBuild = -1, seconds = 0, minutes = 0, hours = 0, days = 0;
	private static final String MIN = " min ", HR = " hr ", DAY = " days ", SECS = " sec";
	private static long fail = -1, skip = -1, total = -1, pass = -1, duration = -1;
	String hourMinSec = null;
	
	public JenkinsReporting() throws Exception {
		this.getConfigurationProperties();
		this.establishJenkinsConnection();
		this.getJenkinsJobs();
		this.createJobsTable();
		this.populateJobsTable();
	}
	
	private void establishJenkinsConnection() throws URISyntaxException {
		jenkins = new JenkinsServer(new URI(jenkinsUrl), username, password);
	}
	
	private void getConfigurationProperties() {
		jenkinsUrl = System.getProperty("qa.jenkins.dns", "");
		username = System.getProperty("qa.jenkins.username", "");
		password = System.getProperty("qa.jenkins.password", "");
		excludeJobs = System.getProperty("qa.jenkins.excludeJobs", "");		
	}
	
	private void getJenkinsJobs(){
		try {
			jobs = jenkins.getJobs();
		} catch (IOException e) {
			System.out.println("No Jobs To Report On");
		}
	}
	
	private void populateJobsTable() throws IOException {
		int tableKey = 1;
		for (Map.Entry<String, Job> job : jobs.entrySet()) {
			if(job.getValue().getName()!="JenkinsReporting") {
			JobWithDetails details = jenkins.getJob(job.getKey());
			jobsTable.put(tableKey, "Job Name", job.getValue().getName());
			jobsTable.put(tableKey, "Job Details", details);
			jobsTable.put(tableKey, "Number Of Builds", details.getLastBuild().getNumber());
			jobsTable.put(tableKey, "Builds", details.getAllBuilds());
			duration = details.getLastBuild().details().getDuration();
			jobsTable.put(tableKey, "Duration Of Last Run", duration + "ms");
			lastTime = details.getLastBuild().details().getTimestamp();
			jobsTable.put(tableKey, "Time Since Last Run", this.checkTimeSinceLastRun());
			jobsTable.put(tableKey, "Time When Job Started", this.checkTimeWhenJobRunStarted());
			jobsTable.put(tableKey, "Time When Job Ended", this.checkTimeWhenJobRunEnded());
			try {
			jobsTable.put(tableKey, "Total Number Of Tests", details.getLastBuild().getTestReport().getTotalCount());
			}catch(Exception e) {
				//DO Nothing
			}
			tableKey++;
			}
		}
	}
	
	private String checkTimeSinceLastRun() {
		currentTime = System.currentTimeMillis();
		passedTime = currentTime - lastTime;
		minutes = (int) ((passedTime / (CONVERT_TIME_BY_1000 * CONVERT_TIME_BY_60)) % CONVERT_TIME_BY_60);
		hours = (int) ((passedTime / (CONVERT_TIME_BY_1000 * CONVERT_TIME_BY_60 * CONVERT_TIME_BY_60))
				% CONVERT_TIME_BY_24);
		days = (int) ((passedTime
				/ (CONVERT_TIME_BY_1000 * CONVERT_TIME_BY_60 * CONVERT_TIME_BY_60 * CONVERT_TIME_BY_24)));
		return days + DAY + hours + HR + minutes + MIN;
	}
	
	private String checkTimeWhenJobRunEnded() {
		Calendar calendar2 = Calendar.getInstance();
		calendar2.setTimeZone(TimeZone.getDefault());
		calendar2.setTimeInMillis(lastTime + duration);
		calendar2.get(Calendar.YEAR);
		StringBuilder strBuildr2 = new StringBuilder();
		if (calendar2.get(Calendar.HOUR) == 0) {
			strBuildr2.append("12");
		} else {
			strBuildr2.append(calendar2.get(Calendar.HOUR));
		}
		strBuildr2.append(":");
		if (calendar2.get(Calendar.MINUTE) < DOUBLE_DIGIT_NUMBERS) {
			strBuildr2.append("0");
			strBuildr2.append(calendar2.get(Calendar.MINUTE));
		} else {
			strBuildr2.append(calendar2.get(Calendar.MINUTE));
		}

		if (calendar2.get(Calendar.AM_PM) == 0) {
			strBuildr2.append("am");
		} else {
			strBuildr2.append("pm");
		}
		return calendar2.get(Calendar.MONTH) + 1 + "/" + calendar2.get(Calendar.DAY_OF_MONTH) + "/"
				+ calendar2.get(Calendar.YEAR) + ", " + strBuildr2.toString();
	}
	
	private String checkTimeWhenJobRunStarted() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeZone(TimeZone.getDefault());
		calendar.setTimeInMillis(lastTime);
		calendar.get(Calendar.YEAR);
		StringBuilder strBuildr = new StringBuilder();
		if (calendar.get(Calendar.HOUR) == 0) {
			strBuildr.append("12");
		} else {
			strBuildr.append(calendar.get(Calendar.HOUR));
		}
		strBuildr.append(":");
		if (calendar.get(Calendar.MINUTE) < DOUBLE_DIGIT_NUMBERS) {
			strBuildr.append("0");
			strBuildr.append(calendar.get(Calendar.MINUTE));
		} else {
			strBuildr.append(calendar.get(Calendar.MINUTE));
		}
		if (calendar.get(Calendar.AM_PM) == 0) {
			strBuildr.append("am");
		} else {
			strBuildr.append("pm");
		}
		return calendar.get(Calendar.MONTH) + 1 + "/" + calendar.get(Calendar.DAY_OF_MONTH) + "/"
				+ calendar.get(Calendar.YEAR) + ", " + strBuildr.toString();
	}
	
	private void buildRowsTableForJobsTable() {
		int tableSeed = 1;
		rowsTableForJobsTable = new ArrayList<Integer>();
		for (Map.Entry<String, Job> job : jobs.entrySet()) {
			//Do nothing with the Job Object for now; rewrite later
			rowsTableForJobsTable.add(tableSeed);
			tableSeed++;
		}
	}
	
	@SuppressWarnings("unchecked")
	public void printBuildDetails() throws IOException {
		excel = new ExcelPOI();
		excel.createExcel();
		this.initializeExcelFile();
		Set<Integer> rowKeys = jobsTable.rowKeySet();
		for (Integer integer : rowKeys) {
			this.writeToExcel(integer);
			System.out.println("Job: " + jobsTable.get(integer, "Job Name"));
			JobWithDetails jobDetails = (JobWithDetails) jobsTable.get(integer, "Job Details");
			System.out.println("Last Build Number: " + jobDetails.getLastBuild().getNumber());
			for (Build build : (List<Build>) jobsTable.get(integer, "Builds")) {
				try {
/*					TestReport testReportResult = build.getTestReport();
					List<TestChildReport> childReports = testReportResult.getChildReports();
					for (TestChildReport testChildReport : childReports) {;
						TestResult childTestResult = testChildReport.getResult();
						System.out.println("Build: " + build.details().getDisplayName());
						System.out.println("Total: " + testReportResult.getTotalCount() 
						+ " Failed: " + childTestResult.getFailCount()
						+ " Skipped: " + childTestResult.getSkipCount()
						+ " Duration: " + childTestResult.getDuration());
					
					}*/
					
				} catch (Exception e) {
					//DO Nothing
				}
			}
		}
		this.cleanup();
		MGICFileUtils.makeDirectory("./build/JenkinsReporting");
		excel.saveToExcel("./build/JenkinsReporting/JenkinsReporting.xls");
	}
	
	private void createJobsTable() {
		this.buildRowsTableForJobsTable();
		columnsTablesForJobsTable = Lists.newArrayList("Job Name",
				"Job Details",
				"Number Of Builds", 
				"Builds",
				"Duration Of Last Run",
				"Time Since Last Run",
				"Time When Job Started",
				"Time When Job Ended",
				"Total Number Of Tests");
		jobsTable = ArrayTable.create(rowsTableForJobsTable,columnsTablesForJobsTable);
	}
	
	private void writeToExcel(Integer i) {
		HSSFRow row1 = excel.makeRowInSheet(i, getWorkbook().getSheetAt(0));

		// set the Project Name
		HSSFCell cell1 = excel.makeCellInRow(CELLNUM0, row1);
		cell1.setCellValue(jobsTable.get(i, "Job Name").toString());
		
		HSSFSheet sheet = getWorkbook().getSheetAt(i);
		HSSFRow link = excel.makeRowInSheet(0, sheet);
		HSSFCell cellHome = excel.makeCellInRow(0, link);
		cellHome.setCellValue("Back to Dashboard");
		cellHome.setCellStyle(getCellStyleForWorkbook());
		
		HSSFRow origin = excel.makeRowInSheet(1, sheet);
		
		//set other high-level build info
		HSSFCell cell2 = excel.makeCellInRow(CELLNUM1, row1);
		try {
		cell2.setCellValue(jobsTable.get(i, "Total Number Of Tests").toString());
		}catch(Exception e) {
			cell2.setCellValue("0");
		}
		
		HSSFCell cell3 = excel.makeCellInRow(CELLNUM5, row1);
		cell3.setCellValue(jobsTable.get(i, "Duration Of Last Run").toString());
		
		HSSFCell cell4 = excel.makeCellInRow(CELLNUM6, row1);
		cell4.setCellValue(jobsTable.get(i, "Time Since Last Run").toString());
		
		HSSFCell cell5 = excel.makeCellInRow(CELLNUM9, row1);
		cell5.setCellValue(jobsTable.get(i, "Time When Job Started").toString());
		
		HSSFCell cell6 = excel.makeCellInRow(CELLNUM10, row1);
		cell6.setCellValue(jobsTable.get(i, "Time When Job Ended").toString());	
		
		HSSFCell cell7 = excel.makeCellInRow(CELLNUM7, row1);
		cell7.setCellValue(jobsTable.get(i, "Number Of Builds").toString());

	}
	
	private void createBasicFrameworkOfExcelFile(HSSFRow row0) {
		HSSFCell c10 = excel.makeCellInRow(CELLNUM1, row0);
		c10.setCellValue("Total   ");
		HSSFCell c11 = excel.makeCellInRow(CELLNUM2, row0);
		c11.setCellValue("Passing   ");
		HSSFCell c12 = excel.makeCellInRow(CELLNUM3, row0);
		c12.setCellValue("Failing   ");
		HSSFCell c13 = excel.makeCellInRow(CELLNUM4, row0);
		c13.setCellValue("Skipped   ");
		HSSFCell c14 = excel.makeCellInRow(CELLNUM5, row0);
		c14.setCellValue("Duration");
		HSSFCell c15 = excel.makeCellInRow(CELLNUM6, row0);
		c15.setCellValue("Time Since Last Run");
		HSSFCell c16 = excel.makeCellInRow(CELLNUM7, row0);
		c16.setCellValue("Number Of Builds");
		HSSFCell c17 = excel.makeCellInRow(CELLNUM8, row0);
		c17.setCellValue("Last Successful Build");
		HSSFCell c18 = excel.makeCellInRow(CELLNUM9, row0);
		c18.setCellValue("Started");
		HSSFCell c19 = excel.makeCellInRow(CELLNUM10, row0);
		c19.setCellValue("Ended");
		HSSFCell c20 = excel.makeCellInRow(CELLNUM11, row0);
		c20.setCellValue("30 Day Metric");
	}
	
	private void initializeExcelFile() {
		// setup the main page
		// //set these initialization in other private method
		HSSFSheet sheet0 = getWorkbook().getSheetAt(0);
		HSSFRow row0 = excel.makeRowInSheet(0, sheet0);
		HSSFCell cell0 = excel.makeCellInRow(0, row0);
		cell0.setCellValue("JENKINS REPORTING - " + jenkinsUrl);
		cell0.setCellStyle(getCellStyleForWorkbook());
		createBasicFrameworkOfExcelFile(row0);
	}
	
	private HSSFWorkbook getWorkbook() {
		return excel.getWorkbook();
	}
	
	public HSSFCellStyle getCellStyleForWorkbook() {
		HSSFCellStyle cellStyle = getWorkbook().createCellStyle();
		HSSFFont font = getWorkbook().createFont();
		font.setBold(true);
		font.setFontName(HSSFFont.FONT_ARIAL);
		cellStyle.setFont(font);
		cellStyle.setAlignment(HorizontalAlignment.CENTER);;
		return cellStyle;
	}
	
	private void cleanup() {
		HSSFWorkbook workbook = excel.getWorkbook();
		// autosize columns
		for (int j = 0; j < TOTAL_WORKBOOK_PAGES_HARDCODED; j++) {
			for (int op = 0; op < NUM_COLUMNS_TO_AUTOSIZE; op++) {
				workbook.getSheetAt(j).autoSizeColumn(op, true);
			}
		}
		for (int i = TOTAL_WORKBOOK_PAGES_HARDCODED; i > jobs.size() - 1; i--) {
			workbook.removeSheetAt(i);
		}

		HSSFSheet sheet = workbook.getSheetAt(0);
		for (int i = jobs.size() - 1 + CLEANUP_OFFSET; i < TOTAL_LINES_IN_FIRST_SHEET_HARDCODED; i++) {
			HSSFRow row = sheet.getRow(i);
			sheet.removeRow(row);
		}
	}
	
}
