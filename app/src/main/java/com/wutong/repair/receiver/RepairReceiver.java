package com.wutong.repair.receiver;

import java.util.Calendar;

import cn.jpush.android.api.JPushInterface;

import com.google.gson.reflect.TypeToken;
import com.wutong.androidprojectlibary.widget.util.ToastUtil;
import com.wutong.repairfjnu.R;
import com.wutong.repair.RepairApplication;
import com.wutong.repair.activity.HelpFoundDetailActivity;
import com.wutong.repair.activity.IndexModularActivity;
import com.wutong.repair.activity.NoticeDetailActivity;
import com.wutong.repair.activity.PushMessageListActivity;
import com.wutong.repair.activity.RepairOrderDetailActivity;
import com.wutong.repair.activity.WelcomeActivity;
import com.wutong.repair.data.bean.NotificationExtraBean;
import com.wutong.repair.dictionary.JPushMessageDict;
import com.wutong.repair.util.Action2IntegerUtil;
import com.wutong.repair.util.InternetConnUtil;
import com.wutong.repair.util.Logger;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;


public class RepairReceiver  extends BroadcastReceiver{
	private RepairApplication application;
	@Override
	public void onReceive(Context context, Intent intent) {
		if(application == null){
			application = (RepairApplication) context.getApplicationContext();
		}
		Bundle bundle = intent.getExtras();
		Logger.i("onReceive - " + intent.getAction() + ", extras: " + printBundle(bundle));
		if(Action2IntegerUtil.ACTION_BOOT_COMPLETED.equals(intent.getAction())){
			AlarmManager bootAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			Intent bootIntent = new Intent(Action2IntegerUtil.ACTION_BOOT_COMPLETED_BROADCAST);
			PendingIntent bootPendIntent = PendingIntent.getBroadcast(context,  
					0, bootIntent, PendingIntent.FLAG_UPDATE_CURRENT); 
			long triggerAtTime =SystemClock.elapsedRealtime() + 10000 ;
			bootAlarmManager.set(AlarmManager.ELAPSED_REALTIME, triggerAtTime, bootPendIntent);
		}
		else if(Action2IntegerUtil.ACTION_CONNECTIVITY_CHANGE.equals(intent.getAction())
				||Action2IntegerUtil.ACTION_BOOT_COMPLETED_BROADCAST.equals(intent.getAction())){
			if(InternetConnUtil.isHaveInternet(context)){
				Logger.i(intent.getAction() +" JPushInterface.init");
				JPushInterface.init(context);
			}
			else{
				Logger.i(intent.getAction() +" JPushInterface.init not do");
			}
		}
		else if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
			String regId = bundle.getString(JPushInterface.EXTRA_REGISTRATION_ID);
			Logger.i("??????Registration Id : " + regId);
			//send the Registration Id to your server...
		}else if (JPushInterface.ACTION_UNREGISTER.equals(intent.getAction())){
			String regId = bundle.getString(JPushInterface.EXTRA_REGISTRATION_ID);
			Logger.i("??????UnRegistration Id : " + regId);
			//send the UnRegistration Id to your server...
		} else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
			Logger.i("???????????????????????????????????????: " + bundle.getString(JPushInterface.EXTRA_MESSAGE));
			String contentType =  bundle.getString(JPushInterface.EXTRA_CONTENT_TYPE);
			String extra = bundle.getString(JPushInterface.EXTRA_EXTRA);
			Logger.i("????????????????????????"+ extra);
			NotificationExtraBean noticficationExtraBean = application.getGson().fromJson(extra, new TypeToken<NotificationExtraBean>(){}.getType());

			//content_type 1 ?????????????????????1?????????????????????2???????????????????????????3???????????????4???????????????5??????????????????2???????????????????????????

			if(contentType.equals(JPushMessageDict.PERSONAL)){

				if(noticficationExtraBean.getFormType().equals("4")){
					Logger.i("contentType" + contentType  + "---messageForHelpFound");
					messageForHelpFound(context,bundle);
				}
				else if(noticficationExtraBean.getFormType().equals("5")){
					Logger.i("contentType" + contentType  + "---messageForPushMessage");
					messageForPushMessage(context,bundle);
				}
				else{
					//??????1???2???3??????
					Logger.i("contentType" + contentType  + "---messageForNotification");
					messageForRepairOrder(context,bundle);
				}
			}
			else if(contentType.equals(JPushMessageDict.ALL)){
				Logger.i("contentType" + contentType  + "---messageForNotice");
				if(noticficationExtraBean.getFormType().equals("6")){
					Logger.i("contentType" + contentType  + "---messageForPushMessage");
					//???????????????????????? url
					messageForLink(context,bundle);
				}
				else if(noticficationExtraBean.getFormType().equals("7")){
					if(application.hasPermission(context.getString(R.string.modular_url_micro_share))){
						Logger.i("has micro share");
						Logger.i("contentType" + contentType  + "---messageForMicroShare");
						//???????????????
						messageForMicroShare(context,bundle);
					}
				}
				else{
					messageForNotice(context,bundle);
				}
			}
			else{
				//????????????????????????????????????
				
				Logger.i("contentType" + contentType  + "---messageFor    none");
				ToastUtil.showToast(context, "????????????????????????:");
			}
		} else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
			Logger.i("??????????????????????????????");
			int notifactionId = bundle.getInt(JPushInterface.EXTRA_NOTIFICATION_ID);
			Logger.i( "?????????????????????????????????ID: " + notifactionId);

		} else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
			Logger.i("???????????????????????????");


		}
		else {

		}



	}



	// ??????????????? intent extra ??????
	private static String printBundle(Bundle bundle) {
		if(bundle != null){
			StringBuilder sb = new StringBuilder();
			for (String key : bundle.keySet()) {
				if (key.equals(JPushInterface.EXTRA_NOTIFICATION_ID)) {
					sb.append("\nkey:" + key + ", value:" + bundle.getInt(key));
				} else {
					sb.append("\nkey:" + key + ", value:" + bundle.get(key).toString());
				}
			}
			return sb.toString();
		}
		else{
			return null;
		}
	}

	private void messageForRepairOrder(Context context,Bundle bundle){
		String title = bundle.getString(JPushInterface.EXTRA_TITLE);
		String content = bundle.getString(JPushInterface.EXTRA_MESSAGE);
		boolean hasShoud = hasSound();
		Logger.i("?????????" +hasShoud);
		String extra = bundle.getString(JPushInterface.EXTRA_EXTRA);
		Logger.i("????????????????????????"+ extra);
		NotificationExtraBean notificationExtraBean = ((RepairApplication)context.getApplicationContext()).getGson().fromJson(extra, new TypeToken<NotificationExtraBean>(){}.getType());
		if(notificationExtraBean == null || notificationExtraBean.getMessageId() ==null ||notificationExtraBean.getMessageId().toString().trim().length() == 0){
			ToastUtil.showToast(context, "????????????????????????????????????");
			showNotifcation(context, title, content, hasShoud, WelcomeActivity.class,null);
		}
		else{
			Bundle data = new Bundle();
			data.putString("repairOrderId", notificationExtraBean.getMessageId());
			data.putString("repairType", notificationExtraBean.getFormType());
			data.putBoolean("isFromIndex", true);
			showNotifcation(context, title, content, hasShoud, RepairOrderDetailActivity.class,data);
		}
	}

	private void showNotifcation(Context context,String title,String content,boolean hasSound, Class<?> cls,Bundle bundle){
		NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);               
//		Notification notification = new Notification(R.drawable.ic_launcher, title, System.currentTimeMillis());
//		notification.flags = Notification.FLAG_AUTO_CANCEL;
		Intent i = new Intent(context, cls);
		if(bundle != null){
			i.putExtras(bundle);
		}
		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);           
		//PendingIntent
		PendingIntent contentIntent = PendingIntent.getActivity(context,R.string.app_name, i, PendingIntent.FLAG_UPDATE_CURRENT);
//		notification.setLatestEventInfo(context,title, content,contentIntent);

		Notification notification = new Notification.Builder(context)
				.setContentTitle(title)
				.setContentText(content)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentIntent(contentIntent)
				.build();
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		// ?????????????????? 
		if(hasSound){
			notification.defaults=Notification.DEFAULT_ALL;
			// audioStreamType????????????AudioManager????????????????????????????????????  
			notification.audioStreamType= android.media.AudioManager.ADJUST_LOWER; 
		}
		else{
			notification.defaults=Notification.DEFAULT_LIGHTS;
		}

		notificationManager.notify(R.string.app_name, notification);
	}

	private void showNotifcationForLink(Context context,String title,String content,boolean hasSound,Bundle bundle){
		NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);               
//		Notification notification = new Notification(R.drawable.ic_launcher, title, System.currentTimeMillis());
//		notification.flags = Notification.FLAG_AUTO_CANCEL;
		Intent i = new Intent();        
		i.setAction("android.intent.action.VIEW");    
		String url =  bundle.getString("url");
		Uri content_url = Uri.parse(url);   
		i.setData(content_url); 
		if(bundle != null){
			i.putExtras(bundle);
		}
		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);           
		//PendingIntent
		PendingIntent contentIntent = PendingIntent.getActivity(context,R.string.app_name, i, PendingIntent.FLAG_UPDATE_CURRENT);
//		notification.setLatestEventInfo(context,title, content,contentIntent);

		Notification notification = new Notification.Builder(context)
				.setContentTitle(title)
				.setContentText(content)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentIntent(contentIntent)
				.build();
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		// ?????????????????? 
		if(hasSound){
			notification.defaults=Notification.DEFAULT_ALL;
			// audioStreamType????????????AudioManager????????????????????????????????????  
			notification.audioStreamType= android.media.AudioManager.ADJUST_LOWER; 
		}
		else{
			notification.defaults=Notification.DEFAULT_LIGHTS;
		}

		notificationManager.notify(R.string.app_name, notification);
	}

	private boolean hasSound(){
		//??????
		String[] weekdayArray = application.getQueitWeekdayArray();//0~8,0???"",1~7????????????~?????????
		Logger.i("weekdayArray:"+weekdayArray[0]+weekdayArray[1]
				+weekdayArray[2]+weekdayArray[3]
						+weekdayArray[4]+weekdayArray[5]
								+weekdayArray[6]+weekdayArray[7]);
		Calendar calendar = Calendar.getInstance();
		int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);//1~7????????????~?????????
		Logger.i("dayOfWeek:" +dayOfWeek);
		if(weekdayArray[dayOfWeek].equals("1")){
			//???????????????
			return false;
		}
		//??????????????????????????????
		//??????????????????

		//??????
		int startTime = application.getQueitStartTime();
		int continuedTime = application.getQueitContinuedTime();
		Logger.i("startTime:"+startTime);
		Logger.i("continuedTime:"+continuedTime);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		Logger.i("hour???"+hour);
		if(continuedTime == 0){
			Logger.i("???????????????0???????????????????????????");
			return true;
		}
		if(continuedTime == 24){
			Logger.i("???????????????24??????????????????????????????");
			return false;
		}
		if(startTime + continuedTime >= 24){
			if((hour >startTime && hour <= 24)
					||(hour<=startTime+continuedTime-24)){
				Logger.i("(hour >startTime && hour <= 24)||(hour<=startTime+continuedTime-24))");
				return false;
			}
			else{
				return true;
			}
		}
		else{
			if(hour>=startTime && hour <=startTime+continuedTime){
				Logger.i("hour>=startTime && hour <=startTime+continuedTime");
				return false;
			}
			else{
				return true;
			}
		}

	}
	/**
	 * ???????????????
	 * @param context
	 * @param bundle
	 */
	private void messageForNotice(Context context,Bundle bundle){
		String title = bundle.getString(JPushInterface.EXTRA_TITLE);
		String content = bundle.getString(JPushInterface.EXTRA_MESSAGE);
		boolean hasShoud = hasSound();
		Logger.i("?????????" +hasShoud);
		String extra = bundle.getString(JPushInterface.EXTRA_EXTRA);
		Logger.i("????????????????????????"+ extra);
		NotificationExtraBean noticficationExtraBean = application.getGson().fromJson(extra, new TypeToken<NotificationExtraBean>(){}.getType());
		String userId = application.getLoginInfoBean().getUserId().toString();
		if(noticficationExtraBean == null || noticficationExtraBean.getMessageId() ==null ||noticficationExtraBean.getMessageId().toString().trim().length() == 0){
			ToastUtil.showToast(context, "?????????????????????????????????");
			showNotifcation(context, title, content, hasShoud, WelcomeActivity.class,null);
		}
		else{
			if(userId != null && !userId.equals("0")){
				Bundle data = new Bundle();
				data.putString("noticeId", noticficationExtraBean.getMessageId());
				showNotifcation(context, title, content, hasShoud, NoticeDetailActivity.class,data);
			}
			else{
				ToastUtil.showToast(context, "?????????????????????????????????");
			}
		}
	}
	
	/**
	 *	?????????
	 * @param context
	 * @param bundle
	 */
	private void messageForMicroShare(Context context,Bundle bundle){
		String title = bundle.getString(JPushInterface.EXTRA_TITLE);
		String content = bundle.getString(JPushInterface.EXTRA_MESSAGE);
		boolean hasShoud = hasSound();
		Logger.i("?????????" +hasShoud);
		String extra = bundle.getString(JPushInterface.EXTRA_EXTRA);
		Logger.i("????????????????????????"+ extra);
		NotificationExtraBean noticficationExtraBean = application.getGson().fromJson(extra, new TypeToken<NotificationExtraBean>(){}.getType());
		String userId = application.getLoginInfoBean().getUserId().toString();
		if(noticficationExtraBean == null || noticficationExtraBean.getMessageId() ==null ||noticficationExtraBean.getMessageId().toString().trim().length() == 0){
			ToastUtil.showToast(context, "??????????????????????????????????????????");
			showNotifcation(context, title, content, hasShoud, WelcomeActivity.class,null);
		}
		else{
			if(userId != null && !userId.equals("0")){
				Bundle data = new Bundle();
				data.putInt("switcherViewId", R.id.switcher_index_micro_share);
				showNotifcation(context, title, content, hasShoud, IndexModularActivity.class,data);
			}
			else{
				ToastUtil.showToast(context, "?????????????????????????????????");
			}
		}
	}

	/**
	 * ???????????????
	 * @param context
	 * @param bundle
	 */
	private void messageForHelpFound(Context context,Bundle bundle){
		String title = bundle.getString(JPushInterface.EXTRA_TITLE);
		String content = bundle.getString(JPushInterface.EXTRA_MESSAGE);
		boolean hasShoud = hasSound();
		Logger.i("?????????" +hasShoud);
		String extra = bundle.getString(JPushInterface.EXTRA_EXTRA);
		Logger.i("????????????????????????"+ extra);
		NotificationExtraBean noticficationExtraBean = application.getGson().fromJson(extra, new TypeToken<NotificationExtraBean>(){}.getType());
		if(noticficationExtraBean == null || noticficationExtraBean.getMessageId() ==null ||noticficationExtraBean.getMessageId().toString().trim().length() == 0){
			ToastUtil.showToast(context, "?????????????????????????????????");
			showNotifcation(context, title, content, hasShoud, WelcomeActivity.class,null);
		}
		else{
			Bundle data = new Bundle();
			data.putString("contributeId", noticficationExtraBean.getMessageId());
			showNotifcation(context, title, content, hasShoud, HelpFoundDetailActivity.class,data);
		}
	}

	/**
	 * ?????????????????????
	 * @param context
	 * @param bundle
	 */
	private void messageForPushMessage(Context context,Bundle bundle){
		String title = bundle.getString(JPushInterface.EXTRA_TITLE);
		String content = bundle.getString(JPushInterface.EXTRA_MESSAGE);
		boolean hasShoud = hasSound();
		Logger.i("?????????" +hasShoud);
		String extra = bundle.getString(JPushInterface.EXTRA_EXTRA);
		Logger.i("????????????????????????"+ extra);
		NotificationExtraBean noticficationExtraBean = application.getGson().fromJson(extra, new TypeToken<NotificationExtraBean>(){}.getType());
		if(noticficationExtraBean == null || noticficationExtraBean.getMessageId() ==null ||noticficationExtraBean.getMessageId().toString().trim().length() == 0){
			ToastUtil.showToast(context, "???????????????????????????????????????");
			showNotifcation(context, title, content, hasShoud, WelcomeActivity.class,null);
		}
		else{
			Bundle data = new Bundle();
			data.putString("contributeId", noticficationExtraBean.getMessageId());
			data.putString("title","?????????");
			data.putString("modularValue","1" );
			showNotifcation(context, title, content, hasShoud, PushMessageListActivity.class,data);
		}
	}

	/**
	 * ??????????????????
	 * @param context
	 * @param bundle
	 */
	private void messageForLink(Context context,Bundle bundle){
		String title = bundle.getString(JPushInterface.EXTRA_TITLE);
		String content = bundle.getString(JPushInterface.EXTRA_MESSAGE);
		boolean hasShoud = hasSound();
		Logger.i("?????????" +hasShoud);
		String extra = bundle.getString(JPushInterface.EXTRA_EXTRA);
		Logger.i("????????????????????????"+ extra);
		NotificationExtraBean noticficationExtraBean = application.getGson().fromJson(extra, new TypeToken<NotificationExtraBean>(){}.getType());
		if(noticficationExtraBean == null || noticficationExtraBean.getMessageId() ==null ||noticficationExtraBean.getMessageId().toString().trim().length() == 0){
			ToastUtil.showToast(context, "???????????????????????????????????????");
			showNotifcation(context, title, content, hasShoud, WelcomeActivity.class,null);
		}
		else{
			Bundle data = new Bundle();
			data.putString("url",noticficationExtraBean.getUrl());
			showNotifcationForLink(context, title, content, hasShoud,data);
		}
	}
}
