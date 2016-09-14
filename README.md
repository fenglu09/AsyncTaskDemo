# Android中的[AsyncTask](https://developer.android.com/reference/android/os/AsyncTask.html)和接口回调使用详解

[我的主页](http://www.jianshu.com/users/64f479a1cef7/latest_articles)
[Demo下载地址](https://github.com/PingerWan/AsyncTaskDemo)
---
## 一、[AsyncTask](https://developer.android.com/reference/android/os/AsyncTask.html)简单介绍
* 官方文档中队[AsyncTask](https://developer.android.com/reference/android/os/AsyncTask.html)的解释是：[AsyncTask](https://developer.android.com/reference/android/os/AsyncTask.html)更加适用于UI线程。这个类允许执行后台操作并在UI界面上发布结果，而不必处理多线程。AsyncTask是围绕Thread和Handler设计的一个辅助类，它不构成一个通用的线程框架。Asynctasks应该用于短作业（最多几秒钟）。

* 说的简单一点，[AsyncTask](https://developer.android.com/reference/android/os/AsyncTask.html)其实就是Android提供的一个轻量级异步类。使用的时候可以自己自定义一个类去继承[AsyncTask](https://developer.android.com/reference/android/os/AsyncTask.html),就能在自定义类中实现异步操作，并且该类的实现方法中提供了接口来反馈当前异步任务执行的程度,最后还可以将执行的结果传递给UI线程。


## 二、接口回调简单介绍

* 接口回调，字面意思可以理解为定义一个接口，等以后出现了某一种状况的时候，然后去调用接口的方法做一些事。 多个比方说，我是搞开发的，目前手里没有项目，我就打电话奥巴马问他手里有没有项目给我做，要是他手里有项目就直接给我了，要是没有他会说后面可能有，你留下电话，有了我就打电话告诉你，这就是一个简单的回调理解，奥巴马后面打电话给我就相当于一个回调过程，而我打电话给奥巴马就相当于注册接口。
* 在Demo中，接口回调使用在异步任务执行完毕之后。因为你备份完短信可能要谈个吐司，播个音乐什么的，那就必须让MainActivity知道你已经执行完任务了，但是MainActivity怎么知道你已经执行完了，这里就需要接口回调了，让MainActivity实现接口，并且先定义好当完成任务需要做什么事情。这样当任务执行完就会直接调用MainActivity中定义好的方法更新UI等操作。短信备份操作回调结构图如下：

![回调操作显示UI](http://upload-images.jianshu.io/upload_images/2786991-9df4c3f47fecc118.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
 
## 三、[AsyncTask](https://developer.android.com/reference/android/os/AsyncTask.html)和接口回调的使用案例
* 先来看一下使用AsyncTask显示备份短信和还原短信的进度条。实现的原理很简单，写一个短信的工具类，在类中提供从数据库读取短信到集合和还原保存的短信到集合的方。我们自定义两个类继承AsyncTask，一个类实现将集合中的短信保存到本地的逻辑，另一个类实现将将集合中的短信插入到数据库中的逻辑。并且当异步任务执行完毕之后，我们使用接口回调，让主线程去处理短信备份和还原完成的工作，这里是谈吐司，当然你也可以播放音乐什么的。


![AsyncTask更新进度条效果图](http://upload-images.jianshu.io/upload_images/2786991-81bc3a25dc8854d2.gif?imageMogr2/auto-orient/strip)

## 四、[AsyncTask](https://developer.android.com/reference/android/os/AsyncTask.html)使用详解
* 官方文档中称：异步任务将耗时操作放在后台线程上计算运行，然后将其结果在用户界面线程上发布。一个异步任务是由参数，过程和结果这3个泛型类型定义。它还包括四个步骤：oPostExecute，doInBackground，onProgressUpdate和onPostexecute。使用AsyncTask必须定义一个类继承AsyncTask，然后子类中必须实现doInBackground方法，经常也会实现oPostExecute方法。
* 自定义类继承AsyncTask代码如下：



		private class DownloadFilesTask extends AsyncTask<URL, Integer, Long> {
		     protected Long doInBackground(URL... urls) {
		         int count = urls.length;
		         long totalSize = 0;
		         for (int i = 0; i < count; i++) {
		             totalSize += Downloader.downloadFile(urls[i]);
		             publishProgress((int) ((i / (float) count) * 100));
		             // Escape early if cancel() is called
		             if (isCancelled()) break;
		         }
		         return totalSize;
		     }
		
		     protected void onProgressUpdate(Integer... progress) {
		         setProgressPercent(progress[0]);
		     }
		
		     protected void onPostExecute(Long result) {
		         showDialog("Downloaded " + result + " bytes");
	     	}
		 }

* 开启异步任务代码如下：



	 	new DownloadFilesTask().execute(url1, url2, url3);
 

### 1. 三个泛型类型
* Params：参数。启动任务执行需要输入的参数，比如HTTP请求的URL
* Progress：过程。后台任务执行的百分比
* Result：结果。后台执行任务最终返回的结果，比如String

* 这三个参数对四个步骤的方法的参数类型和返回值分别进行约束，如果没有约束的话，参数类型都为Void

		
		private class MyTask extends AsyncTask<Void, Void, Void> { ... }
 


### 2. 四个步骤
	
* onPreExecute()

	* 调用时机：第一个执行，并且在异步任务开始之前调用
	* 执行线程：主线程
	* 方法参数：无
	* 方法返回值：无
	* 方法的作用：用于提醒用户，当前正在请求数据，一般用来弹出进度对话框



			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				// 运行在前台，初始化UI操作
				mDialog = new ProgressDialog(context);
				mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				mDialog.show();
			}

* doInBackground()

	* 调用时机：在onPreExecute方法执行完毕之后，这个方法一定会执行
	* 执行线程：子线程
	* 方法参数

		* 由类上面的第一个泛型Params来限定
		* 从execute方法里面传递进来
	* 方法返回值

		* 由类上面的第三个泛型Result来限定
		* 将被当做onPostExecute()方法的参数
	* 方法的作用：在后台线程当中执行耗时操作，比如联网请求数据。在执行过程中可以调用publicProgress()来更新任务的进度





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
	
						SystemClock.sleep(100);
	
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


* onProgressUpdate()

	* 调用时机：在publishProgress()方法执行之后调用
	* 执行线程：主线程
	* 方法参数

		*  由类上面的第二个泛型来限定。
		*  参数是从publishProgress 传递进来
	* 方法返回值：无
	* 方法的作用：更新进度条



			@Override
			protected void onProgressUpdate(Integer... values) {
				super.onProgressUpdate(values);
				// 更新UI
				mDialog.setMax(values[1]);
				mDialog.setProgress(values[0]);
			}	


* onPostExecute()

	* 调用时机：在doInBackground 执行完毕之后调用
	* 执行线程：主线程
	* 方法参数

		* 由类上面的第三个泛型来限定。
		* doInBackground的返回值就是这个方法的参数
	* 方法返回值：无

	* 方法的作用：相当于Handler的handleMessage()方法，在这里面可以对doInBackground()方法得到的结果进行处理，更新UI


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


### 3. 需要遵守的准则

* 任务必须在主线程中执行
* 任务对象必须在主线程中构建
* execute方法必须在主线程中执行
* 四个步骤中的方法不能直接调用，publicProgress()方法可以在类中暴露一个方法出去，让外边调用
* 任务只能执行一次
	* 1.6开始的时候，可以并发执行多个任务，但是3.0之后，只能允许单个任务执行。如果真的想要多任务并发执行，那么可以运行在自己的线程池里面


			mTask.executeOnExecutor(exec, params)


## 五、接口回调简单使用
> 使用接口回调，一般有以下四个步骤，通过这四个步骤就能形成一个简单的回调。在Android中很多地方都用到了接口回调，比如控件的点击事件，GitHub上的许多开源框架也都用了接口回调，开发过程了也频频涉及到接口回调，所以这是一个很重要的知识点。

1. 定义接口，可以是内部接口，也可以是自定义接口


		/**
		 * 定义回调接口
		 */
		public interface OnTaskListener{
			
			/**
			 * 成功之后调用这个方法
			 */
			void onSuccess();
			
			/**
			 * 失败之后调用这个方法
			 */
			void onFailed();
			
			
		}

2. 接收接口实现类对象





		public BackupTask(Context context, OnTaskListener listener) {
				mContext = context;
				mListener = listener;
		}

3. 通过接口实现类对象，访问对应的方法




		if(result){
			mListener.onSuccess();
			// == ToastUtil.showShort(mContext,"备份成功");
		}else{
			mListener.onFailed();
			// == ToastUtil.showShort(mContext,"备份失败");
		}

4. 在实现中编写调用方法执行操作的代码




 		// 直接调用工具类的回调方法来弹出吐司
        new SmsUtil().backUpSms(this, new SmsUtil.OnTaskListener() {
            @Override
            public void onSuccess() {
                ToastUtil.showShort(MainActivity.this,"备份成功");
            }

            @Override
            public void onFailure() {
                ToastUtil.showShort(MainActivity.this,"备份失败");
            }
        });



---
[我的主页](http://www.jianshu.com/users/64f479a1cef7/latest_articles)

[Demo下载地址](https://github.com/PingerWan/AsyncTaskDemo)

####  以上纯属于个人平时工作和学习的一些总结分享，如果有什么错误欢迎随时指出，大家可以讨论一起进步。