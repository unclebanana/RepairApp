package com.wutong.repair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.tsz.afinal.FinalHttp;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import cn.jpush.android.api.JPushInterface;

import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.wutong.androidprojectlibary.http.util.CloudHttpClient;
import com.wutong.androidprojectlibary.http.util.FinalHttpPlus;
import com.wutong.androidprojectlibary.http.util.HttpClientManager;
import com.wutong.androidprojectlibary.widget.util.ToastUtil;
import com.wutong.repair.connection.CommonHttpUrlActionManager;
import com.wutong.repair.data.bean.FunctionBean;
import com.wutong.repair.data.bean.LoginInfoBean;
import com.wutong.repair.data.bean.ModularNetworkBean;
import com.wutong.repair.util.Action2IntegerUtil;
import com.wutong.repair.util.CipherUtil;
import com.wutong.repair.util.Logger;
import com.wutong.repair.util.SettingConfig;
import com.wutong.repairfjnu.BuildConfig;
import com.wutong.repairfjnu.R;

public class RepairApplication extends Application implements OnSharedPreferenceChangeListener{

	public final static int NOT_CLEAR_NOTIFICATION_ID = 998;

	private HttpClientManager httpClientManager;
	private CloudHttpClient cloudHttpClient;
	private CommonHttpUrlActionManager commonHttpUrlActionManager; 

	//
	private FinalHttp finalHttp;

	private String domainUrl;

	private String appFlag;
	private Map<String, List<FunctionBean>> permissionMap;
	private Map<String, String> permissionAllMap;
	private List<ModularNetworkBean> modularNetworkBeanAllList;//????????????all
	private List<ModularNetworkBean> modularNetworkBeanMainList;//?????????????????????index?????????????????????????????????more???
	private List<ModularNetworkBean> modularNetworkBeanExtraList;//?????????????????????more?????????
	private List<ModularNetworkBean> modularNetworkBeanTempExtraList;//??????????????????????????????????????????????????????
	private Map<String, List<String>> runtimePermissionMap;//?????????????????????
	private ModularFragmentManager modularFragmentManager;
	/**
	 * ???????????????????????? 20
	 */
	private final int pagingSize = 20;

	private final int limitForLostFound = 15;

	private final long intervalMillis = 86400000;//?????????30000????????????3600000?????????86400000?????????259200000?????????604800000
	/**
	 * ??????????????????????????????????????????????????????????????????1?????????????????????0?????????
	 */
	private final String DEFAULT_WEEKDAY = "1000000";
	/**
	 * ?????????????????????????????????23??????????????????9??????
	 */
	private final String DEFAULT_PERIOD = "23,9";

	private DisplayImageOptions simpleDisplayImageOptions;//?????????????????????
	private ImageLoader imageLoader;
	/**
	 * ????????????
	 */
	private SharedPreferences defaultPreferences;
	/**
	 * ????????????-user
	 */
	private LoginInfoBean loginInfoBean;
	/**
	 * ????????????????????????????????????["","0","1",??????]
	 */
	private String[] queitWeekdayArray;
	/**
	 * ?????????????????????
	 */
	private int queitStartTime;
	/**
	 * ?????????????????????
	 */
	private int queitContinuedTime;

	private long lastCheckUpdateTime;
	/**
	 * ?????????gson??????
	 */
	private Gson gson;


	/**
	 * ????????????
	 */
	private int skinType = 1;//0????????????1?????????


	private AlarmManager updateAlarmManager;
	private PendingIntent updatePendIntent;
	private Intent updateIntent;

	public void setLoginInfoBean(Context context,LoginInfoBean loginInfoBean) {
		this.loginInfoBean = loginInfoBean;

		permissionMap =loginInfoBean.getPermission();
		permissionAllMap = new HashMap<String, String>();
		Collection<List<FunctionBean>> functionBeanListCollection = permissionMap.values();
		for(List<FunctionBean> functionBeanList:functionBeanListCollection){
			for(FunctionBean functionBean:functionBeanList){
				permissionAllMap.put(functionBean.getUrl(), functionBean.getUrl());
			}
		}


		//
		modularNetworkBeanAllList.clear();
		modularNetworkBeanMainList.clear();
		modularNetworkBeanExtraList.clear();
		modularNetworkBeanTempExtraList.clear();
		//
		List<ModularNetworkBean> modularNetworkBeanList =loginInfoBean.getModulars();
		if(modularNetworkBeanList != null){
			for(ModularNetworkBean networkBean:modularNetworkBeanList){
				//????????????????????????????????????????????????????????????
				if(networkBean.getUrl().equals(getString(R.string.modular_url_applicant_repair_all))){
					//???????????????????????????
					ModularNetworkBean modularNetworkBean = new ModularNetworkBean();
					modularNetworkBean.setModularName("???????????????");
					modularNetworkBean.setUrl(getString(R.string.modular_url_not_disturb_setting));
					modularNetworkBeanTempExtraList.add(modularNetworkBean);


				}
				else if(networkBean.getUrl().equals(getString(R.string.modular_url_repair_repair_all))){
					//?????????????????????
					ModularNetworkBean modularNetworkBean = new ModularNetworkBean();
					modularNetworkBean.setModularName("???????????????");
					modularNetworkBean.setUrl(getString(R.string.modular_url_not_disturb_setting));
					modularNetworkBeanTempExtraList.add(modularNetworkBean);

					modularNetworkBean = new ModularNetworkBean();
					modularNetworkBean.setModularName("??????????????????");
					modularNetworkBean.setUrl(getString(R.string.modular_url_fast_message_setting));
					modularNetworkBeanTempExtraList.add(modularNetworkBean);

					modularNetworkBean = new ModularNetworkBean();
					modularNetworkBean.setModularName("????????????");
					modularNetworkBean.setUrl(getString(R.string.modular_url_material_info));
					modularNetworkBeanTempExtraList.add(modularNetworkBean);

				}
				
				//????????????
				if(!networkBean.getUrl().equals(getString(R.string.modular_url_hom_page))
						&& !networkBean.getUrl().equals(getString(R.string.modular_url_micro_share))
						&& !networkBean.getUrl().equals(getString(R.string.modular_url_help_found_list))
						&& !networkBean.getUrl().equals(getString(R.string.modular_url_help_lost))
						&& !networkBean.getUrl().equals(getString(R.string.modular_url_my_publish_lost_or_found))){
					modularNetworkBeanAllList.add(networkBean);
				}
				
			}
		}
		else{
			modularNetworkBeanAllList.clear();
			//?????????????????????

		}

		packModularMainAndExtra();

		Logger.i("modularNetworkBeanAllList:" +modularNetworkBeanAllList);
		savePermissionConfig(permissionAllMap);
	}

	public void repalaceAppFlagAndDomain(String appFlag,String domain){
		this.appFlag = appFlag;
		this.commonHttpUrlActionManager.setDomainUrl(domain);
	}

	/**
	 * ???????????????
	 */
	private void packModularMainAndExtra(){
		int modularSize = modularNetworkBeanAllList.size();
		if(modularSize >3){
			modularNetworkBeanMainList.add(modularNetworkBeanAllList.remove(0));
			modularNetworkBeanMainList.add(modularNetworkBeanAllList.remove(0));
			modularNetworkBeanMainList.add(modularNetworkBeanAllList.remove(0));

			ModularNetworkBean modularNetworkBean = new ModularNetworkBean();
			modularNetworkBean.setModularName("??????");
			modularNetworkBean.setUrl(getString(R.string.modular_url_more));
			modularNetworkBeanMainList.add(modularNetworkBean);

			modularNetworkBeanExtraList.addAll(modularNetworkBeanAllList);
			modularNetworkBeanAllList.clear();
		}
		else{
			modularNetworkBeanMainList.addAll(modularNetworkBeanAllList);

			ModularNetworkBean modularNetworkBean = new ModularNetworkBean();
			modularNetworkBean.setModularName("??????");
			modularNetworkBean.setUrl(getString(R.string.modular_url_more));
			modularNetworkBeanMainList.add(modularNetworkBean);

			modularNetworkBeanAllList.clear();
		}

		//
		modularNetworkBeanExtraList.addAll(modularNetworkBeanTempExtraList);
	}

	public String getAppFlag() {
		return appFlag;
	}

	public int getPagingSize() {
		return pagingSize;
	}

	public int getLimitForLostFound() {
		return limitForLostFound;
	}

	public DisplayImageOptions getSimpleDisplayImageOptions() {
		return simpleDisplayImageOptions;
	}

	public SharedPreferences getDefaultPreferences() {
		return defaultPreferences;
	}

	public String[] getQueitWeekdayArray() {
		return queitWeekdayArray;
	}

	public int getQueitStartTime() {
		return queitStartTime;
	}

	public int getQueitContinuedTime() {
		return queitContinuedTime;
	}

	public Gson getGson() {
		return gson;
	}

	public ImageLoader getImageLoader() {
		return imageLoader;
	}

	public List<ModularNetworkBean> getModularNetworkBeanMainList() {
		return modularNetworkBeanMainList;
	}
	public List<ModularNetworkBean> getModularNetworkBeanExtraList() {
		return modularNetworkBeanExtraList;
	}

	public ModularFragmentManager getModularFragmentManager() {
		return modularFragmentManager;
	}

	public LoginInfoBean getLoginInfoBean() {
		return loginInfoBean;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		CrashHandler crashHandler = CrashHandler.getInstance();  
		crashHandler.init(this);
		applicationInit();
		initQuietTime();
		checkUpdate();
	}

	public HttpClientManager getHttpClientManager() {
		return httpClientManager;
	}

	public FinalHttp getFinalHttp() {
		return finalHttp;
	}

	public int getSkinType() {
		return skinType;
	}

	public String getDomainUrl() {
		return domainUrl;
	}

	public CommonHttpUrlActionManager getCommonHttpUrlActionManager() {
		return commonHttpUrlActionManager;
	}

	private void applicationInit(){
		if(appFlag == null){
			appFlag = getString(R.string.app_flag);
		}
		if(appFlag.equals("fjnu")){
			skinType = 1;
		}
		else if(appFlag.equals("fjcc")){
			skinType = 1;
		}
		else if(appFlag.equals("fjmk")){
			skinType = 0;
		}
		else if(appFlag.equals("fjmk")){
			skinType = 0;
		}

		this.finalHttp = new FinalHttpPlus(this);
		domainUrl = getString(R.string.http_url_domain);
		if(cloudHttpClient == null){
			cloudHttpClient = new CloudHttpClient();
		}
		if(httpClientManager == null){
			httpClientManager = new HttpClientManager(cloudHttpClient);
		}
		String domainUrl = getString(R.string.http_url_domain);
		if(commonHttpUrlActionManager == null){
			commonHttpUrlActionManager = new CommonHttpUrlActionManager(getApplicationContext(), domainUrl);
		}
		if(loginInfoBean == null){
			loginInfoBean = new LoginInfoBean();
		}
		if(gson == null){
			gson = new Gson();
		}
		if(runtimePermissionMap == null){
			runtimePermissionMap = new HashMap<String, List<String>>();
		}
		if(modularFragmentManager == null){
			modularFragmentManager = new ModularFragmentManager(getApplicationContext());
		}
		if(imageLoader == null){
			imageLoader = ImageLoader.getInstance();
			switch (this.skinType) {
			case 1:
				simpleDisplayImageOptions = new DisplayImageOptions.Builder().showImageOnFail(R.drawable.image_failed).showStubImage(R.drawable.empty_picture).cacheInMemory(true).cacheOnDisc(true).build();

				break;

			default:
				simpleDisplayImageOptions = new DisplayImageOptions.Builder().showImageOnFail(R.drawable.spring_horse_image_failed).showStubImage(R.drawable.empty_picture).cacheInMemory(true).cacheOnDisc(true).build();

				break;
			}
			ImageLoaderConfiguration configuration = ImageLoaderConfiguration.createDefault(getApplicationContext());
			imageLoader.init(configuration );
		}
		ToastUtil.applicationInit(getApplicationContext(), R.drawable.ic_launcher);
		modularNetworkBeanAllList = new ArrayList<ModularNetworkBean>();
		modularNetworkBeanMainList = new ArrayList<ModularNetworkBean>();
		modularNetworkBeanExtraList = new ArrayList<ModularNetworkBean>();
		modularNetworkBeanTempExtraList = new ArrayList<ModularNetworkBean>();
//		refreshJpushInfo();

		defaultPreferences = PreferenceManager.getDefaultSharedPreferences(RepairApplication.this);
		defaultPreferences.registerOnSharedPreferenceChangeListener(this);
	}



	private void initQuietTime(){
		String slientWeekday = defaultPreferences.getString(SettingConfig.QuietTime.SLIENT_WEEKDAY, DEFAULT_WEEKDAY);
		String slientPeriod = defaultPreferences.getString(SettingConfig.QuietTime.SLIENT_PERIOD, DEFAULT_PERIOD);
		Editor editor = defaultPreferences.edit();
		editor.putString(SettingConfig.QuietTime.SLIENT_WEEKDAY, slientWeekday);
		editor.putString(SettingConfig.QuietTime.SLIENT_PERIOD, slientPeriod);
		editor.commit();
		queitWeekdayArray = queitWeekdayStringToArray(slientWeekday);
		queitPeriodStringToInt(slientPeriod);
		Logger.i("init slientPeriod: " +slientPeriod+"|slientWeekday:" + slientWeekday);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if(key.equals(SettingConfig.QuietTime.SLIENT_PERIOD)){
			queitPeriodStringToInt(sharedPreferences.getString(key, DEFAULT_PERIOD));
		}
		else if(key.equals(SettingConfig.QuietTime.SLIENT_WEEKDAY)){
			queitWeekdayArray = queitWeekdayStringToArray(sharedPreferences.getString(key, DEFAULT_WEEKDAY));
		}
	}

	/**
	 * ???????????????????????????????????????
	 * @param slientWeekday
	 * @return
	 */
	private String[] queitWeekdayStringToArray(String slientWeekday){
		String[] weekdayArray = slientWeekday.split("");
		if(weekdayArray.length == 8 && weekdayArray[0].toString().trim().length() == 0){
			return weekdayArray;
		}
		else{
			return new String[]{"","1","0","0","0","0","0","0"};
		}
	}
	/**
	 * ???????????????????????????????????????????????????
	 * @param slientPeriod
	 */
	private void queitPeriodStringToInt(String slientPeriod){
		String[] periodArray = slientPeriod.split(",");
		queitStartTime = Integer.valueOf(periodArray[0]);
		queitContinuedTime = Integer.valueOf(periodArray[1]);
	} 

	private void checkUpdate(){
		if(lastCheckUpdateTime == 0){
			lastCheckUpdateTime = defaultPreferences.getLong(SettingConfig.UpdateInfo.LAST_UPDATE_TIME, 0);
		}
		if(updateAlarmManager == null){
			updateAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		}
		if(updateIntent == null){
			updateIntent = new Intent(Action2IntegerUtil.ACTION_CHECK_UPDATE_CHECK);
			updateIntent.putExtra("tag", new Date().getTime());
		}
		if(updatePendIntent == null){
			updatePendIntent = PendingIntent.getBroadcast(getApplicationContext(),  
					0, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT);  
		}
		updateAlarmManager.cancel(updatePendIntent);
		long elapsedRealTime = SystemClock.elapsedRealtime();
		long x = lastCheckUpdateTime % intervalMillis;
		long y = elapsedRealTime % intervalMillis;
		long triggerAtTime = intervalMillis -(x- y);
		Logger.i("triggerAtTime :" + triggerAtTime + "elapsedRealTime:" +elapsedRealTime +"lastCheckUpdateTime:" +lastCheckUpdateTime);
		updateAlarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, triggerAtTime, intervalMillis , updatePendIntent);
	}

	/**
	 * ??????Jpush??????
	 */
//	public void refreshJpushInfo(){
//		//
//		defaultPreferences = PreferenceManager.getDefaultSharedPreferences(RepairApplication.this);
//		defaultPreferences.registerOnSharedPreferenceChangeListener(this);
//		JPushInterface.setDebugMode(BuildConfig.DEBUG); 	//??????????????????,????????????????????????
//		JPushInterface.init(this);
//		String uid = defaultPreferences.getString(SettingConfig.PersonInfo.UID, null);
//		Logger.i("?????????uid:" + uid);
//		if(uid != null){
//			uid = CipherUtil.Base64Cipher.decript(uid);
//			Logger.i("?????????uid:" + uid);
//			loginInfoBean.setUserId(Integer.valueOf(uid));
//			Logger.i("????????????:" + uid);
//			Set<String> tags = new HashSet<String>();
//			//
//			//
//			tags.add(appFlag);
//			JPushInterface.setAliasAndTags(RepairApplication.this, appFlag+uid, tags);
//		}
//		else{
//			Logger.i("??????????????????:" + uid);
//		}
//	}

	/**
	 * ?????????????????????,?????????
	 * @param permisson
	 * @return
	 */
	public boolean hasPermission(String permisson){
		if(permissionAllMap == null){
			if(loginInfoBean.isLoginSuccess()){
				return false;
			}
			else{
				loadPermissionConfig();
			}
		}
		return permissionAllMap.containsKey(permisson);
	}

	/**
	 * ??????????????????????????????,????????????????????????
	 * @param permisson
	 * @return
	 */
	public boolean hasRunTimePermission(String pageKey,String permisson){
		List<String> permissions = runtimePermissionMap.get(pageKey);
		if(permissions == null || permissions.isEmpty()){
			return false;
		}
		else{
			return permissions.contains(permisson);
		}
	}

	/**
	 * ?????????????????????
	 * @param pageKey
	 * @param permisson
	 */
	public void putRuntimePermission(String pageKey,String permisson){
		List<String> permissions = runtimePermissionMap.get(pageKey);
		if(permissions == null){
			permissions = new ArrayList<String>();
		}

		if(permissions.contains(permisson)){
			throw new PermissionRepeatException("????????????????????????");
		}
		permissions.add(permisson);

		runtimePermissionMap.put(pageKey, permissions);
	}
	/**
	 * ????????????????????????
	 * @param pageKey
	 */
	public void removeRuntimePermission(String pageKey){
		runtimePermissionMap.remove(pageKey);
	}

	/**
	 * ?????????????????????
	 * @param permisson
	 * @return
	 */
	public boolean hasPermission(String modularCode,String permisson){
		List<FunctionBean> functionBeanList = permissionMap.get(modularCode);
		return functionBeanList.contains(permisson);
	}


	private void savePermissionConfig(Map<String, String> map){
		SharedPreferences permissionPreferences = getSharedPreferences(SettingConfig.PERMISSION_CONFIG, MODE_PRIVATE);
		Editor editor = permissionPreferences.edit();
		editor.clear();
		if(map != null && !map.isEmpty()){
			Set<String> keySet  = map.keySet();
			for(String key: keySet){
				editor.putString(key, map.get(key));
			}

		}
		editor.commit();

	}

	private void loadPermissionConfig(){
		SharedPreferences permissionPreferences = getSharedPreferences(SettingConfig.PERMISSION_CONFIG, MODE_PRIVATE);
		Map<String, ?> map = permissionPreferences.getAll();
		if(permissionAllMap == null){
			permissionAllMap = new HashMap<String, String>();
		}
		if(map != null && !map.isEmpty()){
			Set<String> keySet  = map.keySet();
			for(String key: keySet){
				permissionAllMap.put(key, map.get(key).toString());
			}

		}
	}

	/**
	 * ?????????????????????
	 */
	public void logoutInit(){
		SharedPreferences preferences = this.getSharedPreferences(SettingConfig.LOGIN_CONFIG, Context.MODE_PRIVATE);
		preferences.edit().remove(SettingConfig.LoginForm.AUTO_SIGN_IN).commit();
		this.loginInfoBean =null;
		this.permissionMap = null;
		this.permissionAllMap = null;
	}


}