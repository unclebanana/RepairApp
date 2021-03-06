package com.wutong.repair.activity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.google.gson.reflect.TypeToken;
import com.wutong.androidprojectlibary.http.bean.HttpFormBean;
import com.wutong.androidprojectlibary.http.util.HttpAsyncTask;
import com.wutong.androidprojectlibary.http.util.HttpAsyncTask.OnDealtListener;
import com.wutong.androidprojectlibary.widget.util.ToastUtil;
import com.wutong.common.widget.PullToRefreshView;
import com.wutong.common.widget.PullToRefreshView.OnFooterRefreshListener;
import com.wutong.common.widget.PullToRefreshView.OnHeaderRefreshListener;
import com.wutong.repair.BaseActivity;
import com.wutong.repair.data.bean.MaterialBean;
import com.wutong.repair.data.bean.MaterialGrantBean;
import com.wutong.repair.data.bean.ResponseBean;
import com.wutong.repair.dictionary.QueryMaterialDict;
import com.wutong.repair.util.HttpResponseUtil;
import com.wutong.repair.util.StringUtil;
import com.wutong.repair.util.TimeUtil;
import com.wutong.repairfjnu.R;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import android.widget.AdapterView.OnItemClickListener;

public class MaterialInfoActivity extends BaseActivity {
	

	private static final int SWITCHER_MY_MATERIAL = 0;
	private static final int SWITCHER_QUERY_MATERIAL = 1;

	private ImageView titlebarBackView;
	private TextView titlebarTitleView;

	private ViewPager mViewPager;
	private PagerAdapter pagerAdapter;

	private List<MaterialGrantBean> materialGrantBeanList;
	private MaterialGrantBaseAdapter grantAdapter;
	private ListView mMaterialGrantView;
	private PullToRefreshView queryMaterialPullToRefreshView;

	private List<MaterialBean> materialBeanList;
	private MaterialBaseAdapter materialAdapter;
	private ListView mMaterialListView;
	private PullToRefreshView myMaterialPullToRefreshView;

	private TextView myMaterialSwitcherView;
	private TextView queryMaterialSwitcherView;
	
	private View myMaterialPageContentView;
	private View queryMaterialPageContentView;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_material_info);
		setStatPageName(mContext, R.string.title_activity_material_info);
		titleBarInit();
		viewInit();
	}

	@Override
	protected void onResume() {
		super.onResume();
		setupData();
	}


	private void titleBarInit(){
		titlebarTitleView = ((TextView)findViewById(R.id.titlebar_title));
		titlebarTitleView.setText(R.string.title_activity_material_info);
		titlebarBackView = (ImageView) findViewById(R.id.titlebar_back);
		titlebarBackView.setVisibility(ViewGroup.VISIBLE);
		titlebarBackView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				MaterialInfoActivity.this.onBackPressed();

			}
		});
		


	}

	private void viewInit(){
		mViewPager = (ViewPager) findViewById(R.id.viewpager_material_info);
		myMaterialSwitcherView = (TextView) findViewById(R.id.material_info_switcher_my_materials);
		queryMaterialSwitcherView = (TextView) findViewById(R.id.material_info_switcher_query_materials);

		//
		pagerAdapter = new MaterailInfoPagerAdapter();
		mViewPager.setAdapter(pagerAdapter);
		//
		materialBeanList = new ArrayList<MaterialBean>();
		materialAdapter = new MaterialBaseAdapter(materialBeanList);
		//
		materialGrantBeanList = new ArrayList<MaterialGrantBean>();
		grantAdapter = new MaterialGrantBaseAdapter(materialGrantBeanList);
		//
		
		myMaterialPageContentView = LayoutInflater.from(mContext).inflate(R.layout.viewpager_my_material_list, null);
		myMaterialPullToRefreshView = (PullToRefreshView) myMaterialPageContentView.findViewById(R.id.my_material_pull_refresh_view);
		mMaterialListView = (ListView) myMaterialPageContentView.findViewById(R.id.material_list);
		View emptyMyMaterial = myMaterialPageContentView.findViewById(R.id.my_material_empty);
		mMaterialListView.setEmptyView(emptyMyMaterial);
		emptyMyMaterial.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				loadMaterialListAsyncHttp();
				
			}
		});
		mMaterialListView.setAdapter(materialAdapter);
		queryMaterialPageContentView = LayoutInflater.from(mContext).inflate(R.layout.viewpager_material_grant_history, null);
		queryMaterialPullToRefreshView = (PullToRefreshView) queryMaterialPageContentView.findViewById(R.id.query_material_pull_refresh_view);
		mMaterialGrantView = (ListView) queryMaterialPageContentView.findViewById(R.id.grant_list);
		mMaterialGrantView.setAdapter(grantAdapter);
		
		View emptyQueryMaterial = queryMaterialPageContentView.findViewById(R.id.query_material_empty);
		mMaterialGrantView.setEmptyView(emptyQueryMaterial);
		emptyQueryMaterial.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				loadMaterialGrantAsyncHttp();
				
			}
		});
		myMaterialPullToRefreshView.setOnHeaderRefreshListener(new OnHeaderRefreshListener() {
			
			@Override
			public void onHeaderRefresh(PullToRefreshView view) {
				loadMaterialListAsyncHttp();
				
			}
		});
		myMaterialPullToRefreshView.setOnFooterRefreshListener(new OnFooterRefreshListener() {
			
			@Override
			public void onFooterRefresh(PullToRefreshView view) {
				loadMaterialListAsyncHttp();
				
			}
		});
		
		queryMaterialPullToRefreshView.setOnHeaderRefreshListener(new OnHeaderRefreshListener() {
			
			@Override
			public void onHeaderRefresh(PullToRefreshView view) {
				loadMaterialGrantAsyncHttp();
				
			}
		});
		
		queryMaterialPullToRefreshView.setOnFooterRefreshListener(new OnFooterRefreshListener() {
			
			@Override
			public void onFooterRefresh(PullToRefreshView view) {
				loadMaterialGrantAsyncHttp();
				
			}
		});
		
		
		mMaterialGrantView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parentView, View view, int position,
					long id) {
				Intent intent = new Intent();
				intent.setClass(mContext, QueryMaterialsActivity.class);
				intent.putExtra("grantId", String.valueOf(id));
				//
				MaterialGrantBean materialGrantBean = materialGrantBeanList.get(position);
				Bundle bundle = new Bundle();
				bundle.putSerializable("materialGrantBean", (Serializable)materialGrantBean);
				intent.putExtras(bundle);
				startActivity(intent);

			}
		});
		
		myMaterialSwitcherView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mViewPager.setCurrentItem(SWITCHER_MY_MATERIAL);

			}
		});
		queryMaterialSwitcherView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mViewPager.setCurrentItem(SWITCHER_QUERY_MATERIAL);

			}
		});

		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				switcherChange(position);

			}

			@Override
			public void onPageScrolled(int position, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int status) {

			}
		});

		//
	}

	private void setupData(){
		loadMaterialListAsyncHttp();
		loadMaterialGrantAsyncHttp();

	}


	private class MaterailInfoPagerAdapter extends PagerAdapter{

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public int getItemPosition(Object object) {
			return POSITION_NONE;
		}


		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			((ViewPager)container).removeView((View)object);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			View viewPagerView = null;
			switch (position) {
			case 0://????????????
				viewPagerView = myMaterialPageContentView;
				break;
			case 1://????????????
				viewPagerView = queryMaterialPageContentView;
				break;
			default:
				break;
			}
			((ViewPager)container).addView(viewPagerView);
			return viewPagerView;
		}

	}

	private class MaterialGrantBaseAdapter extends BaseAdapter{

		private List<MaterialGrantBean> list;
		public MaterialGrantBaseAdapter(List<MaterialGrantBean> list) {
			super();
			this.list = list;
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return Long.valueOf(list.get(position).getId());
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			MaterialGrantBean materialGrantBean = list.get(position);
			convertView = LayoutInflater.from(mContext).inflate(R.layout.listview_material_grant, null);
			TextView grantCodeView = (TextView) convertView.findViewById(R.id.grant_code);
			TextView grantTimeView = (TextView) convertView.findViewById(R.id.grant_time);
			TextView grantStatusView = (TextView) convertView.findViewById(R.id.grant_status_name);
			String time = StringUtil.time2FullStringByStringType(materialGrantBean.getGrantTime(), TimeUtil.SQL_PATTERN_TIME);
			time = time.substring(0, time.toString().trim().length()-2);
			grantTimeView.setText(time);
			grantStatusView.setText(QueryMaterialDict.getStatusName(materialGrantBean.getStatus()));
			grantCodeView.setText( materialGrantBean.getGrantCode());
			if(materialGrantBean.getStatus() == null){
				ToastUtil.showToast(mContext, "??????????????????????????????????????????");
			}
			else if(materialGrantBean.getStatus().equals("2")){
				grantStatusView.setTextColor(getResources().getColor(R.color.common_status_red));
			}
			
			return convertView;
		}

	}

	private class MaterialBaseAdapter extends BaseAdapter{

		private List<MaterialBean> list;
		public MaterialBaseAdapter(List<MaterialBean> list) {
			super();
			this.list = list;
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return Long.valueOf(list.get(position).getId());
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			MaterialBean materialBean = list.get(position);
			convertView = LayoutInflater.from(mContext).inflate(R.layout.listview_my_material, null);
			TextView materialNameView = (TextView) convertView.findViewById(R.id.materail_name);
			TextView materialSpecView = (TextView) convertView.findViewById(R.id.materail_spec);
			TextView materialNumberView = (TextView) convertView.findViewById(R.id.materail_number);
			materialNameView.setText(materialBean.getMaterialName());
			materialSpecView.setText(materialBean.getSpecification());
			materialNumberView.setText(getString(R.string.format_material_select_number_unit,materialBean.getTotalNumber().toString(),materialBean.getUnit()));
			return convertView;
		}

	}

	private void loadMaterialGrantAsyncHttp(){
		HttpAsyncTask httpAsyncTask = new HttpAsyncTask(mContext,application.getHttpClientManager());
		HttpFormBean httpFormBean = new HttpFormBean();
		httpFormBean.setUrl(application.getCommonHttpUrlActionManager().getMaterialGrantListUrl());
		Collection<NameValuePair> params = new ArrayList<NameValuePair>();
		NameValuePair repairManIdPair = new BasicNameValuePair("repairManId", application.getLoginInfoBean().getUserId().toString());
		params.add(repairManIdPair);
		httpFormBean.setParams(params);
		httpAsyncTask.setOnDealtListener(new OnDealtListener() {

			@Override
			public void success(String resultResponse) {
				hideProgressDialog();
				ResponseBean<List<MaterialGrantBean>> responseBean = application.getGson().fromJson(resultResponse, new TypeToken<ResponseBean<List<MaterialGrantBean>>>(){}.getType());
				List<MaterialGrantBean> responseList = responseBean.getData();
				if(responseBean.isSuccess() == false){
					ToastUtil.showToast(mContext, responseBean.getMessage());
					return;
				}
				if(responseList == null){
					ToastUtil.showToast(mContext, R.string.error_data_null);
					return;
				}
				if(responseList != null && !responseList.isEmpty()){
					materialGrantBeanList.clear();
					grantAdapter.notifyDataSetChanged();
					materialGrantBeanList.addAll(responseList);
					grantAdapter.notifyDataSetChanged();
				}
				queryMaterialPullToRefreshView.onHeaderRefreshComplete();
				queryMaterialPullToRefreshView.onFooterRefreshComplete();
			}

			@Override
			public void failed(Exception exception) {
				hideProgressDialog();
				HttpResponseUtil.justToast(exception, mContext);
				queryMaterialPullToRefreshView.onHeaderRefreshComplete();
				queryMaterialPullToRefreshView.onFooterRefreshComplete();
			}

			@Override
			public void beforeDealt() {
				showProgressDialog("??????????????????");

			}
		});
		httpAsyncTask.execute(httpFormBean);
	}

	/**
	 * ?????????????????????????????????
	 */
	private void loadMaterialListAsyncHttp(){
		HttpAsyncTask httpAsyncTask = new HttpAsyncTask(mContext,application.getHttpClientManager());
		HttpFormBean httpFormBean = new HttpFormBean();
		httpFormBean.setUrl(application.getCommonHttpUrlActionManager().getRepairmanMaterialListUrl());
		Collection<NameValuePair> params = new ArrayList<NameValuePair>();
		NameValuePair repairManIdPair = new BasicNameValuePair("repairManId", application.getLoginInfoBean().getUserId().toString());
		params.add(repairManIdPair);
		httpFormBean.setParams(params);
		httpAsyncTask.setOnDealtListener(new OnDealtListener() {

			@Override
			public void success(String resultResponse) {
				hideProgressDialog();
				ResponseBean<List<MaterialBean>> responseBean = application.getGson().fromJson(resultResponse, new TypeToken<ResponseBean<List<MaterialBean>>>(){}.getType());
				List<MaterialBean> responseList = responseBean.getData();
				if(responseBean.isSuccess() == false){
					ToastUtil.showToast(mContext, responseBean.getMessage());
					return;
				}
				if(responseList == null){
					ToastUtil.showToast(mContext, R.string.error_data_null);
					return;
				}
				if(responseList != null && !responseList.isEmpty()){
					materialBeanList.clear();
					materialAdapter.notifyDataSetChanged();
					materialBeanList.addAll(responseList);
					materialAdapter.notifyDataSetChanged();
				}
				myMaterialPullToRefreshView.onHeaderRefreshComplete();
				myMaterialPullToRefreshView.onFooterRefreshComplete();
			}

			@Override
			public void failed(Exception exception) {
				hideProgressDialog();
				HttpResponseUtil.justToast(exception, mContext);
				myMaterialPullToRefreshView.onHeaderRefreshComplete();
				myMaterialPullToRefreshView.onFooterRefreshComplete();
			}

			@Override
			public void beforeDealt() {
				showProgressDialog("??????????????????");
			}
		});
		httpAsyncTask.execute(httpFormBean);
	}


	private void switcherChange(int position){
		myMaterialSwitcherView.setEnabled(true);
		queryMaterialSwitcherView.setEnabled(true);
		switch (position) {
		case SWITCHER_MY_MATERIAL:
			myMaterialSwitcherView.setEnabled(false);
			break;
		case SWITCHER_QUERY_MATERIAL:
			queryMaterialSwitcherView.setEnabled(false);
			break;
		}
	}
}
