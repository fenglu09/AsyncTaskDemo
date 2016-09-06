package com.pinger.asynctaskdemo.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * 谈吐司的工具类
 */
public class ToastUtil {

	/**
	 * 构造私有化
	 */
	private ToastUtil(){}
	
	/**
	 * 显示短时间的吐司
	 * @param context
	 * @param text
	 */
	public static void showShort(Context context,String text){
		Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
	}
	
	/**
	 * 显示长时间的吐司
	 * @param context
	 * @param text
	 */
	public static void showLong(Context context,String text){
		Toast.makeText(context, text, Toast.LENGTH_LONG).show();
	}
}
