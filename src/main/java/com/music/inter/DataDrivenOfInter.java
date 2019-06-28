package com.music.inter;

import org.testng.annotations.Test;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.jayway.jsonpath.JsonPath;
import com.music.common.ExcelWriter;
import com.music.log.AutoLogger;


public class DataDrivenOfInter {
	public HttpClientKw client;
	public Map<String, String> paramMap;
	//加入成员变量，方便在每一行用例调用时，统一操作的行数、返回结果的断言、excel的写入。
	public String tmpResponse;
	public int line = 0; // 成员变量行数，用于在用例执行时保持执行行和写入行一致
	public ExcelWriter outExcel;
	
	public DataDrivenOfInter(String casePath,String resultPath) {
		client=new HttpClientKw();
		paramMap=new HashMap<String,String>();
		outExcel =new ExcelWriter(casePath, resultPath);
	}
	
	public DataDrivenOfInter(ExcelWriter excel) {
		client=new HttpClientKw();
		paramMap=new HashMap<String,String>();
		outExcel=excel;
	}
	
	@Test
	public String testGet(String url,String input) {
		String response=null;
		try {
			String param=toParam(input);
			response =client.doGet(url, param);
			tmpResponse=response;
			outExcel.writeCell(line, 12, response);
			return response;
		} catch (Exception e) {
			AutoLogger.log.error("get方法发送失败，请检查");
			AutoLogger.log.error(e,e.fillInStackTrace());
			outExcel.writeFailCell(line, 11, "FAIL");
			outExcel.writeFailCell(line, 12, "get方法发送失败，请检查log");
			return response;
		}
		
	}
	@Test
	public String testGetMutipart(String url,String jsonParam) {
		String response=null;
		try {
			String param=toParam(jsonParam);
			response =client.doGetWithMutipart(url, param);
			tmpResponse=response;
			outExcel.writeCell(line, 12, response);
			return response;
		} catch (Exception e) {
			AutoLogger.log.error("get方法发送失败，请检查");
			AutoLogger.log.error(e,e.fillInStackTrace());
			outExcel.writeFailCell(line, 11, "FAIL");
			outExcel.writeFailCell(line, 12, "get方法发送失败，请检查log");
			return response;
		}
		
	}
	
	@Test
	public String testPost(String url,String input) {
		String response=null;
		try {
			String param=toParam(input);
			response =client.doPost(url, param);
			tmpResponse=response;
			outExcel.writeCell(line, 12, response);
			return response;
		} catch (Exception e) {
			AutoLogger.log.error("post方法发送失败，请检查");
			AutoLogger.log.error(e,e.fillInStackTrace());
			outExcel.writeFailCell(line, 11, "FAIL");
			outExcel.writeFailCell(line, 12, "get方法发送失败，请检查log");
			return response;
		}
		
	}
	
	@Test
	public String testPostJson(String url,String json) {
		String response=null;
		System.out.println(json);
		try {
		    response =client.doPostWithJson(url, json);
			tmpResponse=response;
			outExcel.writeCell(line, 12, response);
			return response;
		} catch (Exception e) {
			AutoLogger.log.error("post方法发送失败，请检查");
			AutoLogger.log.error(e,e.fillInStackTrace());
			outExcel.writeFailCell(line, 11, "FAIL");
			outExcel.writeFailCell(line, 12, "get方法发送失败，请检查log");
			return response;
		}
		
	}
	
	@Test
	public String testPostMutipart(String url,String jsonParam) {
		String response=null;
		System.out.println("传入参数"+jsonParam);
		try {
		    response =client.doPostWithMutipart(url, jsonParam);
			tmpResponse=response;
			outExcel.writeCell(line, 12, response);
			return response;
		} catch (Exception e) {
			AutoLogger.log.error("post方法发送失败，请检查");
			AutoLogger.log.error(e,e.fillInStackTrace());
			outExcel.writeFailCell(line, 11, "FAIL");
			outExcel.writeFailCell(line, 12, "get方法发送失败，请检查log");
			return response;
		}
		
	}
	
	
	
	@Test
	public String testPutJson(String url,String jsonParam) {
		String response=null;
		System.out.println("传入参数"+jsonParam);
		try {
		    response =client.doPutWithJson(url, jsonParam);
			tmpResponse=response;
			outExcel.writeCell(line, 12, response);
			return response;
		} catch (Exception e) {
			AutoLogger.log.error("putJson方法发送失败，请检查");
			AutoLogger.log.error(e,e.fillInStackTrace());
			outExcel.writeFailCell(line, 11, "FAIL");
			outExcel.writeFailCell(line, 12, "get方法发送失败，请检查log");
			return response;
		}
		
	}
	@Test
	public String testDeleteJson(String url,String jsonParam) {
		String response=null;
		String param=toParam(jsonParam);//{参数}取出来
		
		System.out.println("传入参数"+param);
		try {
		    response =client.doDeleteWithJson(url, param);
			tmpResponse=response;
			outExcel.writeCell(line, 12, response);
			return response;
		} catch (Exception e) {
			AutoLogger.log.error("deleteJson方法发送失败，请检查");
			AutoLogger.log.error(e,e.fillInStackTrace());
			outExcel.writeFailCell(line, 11, "FAIL");
			outExcel.writeFailCell(line, 12, "get方法发送失败，请检查log");
			return response;
		}
		
	}
	
	@Test
	public String testPostRest(String url,String input) {
		String response=null;
		try {
			String actUrl=toParam(url);
			String param=toParam(input);
//			String encodedParam = URLEncoder.encode(param);
			String encodedParam = URLEncoder.encode(param,"utf-8");
			System.out.println("param:"+encodedParam);
			response =client.doPost(actUrl, encodedParam);
			tmpResponse=response;
			outExcel.writeCell(line, 12, response);
			return response;
		} catch (Exception e) {
			AutoLogger.log.error("post方法发送失败，请检查");
			AutoLogger.log.error(e,e.fillInStackTrace());
			outExcel.writeFailCell(line, 11, "FAIL");
			outExcel.writeFailCell(line, 12, "get方法发送失败，请检查log");
			return response;
		}
	}
	
	@Test
	public String testGetRest(String url,String input) {
		String response=null;
		try {
			String actUrl=toParam(url);
			String param=toParam(input);
//			String encodedParam = URLEncoder.encode(param);
			String encodedParam = URLEncoder.encode(param,"utf-8");
			System.out.println("param:"+encodedParam);
			response =client.doGet(actUrl, encodedParam);
			tmpResponse=response;
			outExcel.writeCell(line, 12, response);
			return response;
		} catch (Exception e) {
			AutoLogger.log.error("post方法发送失败，请检查");
			AutoLogger.log.error(e,e.fillInStackTrace());
			outExcel.writeFailCell(line, 11, "FAIL");
			outExcel.writeFailCell(line, 12, "get方法发送失败，请检查log");
			return response;
		}
	}
	
	public void saveCookie() {
		try {
			client.saveCookie();
			outExcel.writeCell(line, 11, "PASS");
			System.out.println("保存cookie信息成功");
		} catch (Exception e) {
			outExcel.writeCell(line, 11, "FAIL");
		}
	}

	public void clearCookie() {
		try {
			client.clearCookie();
			outExcel.writeCell(line, 11, "PASS");
		} catch (Exception e) {
			outExcel.writeCell(line, 11, "FAIL");
		}
	}
	
	public void addHeader(String originJson) {
		Map<String, String> jsonmap=new HashMap<String, String>();
		String headerJson=toParam(originJson);
		try {
			JSONObject json = new JSONObject(headerJson);
			Iterator<String> jsonit = json.keys();
			while (jsonit.hasNext()) {
				String jsonkey = jsonit.next();
				jsonmap.put(jsonkey, json.get(jsonkey).toString());
			}
			outExcel.writeCell(line, 11, "PASS");
		} catch (JSONException e) {
			AutoLogger.log.error("头域参数格式错误，请检查");
			AutoLogger.log.error(e,e.fillInStackTrace());
			outExcel.writeFailCell(line, 11, "FAIL");
		}
		client.addHeader(jsonmap);
	}
	
	public void clearHeader() {
		try {
			client.clearHeader();
			outExcel.writeCell(line, 11, "PASS");
		} catch (Exception e) {
			outExcel.writeFailCell(line, 11, "FAIL");
		}
	}
	
	public void saveParam(String key,String jsonPath) {
		String value;
		try {
			System.out.println("jsonPath参数处理:"+jsonPath);
			value = JsonPath.read(tmpResponse,jsonPath).toString();
			System.out.println("value:"+value);
			paramMap.put(key, value);
			System.out.println("保存参数："+key+":"+value);
			outExcel.writeCell(line, 11, "PASS");
		} catch (Exception e) {
			AutoLogger.log.error("保存参数失败");
			AutoLogger.log.error(e,e.fillInStackTrace());
			outExcel.writeCell(line, 11, "FAIL");
		}
	}
	
	
	public String toParam(String origin) {
		String param=origin;
		for(String key:paramMap.keySet()) {
		param=param.replaceAll("\\{"+key+"\\}", paramMap.get(key));
		}
		return param;
	}
	
	public String toParamString(String origin) {
		String param=origin;
		for(String key:paramMap.keySet()) {
		param=param.replaceAll("\\{"+key+"\\}", paramMap.get(key));
		}
		return param;
	}
	

	public void assertSame(String jsonPath,String expect) {
		try {
			String actual=JsonPath.read(tmpResponse,jsonPath).toString();
			System.out.println("jsonpath:"+jsonPath);
			System.out.println("实际结果:"+actual);
			if(actual!=null&&actual.equals(expect)) {
				AutoLogger.log.info("测试通过！");
				outExcel.writeCell(line, 11, "PASS");
			}
			else {
				AutoLogger.log.info("测试失败！");
				outExcel.writeFailCell(line, 11, "FAIL");
			}
		} catch (Exception e) {
			AutoLogger.log.error("解析失败，请检查jsonPath表达式");
			AutoLogger.log.error(e,e.fillInStackTrace());
			outExcel.writeFailCell(line, 11, "FAIL");
		}
	}

	
	
	public void assertContains(String jsonPath,String expect) {
		try {
			String actual=JsonPath.read(tmpResponse,jsonPath).toString();
			if(actual!=null&&actual.contains(expect)) {
				AutoLogger.log.info("测试通过！");
				outExcel.writeCell(line, 11, "PASS");
			}
			else {
				AutoLogger.log.info("测试失败！");
				outExcel.writeFailCell(line, 11, "FAIL");
			}
		} catch (Exception e) {
			AutoLogger.log.error("解析失败，请检查jsonPath表达式");
			AutoLogger.log.error(e,e.fillInStackTrace());
			outExcel.writeFailCell(line, 11, "FAIL");
		}
	}
}
