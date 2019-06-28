package com.music.testng;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.music.common.ExcelReader;
import com.music.common.ExcelWriter;
import com.music.common.report;
import com.music.inter.DataDrivenOfInter;

public class DataDrivenInvoke {

	public DataDrivenOfInter http;
	public ExcelReader caseExcel;
	public ExcelWriter resultExcel;
	public static String createdate;
	public static String resultXlsxPath;


	@Test(dataProvider = "keywords")
	public void f(String rowNo, String group, String type, String casename, String keywords, String param1,
			String param2, String param3, String k1, String k2, String k3, String k4, String k5, String k6) {
		int No = 0;
		No = Integer.parseInt(rowNo);
		http.line = No;
		System.out.println(rowNo + casename);
		runHttpWithInvoke(keywords, param1, param2, param3);

	}

	@DataProvider
	public Object[][] keywords() {
		return caseExcel.readAsMatrix();
	}

	@BeforeSuite
	public void beforeSuite() {
		
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd+HH-mm-ss");
		createdate = sdf.format(date);
		String rootpath = System.getProperty("user.dir");
		caseExcel = new ExcelReader(rootpath + "\\cases\\music_auto_testng.xlsx");
		resultExcel = new ExcelWriter(rootpath + "\\cases\\music_auto_testng.xlsx",
				rootpath + "\\cases\\result-" + createdate + "music_auto_testng.xlsx");
		resultXlsxPath = rootpath + "\\cases\\result-" + createdate + "music_auto_testng.xlsx";
		System.out.println("所需路径"+resultXlsxPath);
		http = new DataDrivenOfInter(resultExcel);

	}

	@AfterSuite
	public void afterSuite() {
		caseExcel.close();
		resultExcel.save();
		// 发送邮件
//		report testReport = new report();
//		testReport.sendreport(resultXlsxPath,createdate);

	}

	private void runHttpWithInvoke(String keywords, String param1, String param2, String param3) {
		try {
			Method httpMethod0 = http.getClass().getDeclaredMethod(keywords);
			// invoke语法，需要输入类名以及相应的方法用到的参数
			httpMethod0.invoke(http);
			return;
		} catch (Exception e) {
		}
		try {
			Method httpMethod1 = http.getClass().getDeclaredMethod(keywords, String.class);
			// invoke语法，需要输入类名以及相应的方法用到的参数
			httpMethod1.invoke(http, param1);
			return;
		} catch (Exception e) {
		}
		try {
			Method httpMethod2 = http.getClass().getDeclaredMethod(keywords, String.class, String.class);
			// invoke语法，需要输入类名以及相应的方法用到的参数
			httpMethod2.invoke(http, param1, param2);
			return;
		} catch (Exception e) {
		}
		try {
			Method httpMethod3 = http.getClass().getDeclaredMethod(keywords, String.class, String.class, String.class);
			// invoke语法，需要输入类名以及相应的方法用到的参数
			httpMethod3.invoke(http, param1, param2, param3);
			return;
		} catch (Exception e) {
		}
	}



}
