package com.music.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


public class ExcelReader {

	/*
	 * 完成对excel文件的读取
	 * 
	 * @method ExcelReader构造函数，读取excel文件内容到workbook中 useSheet读取指定sheet页
	 * close完成文件读取，释放资源 readNextLine读取当前行，并将焦点移动到下一行 readLine读取指定行
	 * getCellValue针对单元格内容不同格式进行读取
	 */

	// 用于读取excel中日期类型的单元格，指定日期格式
	private SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
	// xlsx格式的工作簿
	private XSSFWorkbook xssfWorkbook;
	// xls格式的工作簿
	private HSSFWorkbook hssfWorkbook;
	// 工作的sheet页
	private Sheet sheet;
	// 最大行数
	public int rows = 0;
	// 当前读取的行数
	private int lineNow = 0;

	// 构造函数，用来打开Excel
	public ExcelReader(String path) {
		// 截取后缀名
		String type = path.substring(path.indexOf("."));
		// 初始化文件流
		FileInputStream in = null;
		try {
			// 通过文件流打开excel文件
			in = new FileInputStream(new File(path));
		} catch (FileNotFoundException e1) {
			// 读取失败则给出Excel读取失败的提示，并停止
			e1.printStackTrace();
			return;
		}
		// 判断是xls还是xlsx格式
		if (type.equals(".xlsx")) {
			try {
				// 如果是xlsx格式，通过文件流，在内存中创建xssfworkbook工作簿
				xssfWorkbook = new XSSFWorkbook(in);
				// 初始化sheet页
				sheet = xssfWorkbook.getSheetAt(0);
				// 获取最大行数
				rows = sheet.getPhysicalNumberOfRows();
				lineNow = 0;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// 如果是xls格式，则创建hssf类型工作簿
		if (type.equals(".xls")) {
			try {
				hssfWorkbook = new HSSFWorkbook(in);
				sheet = hssfWorkbook.getSheetAt(0);
				rows = sheet.getPhysicalNumberOfRows();
				lineNow = 0;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// 关闭文件输入流
		try {
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// 如果没有sheet，则输出打开Excel失败的提示
		if (sheet == null)
			System.out.println("Excel文件打开失败！");
	}

	// 设置日期显示格式
	public void setDateFormat(String dateFormat) {
		fmt = new SimpleDateFormat(dateFormat);
	}

	// 设置读取的sheet页
	public void useSheet(String sheetName) {
		if (sheet != null) {
			// 根据打开的工作簿类型，在其中初始化sheet页
			if (hssfWorkbook != null)
				sheet = hssfWorkbook.getSheet(sheetName);
			else
				sheet = xssfWorkbook.getSheet(sheetName);

			rows = sheet.getPhysicalNumberOfRows();
			lineNow = 0;
		} else
			System.out.println("未打开Excel文件！");
	}

	// 获取当前Excel的所有sheet页
	public int getTotalSheetNo() {
		int sheets = 0;
		if (hssfWorkbook != null)
			sheets = hssfWorkbook.getNumberOfSheets();
		else
			sheets = xssfWorkbook.getNumberOfSheets();
		return sheets;
	}

	// 获取当前sheet页面的名字
	public String getSheetName(int sheetIndex) {
		String sheetname = "";
		if (hssfWorkbook != null)
			sheetname = hssfWorkbook.getSheetName(sheetIndex);
		else
			sheetname = xssfWorkbook.getSheetName(sheetIndex);
		return sheetname;
	}

	//根据sheet序号指定使用的sheet
	public void useSheetByIndex(int sheetIndex) {
		if (sheet != null) {
			try {
				if (hssfWorkbook != null)
					sheet = hssfWorkbook.getSheetAt(sheetIndex);
				else
					sheet = xssfWorkbook.getSheetAt(sheetIndex);

				rows = sheet.getPhysicalNumberOfRows();
				lineNow = 0;
			} catch (Exception e) {
				System.out.println("error::sheet页面不存在！");
				System.out.println(e.fillInStackTrace());
			}
		} else
			System.out.println("error::未打开Excel文件！");
	}

	// 读取完成，关闭Excel
	public void close() {
		try {
			if (hssfWorkbook != null)
				hssfWorkbook.close();
			else
				xssfWorkbook.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 读取lineNow行，lineNow++
	public List<String> readNextLine() {
		// 使用list存放当前行的信息
		List<String> line = new ArrayList<String>();
		// 读取当前行
		Row row = sheet.getRow(lineNow);
		// 读取当前行的单元格数
		int cellCount = row.getPhysicalNumberOfCells();
		// 循环读取该行中单元格的内容，存入list中
		for (int c = 0; c < cellCount; c++) {
			line.add(getCellValue(row.getCell(c)));
		}
		lineNow++;
		return line;
	}

	// 读取参数中指定的行
	public List<String> readLine(int rowNo) {
		List<String> line = new ArrayList<String>();
		Row row = sheet.getRow(rowNo);
		int cellCount = row.getPhysicalNumberOfCells();
		for (int c = 0; c < cellCount; c++) {
			line.add(getCellValue(row.getCell(c)));
		}
		return line;
	}

	// 读取指定列
	public List<String> readColumn(int colNo) {
		List<String> column = new ArrayList<String>();
		for (int i = 0; i < rows; i++) {
			Row row = sheet.getRow(i);
			column.add(getCellValue(row.getCell(colNo)));
		}
		return column;
	}

	// 读取指定单元格
	public String readCell(int rowNo, int column) {
		String content;
		Row row = sheet.getRow(rowNo);
		content = getCellValue(row.getCell(column));
		return content;
	}
	//以二维数组形式读取excel文件内容
		public Object[][] readAsMatrix(){
			int cellcount=sheet.getRow(0).getPhysicalNumberOfCells();
			Object[][] matrix=new Object[rows-1][cellcount];
			for(int rowNo =1;rowNo<rows;rowNo++) {
				for(int colNo=0;colNo<cellcount;colNo++)
				{
					matrix[rowNo-1][colNo]=readCell(rowNo, colNo);
				}
			}
			return matrix;
		}
		
		//以二维数组形式读取excel文件某一行内容
		public Object[][] readLineAsMatrix(int lineNo){
			int cellcount=sheet.getRow(0).getPhysicalNumberOfCells();
			Object[][] matrix=new Object[lineNo][cellcount];
				for(int colNo=0;colNo<cellcount;colNo++)
				{
					matrix[lineNo-1][colNo]=readCell(lineNo, colNo);
				}

			return matrix;
		}

	// 针对单元格内容不同格式进行读取
	@SuppressWarnings("deprecation")
	private String getCellValue(Cell cell) {
		// 当单元格内容为空时，返回null，这是由于poi读取某些xls格式的excel表时，针对某些空白格会报空指针异常
		String cellValue = "";
		if (cell == null)
			return cellValue;
		try {
			int cellType = cell.getCellType();
			// 将所有格式转为字符串
			switch (cellType) {
			case Cell.CELL_TYPE_STRING: // 文本
				cellValue = cell.getStringCellValue();
				break;
			case Cell.CELL_TYPE_NUMERIC: // 数字、日期
				if (DateUtil.isCellDateFormatted(cell)) {
					cellValue = fmt.format(cell.getDateCellValue()); // 日期型
				} else {
					Double d = cell.getNumericCellValue();
					DecimalFormat df = new DecimalFormat("#.##");
					cellValue = df.format(d);
				}
				break;
			case Cell.CELL_TYPE_BOOLEAN: // 布尔型
				cellValue = String.valueOf(cell.getBooleanCellValue());
				break;
			case Cell.CELL_TYPE_BLANK: // 空白
				cellValue = cell.getStringCellValue();
				break;
			case Cell.CELL_TYPE_ERROR: // 错误
				cellValue = "错误";
				break;
			case Cell.CELL_TYPE_FORMULA: // 公式
				cellValue = "错误";
				break;
			default:
				cellValue = "错误";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cellValue;
	}

}
