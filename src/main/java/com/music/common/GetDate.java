package com.music.common;

import java.text.SimpleDateFormat;
import java.util.Date;

public class GetDate {
	
	public String getDate() {
		//把当前的执行时间加入到结果文件名中
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HH+mm+ss");
        String createdate = sdf.format(date);
        return createdate;
	}

}
