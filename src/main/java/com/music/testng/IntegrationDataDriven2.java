package com.music.testng;

import org.testng.annotations.Test;

import com.music.common.ExcelReader;
import com.music.common.ExcelWriter;
import com.music.common.report;
import com.music.inter.DataDrivenOfInter;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class IntegrationDataDriven2 {

	public static DataDrivenOfInter inter;

	public static String filepath;// 绝对文件路径

	@Test
	public void f1() {
		System.out.println("CaseTest类中@Test方法2被执行");

		// String type = args[0];
		String type = "http";
		// 获取项目的绝对路径
		filepath = System.getProperty("user.dir");
		// 根据不同的自动化类型，拼接用例文件和结果文件的路径
		String filename = "/cases/";
		String fileres = "/cases/result-";
		//
		try {
			System.out.println("log::info：文件路径：" + filepath);
			String date = createDate("yyyyMMdd+HH-mm-ss");
			String startTime = createDate("yyyy-MM-dd HH:mm:ss");
			switch (type) {// 根据传入参数不同选择执行不同的测试用例
			case "web":
				filename += "WebCases.xlsx";
				fileres += "WebCases" + date + ".xlsx";
				break;
			case "http":
				filename += "music_auto1.xlsx";
				fileres += "music_auto1" + date + ".xlsx";
				break;
			default:
				filename += "music_auto1.xls";
				fileres += "music_auto1.xls";
				System.out.println("log::error：类型错误！已经默认执行UI自动化。");
				break;
			}
			System.out.println("用例文件路径：" + filepath + filename);
			GetCase(filepath + filename, filepath + fileres, type);// 设置用例文件的路径
			// 发送邮件
			report testReport = new report();
			testReport.sendreport(filepath + fileres, startTime);
			System.out.println("发送报告所需数据"+filepath+fileres+startTime);
		} catch (Exception e) {
			System.out.println("log::error：获取文件位置失败，请检查。");
			e.printStackTrace();
		}

		System.out.print("输入回车，退出...");
		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 获取测试用例方法
	 * 
	 * @param file
	 * @param fileres
	 * @param type
	 */
	
	private static void GetCase(String file, String fileres, String type) {
		// 打开Excel
		ExcelReader excelr = new ExcelReader(file);
		ExcelWriter excelw = new ExcelWriter(file, fileres);

		List<String> list = null;
		// 根据传入的用例和结果文件路径，实例化关键字类的成员变量
		inter = new DataDrivenOfInter(excelw);
		String close = "no";// 是否进行保存标志

		// 循环读取用例sheet
		for (int i = 0; i < excelr.getTotalSheetNo(); i++) {
			String sheetName = excelr.getSheetName(i);
			excelr.useSheet(sheetName);
			excelw.useSheet(sheetName);
			if (i == excelr.getTotalSheetNo() - 1) {
				close = "yes";
			}
			System.out.println("当前sheet页名字：" + sheetName);

			// 遍历excel当中每一行，执行关键字并将结果写入对应单元格
			for (int caseline = 0; caseline < excelr.rows; caseline++) {
				// 读取excel当中的每行内容
				System.out.println(excelr.readLine(caseline));
				// 调用时，赋值给关键字类中的成员变量line
				inter.line = caseline;
				// 读取每行中的内容
				list = excelr.readLine(caseline);
				// 判断第一第二列是否为空，第一、二列为空才是要执行的
				if ((list.get(0) != null || list.get(1) != null)
						&& (!list.get(0).equals("null") || !list.get(1).equals("null"))
						&& (list.get(0).length() > 0 || list.get(1).length() > 0)) {
					;
				} else {
					switch (type) {
//							case "web":
//								web.line = line;
//								runWeb(list);
//								break;
					case "http":
						inter.line = caseline;
						runHttp(list);
						break;

					default:
						break;
					}
				}
			}
			// 保存写入的结果文件
			if (close.equals("yes")) {
				excelr.close();
				excelw.save();
			}

		}
	}

	/**
	 * http执行接口用例
	 * 
	 * @param list
	 */

	private static void runHttp(List<String> list) {
		// 执行关键字相应操作
		try {
			// 通过Excel表中填写的关键字判断调用哪个方法执行
			switch (list.get(3)) {
			case "post":
				inter.testPost(list.get(4), list.get(5));
				break;
			case "postJson":
				inter.testPostJson(list.get(4), list.get(5));
				break;
			case "postMutipart":
				inter.testPostMutipart(list.get(4), list.get(5));
				break;
			case "get":
				inter.testGet(list.get(4), list.get(5));
				break;
			case "getMutipart":
				inter.testGetMutipart(list.get(4), list.get(5));
				break;
			case "putJson":
				inter.testPutJson(list.get(4), list.get(5));
				break;
			case "deleteJson":
				inter.testDeleteJson(list.get(4), list.get(5));
				break;
			case "testPostRest":
				inter.testPostRest(list.get(4), list.get(5));
				break;
			case "savecookie":
				inter.saveCookie();
				break;
			case "clearcookie":
				inter.clearCookie();
				break;
			case "addHeader":
				inter.addHeader(list.get(4));
				break;
			case "saveParam":
				inter.saveParam(list.get(4), list.get(5));
				break;
			}
			// 通过excel表中填写的校验方法确定
			switch (list.get(7)) {
			case "equal":
				inter.assertSame(list.get(9), list.get(10));
				break;
			case "contain":
				inter.assertContains(list.get(9), list.get(10));
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 创建时间格式设置
	 * @param dateFormat
	 * @return
	 */
	private static String createDate(String dateFormat) {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		String createdate = sdf.format(date);
		return createdate;
	}

}
