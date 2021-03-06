package com.wutong.repair.activity;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.google.gson.reflect.TypeToken;
import com.wutong.androidprojectlibary.http.bean.HttpFormBean;
import com.wutong.androidprojectlibary.http.util.HttpAsyncTask;
import com.wutong.androidprojectlibary.http.util.HttpAsyncTask.OnDealtListener;
import com.wutong.androidprojectlibary.widget.util.ToastUtil;
import com.wutong.common.widget.CustomDialog;
import com.wutong.repair.BaseActivity;
import com.wutong.repairfjnu.R;
import com.wutong.repair.data.bean.ResponseBean;
import com.wutong.repair.util.ApplicationInfoUtil;
import com.wutong.repair.util.CommonOperateUtil;
import com.wutong.repair.util.InternetConnUtil;
import com.wutong.repair.util.MobileInfoUtil;
import com.wutong.repair.util.HttpResponseUtil;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;


public class FeedbackUsActivity extends BaseActivity {

	private ImageView titlebarBackView;
	private ImageView titlebarSubmitView;
	private TextView titlebarTitleView;

	private EditText mContentView;
	private EditText mEmailView;

	private CustomDialog submitDialog;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_feekback_us);
		setStatPageName(mContext, R.string.title_activity_feekback_us);
		titleBarInit();
		viewInit();
	}
	
	@Override
	public void onBackPressed() {
		CommonOperateUtil.hideIME(this);
		super.onBackPressed();
	}

	private void titleBarInit(){
		titlebarTitleView = ((TextView)findViewById(R.id.titlebar_title));
		titlebarTitleView.setText(R.string.title_activity_feekback_us);
		titlebarTitleView.setVisibility(ViewGroup.VISIBLE);
		titlebarBackView = (ImageView) findViewById(R.id.titlebar_back);
		titlebarBackView.setVisibility(ViewGroup.VISIBLE);
		titlebarBackView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				FeedbackUsActivity.this.onBackPressed();

			}
		});
		
		titlebarSubmitView = (ImageView) findViewById(R.id.titlebar_submit);
		titlebarSubmitView.setVisibility(ViewGroup.VISIBLE);
		titlebarSubmitView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				submit();
			}
		});

	}

	private void viewInit(){
		mContentView = (EditText) findViewById(R.id.feedback_us_content);
		mEmailView = (EditText) findViewById(R.id.feedback_us_contact_info);
	}


	private void submit(){
		//check
		if(mContentView.getText().toString().trim().toString().trim().length() == 0){
			ToastUtil.showToast(mContext, "?????????????????????????????????");
			return ;
		}
		//??????
		String message = "";
		if(mEmailView.getText().toString().trim().toString().trim().length() == 0){
			message +="???????????????????????????????????????????????????????????????????????????????????????\n";
		}
		message +="?????????????????????????????????????????????";
		if(submitDialog == null){
			submitDialog = new CustomDialog.Builder(mContext)
			.setTitle("????????????")
			.setMessage(message)
			.setPositiveButton("??????", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					submitFeedbackUsAsyncHttp();

				}
			})
			.setNegativeButton("??????", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					submitDialog.dismiss();

				}
			})
			.create();
		}
		submitDialog.show();
	}

	private void submitFeedbackUsAsyncHttp(){
		HttpAsyncTask httpAsyncTask = new HttpAsyncTask(mContext,application.getHttpClientManager());
		HttpFormBean httpFormBean = new HttpFormBean();
		httpFormBean.setUrl(application.getCommonHttpUrlActionManager().getFeedbackUsSendUrl());
		Collection<NameValuePair> params = new ArrayList<NameValuePair>();
		NameValuePair contentPair = new BasicNameValuePair("content", mContentView.getText().toString());
		params.add(contentPair);
		NameValuePair versionPair = new BasicNameValuePair("version", getString(R.string.app_name) + ApplicationInfoUtil.getVersionName(mContext));
		params.add(versionPair);
		NameValuePair phoneModelPair = new BasicNameValuePair("phoneModel", MobileInfoUtil.getMobileDevice());
		params.add(phoneModelPair);
		NameValuePair brandPair = new BasicNameValuePair("brand", MobileInfoUtil.getMobileBrand());
		params.add(brandPair);
		NameValuePair operatingSystemPair = new BasicNameValuePair("operatingSystem", MobileInfoUtil.getSystemVersionName());
		params.add(operatingSystemPair);
		String netState;
		if(InternetConnUtil.isWifiConnect(mContext)){
			netState = "WIFI";
		}
		else if(InternetConnUtil.isMobileConnect(mContext)){
			netState = "MOBILE";
		}
		else{
			netState = "NETWORK DISCONNECTED";
		}
		NameValuePair netStatePair = new BasicNameValuePair("netState", netState);
		params.add(netStatePair);
		NameValuePair suggestionPhonePair = new BasicNameValuePair("email", mEmailView.getText().toString());
		params.add(suggestionPhonePair);
		httpFormBean.setParams(params);
		httpAsyncTask.setOnDealtListener(new OnDealtListener() {

			@Override
			public void success(String resultResponse) {
				hideProgressDialog();
				ResponseBean responseBean = application.getGson().fromJson(resultResponse, new TypeToken<ResponseBean>(){}.getType());
				if(responseBean.isSuccess()){
					//?????????????????????
					CommonOperateUtil.hideIME(FeedbackUsActivity.this);
					ToastUtil.showToast(mContext, "???????????????");
					FeedbackUsActivity.this.finish();
				}
				else{
					ToastUtil.showToast(mContext, "???????????????");
				}
				titlebarSubmitView.setEnabled(true);
			}

			@Override
			public void failed(Exception exception) {
				HttpResponseUtil.justToast(exception, mContext);
				hideProgressDialog();
				titlebarSubmitView.setEnabled(true);
			}

			@Override
			public void beforeDealt() {
				titlebarSubmitView.setEnabled(false);
				showProgressDialog(R.string.tips_for_collecting_for_submit);
			}
		});
		httpAsyncTask.execute(httpFormBean);
	}

}
