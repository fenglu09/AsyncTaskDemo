package com.pinger.asynctaskdemo.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Xml;

import com.pinger.asynctaskdemo.domain.SmsBean;


/**
 * 短信的备份和还原
 * 
 * @author Pinger
 * 
 */
public class SmsUtil {

	/**
	 *  插入短信
	 * @param context
     */
	public static void insertSms(Context context){
		Uri uri = Uri.parse("content://sms");

		ContentResolver resolver = context.getContentResolver();

		long address = 10000;
		Random random = new Random();
		for(int i = 1;i <= 50; i ++){

			ContentValues values = new ContentValues();
			values.put("address",i + address);
			values.put("type",random.nextInt(3) + 1);
		 	values.put("date",5000 + i * 100);
			values.put("body","我是天才" + i + "号");
			resolver.insert(uri,values);
		}
	}

	/**
	 * 备份短信
	 * 
	 * @param context
	 *            备份的路径
	 */
	public void backUpSms(Context context, OnTaskListener listener){

		new BackupTask(context, listener).execute();
	}

	/**
	 * 还原短信
	 * 
	 * @param context
	 */
	public void restoreSms(Context context, OnTaskListener listener) {
		
		new RestoreTask(context, listener).execute();
	}

	/**
	 * 解析短信，存入集合
	 * 
	 * @param context
	 * @return
	 * @throws Exception
	 */
	public static List<SmsBean> parserSms(Context context) throws Exception {
		XmlPullParser parser = Xml.newPullParser();

		FileInputStream is = context.openFileInput("sms.xml");
		parser.setInput(is, "utf-8");

		List<SmsBean> list = new ArrayList<>();
		int type = parser.getEventType();

		SmsBean bean = null;
		while (type != XmlPullParser.END_DOCUMENT) {

			if (type == XmlPullParser.START_TAG) {

				String eventName = parser.getName();

				if ("sms".equals(eventName)) {
					bean = new SmsBean();
				} else if ("address".equals(eventName)) {
					bean.address = parser.nextText();
				} else if ("date".equals(eventName)) {
					bean.date = Long.parseLong(parser.nextText());
				} else if ("type".equals(eventName)) {
					bean.type = Integer.parseInt(parser.nextText());
				} else if ("body".equals(eventName)) {
					bean.body = parser.nextText();
				}

			} else if (type == XmlPullParser.END_TAG) {
				String eventName = parser.getName();
				if ("sms".equals(eventName)) {
					list.add(bean);
				}
			}
			type = parser.next();
		}
		return list;
	}

	/**
	 * 获取所有的短信
	 * 
	 * @param context
	 * @return
	 */
	public static List<SmsBean> getAllSms(Context context) {
		List<SmsBean> list = new ArrayList<SmsBean>();

		Uri uri = Uri.parse("content://sms");

		ContentResolver resolver = context.getContentResolver();

		Cursor cursor = resolver.query(uri, new String[] { "address", "date",
				"type", "body" }, null, null, null);

		// 遍历查询数据
		while (cursor.moveToNext()) {
			SmsBean bean = new SmsBean();

			bean.address = cursor.getString(0);
			bean.date = cursor.getLong(1);
			bean.type = cursor.getInt(2);
			bean.body = cursor.getString(3);

			list.add(bean);
		}

		return list;
	}

	/**
	 * 备份短信
	 */
	class BackupTask extends AsyncTask<Void, Integer, Boolean> {

		private ProgressDialog mDialog;
		private Context context;
		private OnTaskListener listener;

		public BackupTask(Context context, OnTaskListener listener) {
			this.context = context;
			this.listener = listener;
		}

		@Override
		protected Boolean doInBackground(Void... params) {

			List<SmsBean> list = SmsUtil.getAllSms(context);

			try {
				// 序列化器
				XmlSerializer xs = Xml.newSerializer();
				File file = new File(context.getFilesDir(), "sms.xml");
				// 设置输出路径
				xs.setOutput(new FileOutputStream(file), "utf-8");

				xs.startDocument("utf-8", true);

				xs.startTag(null, "smss");

				for (int i = 0; i < list.size(); i++) {

					SmsBean bean = list.get(i);

					xs.startTag(null, "sms");

					xs.startTag(null, "address");
					xs.text(bean.address);
					xs.endTag(null, "address");

					xs.startTag(null, "date");
					xs.text(bean.date + "");
					xs.endTag(null, "date");

					xs.startTag(null, "type");
					xs.text(bean.type + "");
					xs.endTag(null, "type");

					xs.startTag(null, "body");
					xs.text(bean.body);
					xs.endTag(null, "body");

					xs.endTag(null, "sms");

					SystemClock.sleep(50);

					publishProgress(i + 1, list.size());
				}

				xs.endTag(null, "smss");
				xs.endDocument();

				return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			// 运行在前台，初始化UI操作
			mDialog = new ProgressDialog(context);

			mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

			mDialog.show();

		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (result) {
				listener.onSuccess();
			} else {
				listener.onFailure();
			}

			mDialog.dismiss();
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			// 更新UI
			mDialog.setMax(values[1]);
			mDialog.setProgress(values[0]);
		}
	}

	/**
	 * 还原短信
	 * 
	 * @author Pinger
	 * 
	 */
	class RestoreTask extends AsyncTask<Void, Integer, Boolean> {
		private ProgressDialog mDialog;
		private Context context;
		private OnTaskListener listener;

		public RestoreTask(Context context, OnTaskListener listener) {
			this.context = context;
			this.listener = listener;
		}

		@Override
		protected Boolean doInBackground(Void... params) {

			try {
				Uri uri = Uri.parse("content://sms");

				ContentResolver resolver = context.getContentResolver();

				List<SmsBean> list = SmsUtil.parserSms(context);

				for (int i = 0; i < list.size(); i++) {
					SmsBean bean = list.get(i);

					ContentValues values = new ContentValues();
					values.put("address", bean.address);
					values.put("type", bean.type);
					values.put("date", bean.date);
					values.put("body", bean.body);
					resolver.insert(uri, values);

					SystemClock.sleep(50);

					publishProgress(i + 1, list.size());
				}

				return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			mDialog = new ProgressDialog(context);

			mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

			mDialog.show();

		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (result) {
				listener.onSuccess();
			} else {
				listener.onFailure();
			}

			mDialog.dismiss();
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			mDialog.setMax(values[1]);
			mDialog.setProgress(values[0]);
		}

	}

	/**
	 * 内部接口，用于回调
	 * 
	 * @author Pinger
	 * 
	 */
	public interface OnTaskListener {
		void onSuccess();

		void onFailure();
	}

}
