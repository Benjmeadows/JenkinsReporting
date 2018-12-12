package com.mgic.qa;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang.Validate;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.mgic.qa.MGICFileUtils;;

/**
 * @author Ben Meadows <ben_meadows@mgic.com>
 * @version 1.0 Created Date: 12/05/18
 */

public class ExcelPOI {

    HSSFWorkbook workbook;
    
    public void createExcel() throws IOException {
        InputStream isFile = this.getClass().getResourceAsStream("/reporting.xls");
        workbook = new HSSFWorkbook(isFile);
    }
  
	public boolean saveToExcel(String filePath)	throws IOException {
		Validate.notEmpty(filePath);
		
		FileOutputStream out = null;
		try {			
			out = new FileOutputStream(new File(filePath));
			workbook.write(out);
			return true;
		} finally {
			MGICFileUtils.close(out);
		}
	}
    
    public HSSFWorkbook getWorkbook(){
        return workbook;
    }
    
    public HSSFRow makeRowInSheet(int rowNum, HSSFSheet sheet){
        return sheet.createRow(rowNum);
    }
    
    public HSSFCell makeCellInRow(int cellNum, HSSFRow row){
        return row.createCell(cellNum);
    }    
}
