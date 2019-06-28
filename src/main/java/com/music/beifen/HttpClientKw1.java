package com.music.beifen;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;

import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import com.jayway.jsonpath.JsonPath;

public class HttpClientKw1 {

	// 是否使用cookie标志位，默认不使用cookie
	private boolean useCookie = false;
	// cookieStore类，httpclient用它来记录得到的cookie值
	private CookieStore cookies = new BasicCookieStore();
	// 成员变量headers，用于存放需要加载的头域参数。
	private Map<String, String> headers = new HashMap<String, String>();
	// 是否添加header，默认不添加
	private boolean addHeaderFlag = false;

	// 匹配unicode编码格式的正则表达式。
	private static final Pattern reUnicode = Pattern.compile("\\\\u([0-9a-zA-Z]{4})");

	/**
	 * 查找字符串中的unicode编码并转换为中文。
	 * 
	 * @param u
	 * @return
	 */
	private String DeCode(String u) {
		try {
			Matcher m = reUnicode.matcher(u);
			StringBuffer sb = new StringBuffer(u.length());
			while (m.find()) {
				m.appendReplacement(sb, Character.toString((char) Integer.parseInt(m.group(1), 16)));
			}
			m.appendTail(sb);
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return u;
		}
	}

	/**
	 * SSLcontext用于绕过ssl验证，使发包的方法能够对https的接口进行请求。
	 */
	public static SSLContext createIgnoreVerifySSL() throws NoSuchAlgorithmException, KeyManagementException {
		SSLContext sc = SSLContext.getInstance("SSLv3");

		// 实现一个X509TrustManager接口，用于绕过验证，不用修改里面的方法
		X509TrustManager trustManager = new X509TrustManager() {
			@Override
			public void checkClientTrusted(java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
					String paramString) throws CertificateException {
			}

			@Override
			public void checkServerTrusted(java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
					String paramString) throws CertificateException {
			}

			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}
		};

		sc.init(null, new TrustManager[] { trustManager }, null);
		return sc;
	}

	/**
	 * 通过httpclient实现get方法，其中包括代理地址的设置、头域添加和cookie使用。
	 * 
	 * @param url   接口的url地址
	 * @param param 接口的参数列表。
	 */
	public String doGet(String url, String param) throws Exception {

		String body = "";

		// 采用绕过验证的方式处理https请求
		SSLContext sslcontext = createIgnoreVerifySSL();
		// 设置代理地址，适用于需要用fiddler抓包时使用，不用时切记注释掉这句！
//		HttpHost proxy = new HttpHost("localhost", 8888, "http");  

		// 设置协议http和https对应的处理socket链接工厂的对象
		Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
				.register("http", PlainConnectionSocketFactory.INSTANCE)
				.register("https", new SSLConnectionSocketFactory(sslcontext)).build();
		PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);

		// 创建自定义的httpclient对象
		CloseableHttpClient client;
		// 当需要进行代理抓包时，启动如下代码，否则，用下一段代码。
//		if (useCookie) {
//			client = HttpClients.custom().setProxy(proxy).setConnectionManager(connManager).setDefaultCookieStore(cookies).build();
//		} else {
//			client = HttpClients.custom().setProxy(proxy).setConnectionManager(connManager).build();
//		}
//		//设置请求时通过代理发送。
//		RequestConfig config = RequestConfig.custom().setProxy(proxy).build();

		// 基于是否需要使用cookie，用不同方式创建httpclient实例。
		if (useCookie) {
			// 实例化httpclient时，使用cookieStore，此时将会使用cookie
			client = HttpClients.custom().setConnectionManager(connManager).setDefaultCookieStore(cookies).build();
		} else {
			// 实例化httpclient时，使用cookieStore，此时将不使用cookie
			client = HttpClients.custom().setConnectionManager(connManager).build();
		}

		try {
			String urlWithParam = "";
			if (param.length() > 0) {
				urlWithParam = url + "?" + param;
			} else {
				urlWithParam = url;
			}
			// 创建get方式请求对象
			HttpGet get = new HttpGet(urlWithParam);
			RequestConfig config = RequestConfig.custom().setSocketTimeout(15000).setConnectTimeout(10000).build();
			get.setConfig(config);
			// 指定报文头Content-type、User-Agent
			get.setHeader("accept", "*/*");
			get.setHeader("Content-type", "application/x-www-form-urlencoded");
			get.setHeader("User-Agent",
					"Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.77 Safari/537.36");

			// 通过是否添加头域的标识符判断是否执行头域参数添加操作
			if (addHeaderFlag = true) {
				// 从头域map中遍历添加头域
				Set<String> headerKeys = headers.keySet();
				for (String key : headerKeys) {
					get.setHeader(key, headers.get(key));
				}
			}

			// 执行请求操作
			CloseableHttpResponse response = client.execute(get);

			// 打印所有cookie
			List<Cookie> cookiestore = cookies.getCookies();
			for (Cookie c : cookiestore) {
				System.out.println(c);
			}

			// 获取结果实体
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				// 按指定编码转换结果实体为String类型
				body = EntityUtils.toString(entity, "UTF-8");
			}
			// 关闭流实体
			EntityUtils.consume(entity);
			// 释放链接
			response.close();
			String result = DeCode(body);
//			System.out.println("body:" + body);
			System.out.println("result:" + result);
			return result;
		} catch (Exception e) {
			System.out.println();
			e.printStackTrace();
		} finally {
			client.close();
		}
		return null;
	}

	/**
	 * 通过httpclient实现get方法，其中包括代理地址的设置、头域添加和cookie使用。
	 * 
	 * @param url   接口的url地址
	 * @param param 接口的参数列表。
	 */
	public String doGetWithMutipart(String url, String jsonParam) throws Exception {

		OutputStream out = null;
		InputStream in = null;
		String result = "";// 返回结果
		String musicId = "";
		String filePath = "";
		String body = "";
		// 采用绕过验证的方式处理https请求
		SSLContext sslcontext = createIgnoreVerifySSL();
		// 设置代理地址，适用于需要用fiddler抓包时使用，不用时切记注释掉这句！
//		HttpHost proxy = new HttpHost("localhost", 8888, "http");

		// 设置协议http和https对应的处理socket链接工厂的对象
		Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
				.register("http", PlainConnectionSocketFactory.INSTANCE)
				.register("https", new SSLConnectionSocketFactory(sslcontext)).build();
		PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);

		// 创建自定义的httpclient对象
		CloseableHttpClient client;
		// 当需要进行代理抓包时，启动如下代码，否则，用下一段代码。
//		if (useCookie) {
//			client = HttpClients.custom().setProxy(proxy).setConnectionManager(connManager)
//					.setDefaultCookieStore(cookies).build();
//		} else {
//			client = HttpClients.custom().setProxy(proxy).setConnectionManager(connManager).build();
//		}
//		//设置请求时通过代理发送。
//		RequestConfig config = RequestConfig.custom().setProxy(proxy).build();

		 //基于是否需要使用cookie，用不同方式创建httpclient实例。
		if (useCookie) {
			// 实例化httpclient时，使用cookieStore，此时将会使用cookie
			client = HttpClients.custom().setConnectionManager(connManager).setDefaultCookieStore(cookies).build();
		} else {
			// 实例化httpclient时，使用cookieStore，此时将不使用cookie
			client = HttpClients.custom().setConnectionManager(connManager).build();
		}
		try {
			String urlWithParam = "";
			if (jsonParam.length() > 0) {// 对参数进行处理

				JSONObject jsonObject = new JSONObject(jsonParam);

				if (jsonObject.has("musicId") && jsonObject.has("filePath")) {// 如果必填字段未填写

					if (!jsonObject.getString("musicId").equals("") && !jsonObject.getString("filePath").equals("")) {
						musicId = jsonObject.getString("musicId");
						filePath = jsonObject.getString("filePath");
						urlWithParam = url + "/" + musicId;
					} else {// 字段为空信息校验
						if (jsonObject.getString("musicId").equals("")) {// userId为空
							urlWithParam = url;
						}
						if (jsonObject.getString("filePath").equals("")) {// filePath为空
							musicId = jsonObject.getString("musicId");
							urlWithParam = url + "/" + musicId;
						}
					}
				} else {// 必填字段填写校验
					if (!jsonObject.has("musicId")) {// userId未填写
						urlWithParam = url;
					}
					if (!jsonObject.has("filePath")) {// filePath未填写
						musicId = jsonObject.getString("musicId");
						urlWithParam = url + "/" + musicId;
					}
				}

			} else {

				urlWithParam = url;
				System.out.println("参数为空");
			}

			System.out.println("getMutipart::" + urlWithParam);

			// 创建get方式请求对象
			HttpGet get = new HttpGet(urlWithParam);

			RequestConfig config = RequestConfig.custom().setSocketTimeout(15000).setConnectTimeout(10000).build();
			get.setConfig(config);
			// 指定报文头Content-type、User-Agent
			get.setHeader("accept", "application/json");
			get.setHeader("Content-type", "application/json");
			get.setHeader("User-Agent",
					"Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.77 Safari/537.36");

			// 通过是否添加头域的标识符判断是否执行头域参数添加操作
			if (addHeaderFlag = true) {
				// 从头域map中遍历添加头域
				Set<String> headerKeys = headers.keySet();
				for (String key : headerKeys) {
					get.setHeader(key, headers.get(key));
				}
			}
			// 打印所有cookie
			List<Cookie> cookiestore = cookies.getCookies();
			for (Cookie c : cookiestore) {
				System.out.println(c);
			}
			// 执行请求操作
			CloseableHttpResponse response = client.execute(get);

			// 获取结果实体
			HttpEntity entity = response.getEntity();
			in = entity.getContent();
			long length = entity.getContentLength();

			// 判定文件存在否
			if (length <= 0) {
				body = EntityUtils.toString(entity, "UTF-8");
				// 关闭流实体
				EntityUtils.consume(entity);
				result = DeCode(body);
				System.out.println("服务器返回结果数据:" + result);
				return result;
			}

			if (filePath.equals("")) {
				result = "{'code':'-1','data':'系统找不到指定的路径'}";
				System.out.println("{'code':'-1','data':'系统找不到指定的路径'}");
				return result;
			} else {
				// 存储下载文件
				File file = new File(filePath);
				if (!file.exists()) {
					file.createNewFile();
					out = new FileOutputStream(file);
					byte[] buffer = new byte[4096];
					int readLength = 0;
					while ((readLength = in.read(buffer)) > 0) {
						byte[] bytes = new byte[readLength];
						System.arraycopy(buffer, 0, bytes, 0, readLength);
						out.write(bytes);
					}
					out.flush();// 清空数据
					// 释放链接
				}
//				
				response.close();
				result = "{'result':'1','code':0,'success':'true','message':'下载成功'}";
				System.out.println("下载成功:" + result);
				return result;
			}
		} catch (Exception e) {// try
			e.printStackTrace();
		} finally {
			client.close();
		}
		return null;
	}

	/**
	 * 通过httpclient实现post方法，其中包括代理地址的设置、头域添加和cookie使用。
	 * 
	 * @param url   接口的url地址
	 * @param param 接口的参数列表。
	 */
	public String doPost(String url, String param) throws Exception {
		// 接收返回数据的String
		String body = "";
		// 设置代理地址，适用于需要用fiddler抓包时使用，不用时切记注释掉这句！
//		HttpHost proxy = new HttpHost("localhost", 8888, "http");  
		// 采用绕过验证的方式处理https请求
		SSLContext sslcontext = createIgnoreVerifySSL();
		// 设置协议http和https对应的处理socket链接工厂的对象，用于同时发送http和https请求
		Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
				.register("http", PlainConnectionSocketFactory.INSTANCE)
				.register("https", new SSLConnectionSocketFactory(sslcontext)).build();
		PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);

		// 创建httpclient对象
		CloseableHttpClient client;

//		//当需要进行代理抓包时，启动如下代码，否则，用下一段代码。
//		if (useCookie) {
//			client = HttpClients.custom().setProxy(proxy).setConnectionManager(connManager).setDefaultCookieStore(cookies).build();
//		} else {
//			client = HttpClients.custom().setProxy(proxy).setConnectionManager(connManager).build();
//		}
//		//设置请求时通过代理发送。
//		RequestConfig config = RequestConfig.custom().setProxy(proxy).build();

//		//基于是否需要使用cookie，用不同方式创建httpclient实例。
		if (useCookie) {
			// 实例化httpclient时，使用cookieStore，此时将会使用cookie
			client = HttpClients.custom().setConnectionManager(connManager).setDefaultCookieStore(cookies).build();
		} else {
			// 实例化httpclient时，使用cookieStore，此时将不使用cookie
			client = HttpClients.custom().setConnectionManager(connManager).build();
		}

		// 拼接接口地址和参数
		try {
			String urlWithParam = "";
			if (param.length() > 0) {
				urlWithParam = url + "?" + param;
			} else {
				urlWithParam = url;
			}

			// 创建post方式请求对象
			HttpPost httpPost = new HttpPost(urlWithParam);
			RequestConfig config = RequestConfig.custom().setSocketTimeout(15000).setConnectTimeout(10000).build();
			httpPost.setConfig(config);
			// 指定报文头Content-type、User-Agent
			httpPost.setHeader("accept", "*/*");
			httpPost.setHeader("Content-type", "application/x-www-form-urlencoded");
			httpPost.setHeader("User-Agent",
					"Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.77 Safari/537.36");

			// 通过是否添加头域的标识符判断是否执行头域参数添加操作
			if (addHeaderFlag = true) {
				// 从头域map中遍历添加头域
				Set<String> headerKeys = headers.keySet();
				for (String key : headerKeys) {
					httpPost.setHeader(key, headers.get(key));
				}
			}

			// 执行请求操作，并拿到结果
			CloseableHttpResponse response = client.execute(httpPost);

			// 打印所有cookie
			List<Cookie> cookiestore = cookies.getCookies();
			for (Cookie c : cookiestore) {
				System.out.println(c);
			}

			// 获取结果实体
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				// 按指定编码转换结果实体为String类型
				body = EntityUtils.toString(entity, "UTF-8");
			}

			EntityUtils.consume(entity);
			// 释放链接
			response.close();
			String result = DeCode(body);
//			System.out.println("body:" + body);
			System.out.println("result:" + result);
			return result;
		} catch (Exception e) {
			System.out.println();
			e.printStackTrace();
		} finally {
			client.close();
		}
		return null;
	}

	/**
	 * 通过httpclient实现post方法，其中包括代理地址的设置、头域添加和cookie使用。
	 * 
	 * @param url   接口的url地址
	 * @param param 接口的参数列表。
	 * @return
	 */
	public String doPostWithJson(String url, String json) throws Exception {

		// 接收返回数据的String
		String body = "";
		String result = "";
		// 设置代理地址，适用于需要用fiddler抓包时使用，不用时切记注释掉这句！
//		HttpHost proxy = new HttpHost("localhost", 8888, "http");  
		// 采用绕过验证的方式处理https请求
		SSLContext sslcontext = createIgnoreVerifySSL();
		// 设置协议http和https对应的处理socket链接工厂的对象，用于同时发送http和https请求
		Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
				.register("http", PlainConnectionSocketFactory.INSTANCE)
				.register("https", new SSLConnectionSocketFactory(sslcontext)).build();
		PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);

		// 创建httpclient对象
		CloseableHttpClient client;

//		//基于是否需要使用cookie，用不同方式创建httpclient实例。
		if (useCookie) {
			// 实例化httpclient时，使用cookieStore，此时将会使用cookie
			client = HttpClients.custom().setConnectionManager(connManager).setDefaultCookieStore(cookies).build();
		} else {
			// 实例化httpclient时，使用cookieStore，此时将不使用cookie
			client = HttpClients.custom().setConnectionManager(connManager).build();
		}

		// 拼接接口地址和参数
		try {

			
			// 创建post方式请求对象
			HttpPost httpPost = new HttpPost(url);
			RequestConfig config = RequestConfig.custom().setSocketTimeout(15000).setConnectTimeout(10000).build();
			httpPost.setConfig(config);
			// 指定报文头Content-type、User-Agent
			httpPost.setHeader("accept", "application/json");
			httpPost.setHeader("Content-type", "application/json");
			httpPost.setHeader("User-Agent",
					"Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36");

			// 通过是否添加头域的标识符判断是否执行头域参数添加操作
			if (addHeaderFlag = true) {
				// 从头域map中遍历添加头域
				Set<String> headerKeys = headers.keySet();
				for (String key : headerKeys) {
					httpPost.setHeader(key, headers.get(key));
				}
			}
			// 打印所有cookie
			List<Cookie> cookiestore = cookies.getCookies();
			for (Cookie c : cookiestore) {
				System.out.println(c);
			}

			// 传入json处理
			JSONObject obj = new JSONObject(json);
			System.out.println("传入json数据" + obj);

			StringEntity entity = new StringEntity(obj.toString(), "utf-8");// 解决中文乱码问题
			httpPost.setEntity(entity);

			CloseableHttpResponse response = client.execute(httpPost);

			// 判断是否重定向开始
			int code = response.getStatusLine().getStatusCode();
			String newuri = "";

			if (code == 302) {
				Header header = response.getFirstHeader("location"); // 跳转的目标地址是在 HTTP-HEAD 中的
				newuri = header.getValue(); // 这就是跳转后的地址，再向这个地址发出新申请，以便得到跳转后的信息是啥。
				System.out.println("重定向地址:" + newuri);
				System.out.println(code);
				// 自己进行跳转传递参数过去
				result = doGet(newuri, "");
				return result;
			} else {
				// 获取结果实体
				HttpEntity entity1 = response.getEntity();
				if (entity1 != null) {
					// 按指定编码转换结果实体为String类型
					body = EntityUtils.toString(entity1, "UTF-8");
				}
				// 关闭流实体
				EntityUtils.consume(entity1);
				// 释放链接
				response.close();
				result = DeCode(body);
				System.out.println("result:" + result);
				return result;
			}
		} catch (Exception e) {
			System.out.println();
			e.printStackTrace();
		} finally {
			client.close();
		}
		return null;
	}

	/**
	 * 通过httpclient实现post方法，其中包括代理地址的设置、头域添加和cookie使用。
	 * 
	 * @param url   接口的url地址
	 * @param param 接口的参数列表。
	 */
	public String doPostWithMutipart(String url, String jsonParam) throws Exception {
		// 接收返回数据的String
		String body = "";
		// 传入参数接受
		String filePath = "";
		String speed = "";
		String styleId = "";
		String result = "";// 如果发生302跳转后执行返回结果
		String flag = "";// 传入参数个数处理
		// 采用绕过验证的方式处理https请求
		SSLContext sslcontext = createIgnoreVerifySSL();
		// 使用代理方式
//		HttpHost proxy = new HttpHost("localhost", 8888, "http");
		// 设置协议http和https对应的处理socket链接工厂的对象，用于同时发送http和https请求
		Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
				.register("http", PlainConnectionSocketFactory.INSTANCE)
				.register("https", new SSLConnectionSocketFactory(sslcontext)).build();
		PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);

		// 创建httpclient对象
		CloseableHttpClient client;
		// 接受返回数据
		CloseableHttpResponse response;

		// 使用代理方式 基于是否需要使用cookie，用不同方式创建httpclient实例。
//		if (useCookie) {
//			// 实例化httpclient时，使用cookieStore，此时将会使用cookie
//			client = HttpClients.custom().setConnectionManager(connManager).setProxy(proxy)
//					.setDefaultCookieStore(cookies).build();
//		} else {
//			// 实例化httpclient时，使用cookieStore，此时将不使用cookie
//			client = HttpClients.custom().setProxy(proxy).setConnectionManager(connManager).build();
//		}

//		 基于是否需要使用cookie，用不同方式创建httpclient实例。
		if (useCookie) {
			// 实例化httpclient时，使用cookieStore，此时将会使用cookie
			client = HttpClients.custom().setConnectionManager(connManager).setDefaultCookieStore(cookies).build();
		} else {
			// 实例化httpclient时，使用cookieStore，此时将不使用cookie
			client = HttpClients.custom().setConnectionManager(connManager).build();
		}

//		//拼接接口地址和参数
		try {
			// 判定传入参数不为空
			if (jsonParam.length() > 0) {
				JSONObject jsonObject = new JSONObject(jsonParam);
				// 判定必填项不为空
				if (jsonObject.has("filePath") && jsonObject.has("speed") && jsonObject.has("styleId")) {
					// 存在空字符串
					if (jsonObject.getString("filePath").equals("") || jsonObject.getString("speed").equals("")
							|| jsonObject.getString("styleId").equals("")) {
						if (jsonObject.getString("filePath").equals("")) {
							speed = jsonObject.getString("speed");
							styleId = jsonObject.getString("styleId");
							flag = "2";
						}
						if (jsonObject.getString("speed").equals("")) {
							filePath = jsonObject.getString("filePath");
							styleId = jsonObject.getString("styleId");
//							System.out.println("speed为空");
							flag = "3";
						}
						if (jsonObject.getString("styleId").equals("")) {
							filePath = jsonObject.getString("filePath");
							speed = jsonObject.getString("speed");
//							System.out.println("styleId为空");
							flag = "4";
						}
					} else {// 全部数据正确
						filePath = jsonObject.getString("filePath");
//						System.out.println("非空参数"+filePath);
						speed = jsonObject.getString("speed");
						styleId = jsonObject.getString("styleId");
						flag = "1";
					}

				} else {
					if (!jsonObject.has("filePath") && jsonObject.has("speed") && jsonObject.has("styleId")) {
						speed = jsonObject.getString("speed");
						styleId = jsonObject.getString("styleId");
//						System.out.println("参数filePath为空");
						flag = "2";
					}
					if (!jsonObject.has("speed") && jsonObject.has("filePath") && jsonObject.has("styleId")) {
						filePath = jsonObject.getString("filePath");
						styleId = jsonObject.getString("styleId");
//						System.out.println("参数speed为空");
						flag = "3";
					}
					if (!jsonObject.has("styleId") && jsonObject.has("filePath") && jsonObject.has("speed")) {
						filePath = jsonObject.getString("filePath");
						speed = jsonObject.getString("speed");
//						System.out.println("参数styleId为空");
						flag = "4";
					}

				}
			} else {
				jsonParam = "";
				System.out.println("传入参数为空");
			}

			// 创建post方式请求对象
			HttpPost httpPost = new HttpPost(url);
			RequestConfig config = RequestConfig.custom().setSocketTimeout(15000).setConnectTimeout(10000).build();
			httpPost.setConfig(config);

			// 指定报文头Content-type、User-Agent
			httpPost.setHeader("accept", "application/json");
//			httpPost.setHeader("Content-type", "multipart/form-data; boundary=----WebKitFormBoundarySieRSBXAHSorZBoL");
			httpPost.setHeader("User-Agent",
					"Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.77 Safari/537.36");
			// 通过是否添加头域的标识符判断是否执行头域参数添加操作
			if (addHeaderFlag = true) {
				// 从头域map中遍历添加头域
				Set<String> headerKeys = headers.keySet();
				for (String key : headerKeys) {
					httpPost.setHeader(key, headers.get(key));
				}
			}
			// 打印所有cookie
			List<Cookie> cookiestore = cookies.getCookies();
			for (Cookie c : cookiestore) {
				System.out.println(c);
			}

			// 加入文件数据代码
			MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create()
					.setMode(HttpMultipartMode.RFC6532);

			if (flag.equals("1")) {
				File file = new File(filePath);
				if (file.isFile()) {// 如果是文件才创建避免出错
					multipartEntityBuilder.addBinaryBody("file", file);
				}
				multipartEntityBuilder.addTextBody("speed", speed);
				multipartEntityBuilder.addTextBody("styleId", styleId);
			}
			if (flag.equals("2")) {
				multipartEntityBuilder.addTextBody("speed", speed);
				multipartEntityBuilder.addTextBody("styleId", styleId);
			}
			if (flag.equals("3")) {
				File file = new File(filePath);
				multipartEntityBuilder.addBinaryBody("file", file);
				multipartEntityBuilder.addTextBody("styleId", styleId);

			}
			if (flag.equals("4")) {
				File file = new File(filePath);
				multipartEntityBuilder.addBinaryBody("file", file);
				multipartEntityBuilder.addTextBody("speed", speed);
			}

			HttpEntity httpEntity = multipartEntityBuilder.build();
			httpPost.setEntity(httpEntity);

			// 执行请求操作，并拿到结果
			response = client.execute(httpPost);
//			// 获取结果实体
//			HttpEntity responseEntity = response.getEntity();

			// 判断是否重定向开始
			int code = response.getStatusLine().getStatusCode();
			String newuri = "";
			if (code == 302) {
				Header header = response.getFirstHeader("location"); // 跳转的目标地址是在 HTTP-HEAD 中的
				newuri = header.getValue(); // 这就是跳转后的地址，再向这个地址发出新申请，以便得到跳转后的信息是啥。
				System.out.println("重定向地址:" + newuri);
				System.out.println(code);
				// 自己进行跳转传递参数过去
				result = doGet(newuri, "");
				return result;
			} else {
				// 获取结果实体
				HttpEntity entity1 = response.getEntity();
				if (entity1 != null) {
					// 按指定编码转换结果实体为String类型
					body = EntityUtils.toString(entity1, "UTF-8");
				}
				// 关闭流实体
				EntityUtils.consume(entity1);
				// 释放链接
				response.close();
				result = DeCode(body);
				System.out.println("result:" + result);
				return result;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			client.close();
		}
		return null;

	}

	/**
	 * 通过httpclient实现soap请求，其中包括代理地址的设置、头域添加和cookie使用。
	 * 
	 * @param url   接口的url地址
	 * @param param 接口的参数列表。
	 */
	public String doSoap(String url, String param) throws Exception {
		// 接收返回数据的String
		String body = "";
		// 设置代理地址，适用于需要用fiddler抓包时使用，不用时切记注释掉这句！
//		HttpHost proxy = new HttpHost("localhost", 8888, "http");  
		// 采用绕过验证的方式处理https请求
		SSLContext sslcontext = createIgnoreVerifySSL();
		// 设置协议http和https对应的处理socket链接工厂的对象，用于同时发送http和https请求
		Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
				.register("http", PlainConnectionSocketFactory.INSTANCE)
				.register("https", new SSLConnectionSocketFactory(sslcontext)).build();
		PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);

		// 创建httpclient对象
		CloseableHttpClient client;

		// 当需要进行代理抓包时，启动如下代码，否则，用下一段代码。
//		if (useCookie) {
//			client = HttpClients.custom().setProxy(proxy).setConnectionManager(connManager).setDefaultCookieStore(cookies).build();
//		} else {
//			client = HttpClients.custom().setProxy(proxy).setConnectionManager(connManager).build();
//		}
		// 设置请求时通过代理发送。
//		RequestConfig config = RequestConfig.custom().setProxy(proxy).build();

		// 基于是否需要使用cookie，用不同方式创建httpclient实例。
		if (useCookie) {
			// 实例化httpclient时，使用cookieStore，此时将会使用cookie
			client = HttpClients.custom().setConnectionManager(connManager).setDefaultCookieStore(cookies).build();
		} else {
			// 实例化httpclient时，使用cookieStore，此时将不使用cookie
			client = HttpClients.custom().setConnectionManager(connManager).build();
		}

		// 拼接接口地址和参数
		try {
			String urlWithParam = url;

			// 创建post方式请求对象
			HttpPost httpPost = new HttpPost(urlWithParam);

			// 指定报文头Content-type、User-Agent
			httpPost.setHeader("accept", "*/*");
			// 要收发soap协议，必须是XML格式
			httpPost.setHeader("Content-type", "text/xml;charset=UTF-8");
			httpPost.setHeader("User-Agent",
					"Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.77 Safari/537.36");

			// 通过是否添加头域的标识符判断是否执行头域参数添加操作
			if (addHeaderFlag = true) {
				// 从头域map中遍历添加头域
				Set<String> headerKeys = headers.keySet();
				for (String key : headerKeys) {
					httpPost.setHeader(key, headers.get(key));
				}
			}
			// 将XML数据以实体形式添加到请求中
			StringEntity data = new StringEntity(param, Charset.forName("UTF-8"));
			httpPost.setEntity(data);

			// 执行请求操作，并拿到结果
			CloseableHttpResponse response = client.execute(httpPost);

			// 打印所有cookie
			List<Cookie> cookiestore = cookies.getCookies();
			for (Cookie c : cookiestore) {
				System.out.println(c);
			}

			// 获取结果实体
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				// 按指定编码转换结果实体为String类型
				body = EntityUtils.toString(entity, "UTF-8");
			}

			EntityUtils.consume(entity);
			// 释放链接
			response.close();
			String result = DeCode(body);
//			System.out.println("body:" + body);
			System.out.println("result:" + result);
			return result;
		} catch (Exception e) {
			System.out.println();
			e.printStackTrace();
		} finally {
			client.close();
		}
		return null;
	}

	/**
	 * 设置使用cookie标志位为true，此时实例化httpclient带上cookie
	 */
	public void saveCookie() {
		useCookie = true;
	}

	/**
	 * 设置使用cookie标志位为false，此时实例化httpclient不带cookie，并且重置cookieStore，清空其中的内容。
	 */
	public void clearCookie() {
		useCookie = false;
		cookies = new BasicCookieStore();
	}

	/**
	 * 设置添加头域标志位为true，并且通过传递头域map，实例化成员变量headers
	 * 
	 * @param headerMap传递的头域参数map
	 */
	public void addHeader(Map<String, String> headerMap) {
		headers = headerMap;
		addHeaderFlag = true;
	}

	/**
	 * 设置添加头域标志位为false，并重置成员变量headers
	 */
	public void clearHeader() {
		addHeaderFlag = false;
		headers = new HashMap<String, String>();
	}

}
