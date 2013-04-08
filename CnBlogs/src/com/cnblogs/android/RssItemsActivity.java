package com.cnblogs.android;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.cnblogs.android.adapter.RssItemsAdapter;
import com.cnblogs.android.core.RssItemHelper;
import com.cnblogs.android.entity.RssItem;
/**
 * ������Ŀ
 * @author walkingp
 * @date:2012-3
 *
 */
public class RssItemsActivity extends BaseActivity {
	List<RssItem> listBlog = new ArrayList<RssItem>();

	ListView listView;
	private RssItemsAdapter adapter;// ����Դ
	List<RssItem> listRss;

	String itemTitle, itemUrl;

	Button btnBack;// ����
	ProgressBar bodyProgressBar;// ����ListView���ؿ�
	ImageButton btnRefresh;// ˢ�°�ť
	ProgressBar topProgressBar;// ���ذ�ť

	TextView txtAppTitle;// ����
	TextView txtNoData;// û������
	Resources res;// ��Դ
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.rss_list_layout);

		res = this.getResources();
		InitialControls();
		InitialData();
		BindControls();
		new PageTask().execute();
	}
	/**
	 * ��ʼ���б�
	 */
	private void InitialControls() {
		listView = (ListView) findViewById(R.id.rss_list);
		bodyProgressBar = (ProgressBar) findViewById(R.id.rssList_progressBar);

		btnBack = (Button) findViewById(R.id.rss_button_back);
		btnRefresh = (ImageButton) findViewById(R.id.rss_refresh_btn);
		topProgressBar = (ProgressBar) findViewById(R.id.rss_progressBar);
		txtAppTitle = (TextView) findViewById(R.id.txtAppTitle);
		txtNoData = (TextView) findViewById(R.id.txtNoData);
	}
	/**
	 * ��ʼ������
	 */
	void InitialData() {
		itemTitle = getIntent().getStringExtra("title");
		itemUrl = getIntent().getStringExtra("url");
	}
	/**
	 * ���¼�
	 */
	private void BindControls() {
		txtAppTitle.setText(itemTitle);
		// ����
		btnBack.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				RssItemsActivity.this.finish();
			}
		});
		// ˢ��
		btnRefresh.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new PageTask().execute();
			}
		});
		// �����ת
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				TextView tvTitle = (TextView) v
						.findViewById(R.id.recommend_text_title);
				TextView tvContent = (TextView) v
						.findViewById(R.id.recommend_text_full_text);
				TextView tvLink = (TextView) v
						.findViewById(R.id.recommend_text_url);
				String title = tvTitle.getText().toString();
				String content = tvContent.getText().toString();
				String link = tvLink.getText().toString();

				Intent intent = new Intent();
				intent.setClass(RssItemsActivity.this, RssDetailActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("cate",itemTitle);
				bundle.putString("title", title);
				bundle.putString("content", content);
				bundle.putString("link", link);

				intent.putExtras(bundle);

				startActivity(intent);
			}
		});
		// �����¼�
		listView.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenuInfo menuInfo) {
				MenuInflater inflater = getMenuInflater();
				inflater.inflate(R.menu.blog_list_contextmenu, menu);
				menu.setHeaderTitle(R.string.menu_bar_title);
			}
		});
	}
	/**
	 * �첽����
	 * 
	 * @author walkingp
	 * 
	 */
	public class PageTask extends AsyncTask<String, Integer, List<RssItem>> {
		@Override
		protected List<RssItem> doInBackground(String... params) {
			List<RssItem> listRss = RssItemHelper.GetRssList(itemUrl);
			return listRss;
		}
		@Override
		protected void onPostExecute(List<RssItem> result) {
			bodyProgressBar.setVisibility(View.GONE);
			topProgressBar.setVisibility(View.GONE);
			btnRefresh.setVisibility(View.VISIBLE);
			if (result == null || result.size() == 0) {
				txtNoData.setVisibility(View.VISIBLE);
			} else {
				adapter = new RssItemsAdapter(getApplicationContext(), result);
				listView.setAdapter(adapter);
			}
		}
		@Override
		protected void onPreExecute() {
			bodyProgressBar.setVisibility(View.VISIBLE);
			topProgressBar.setVisibility(View.VISIBLE);
			btnRefresh.setVisibility(View.GONE);
		}
	}
}
