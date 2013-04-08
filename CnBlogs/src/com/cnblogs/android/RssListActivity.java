package com.cnblogs.android;

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

import com.cnblogs.android.adapter.RssListAdapter;
import com.cnblogs.android.core.RssListHelper;
import com.cnblogs.android.entity.RssList;
/**
 * RSS�����µ�����
 * 
 * @author walkingp
 * 
 */
public class RssListActivity extends BaseActivity {
	ListView listView;
	private RssListAdapter adapter;// ����Դ
	List<RssList> listRss;

	int cateId;
	String cateTitle;

	TextView txtNoData;// û������

	Button btnBack;// ����
	ProgressBar bodyProgressBar;// ����ListView���ؿ�
	ImageButton btnRefresh;// ˢ�°�ť
	ProgressBar topProgressBar;// ���ذ�ť

	TextView txtAppTitle;// ����

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
		cateId = getIntent().getIntExtra("cateId", 0);
		cateTitle = getIntent().getStringExtra("title");
	}
	/**
	 * ���¼�
	 */
	private void BindControls() {
		txtAppTitle.setText(cateTitle);
		// ����
		btnBack.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				RssListActivity.this.finish();
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
						.findViewById(R.id.rss_item_title);
				TextView tvUrl = (TextView) v.findViewById(R.id.rss_item_url);
				TextView tvIsCnblogs = (TextView) v
						.findViewById(R.id.rss_item_is_cnblogs);
				TextView tvAuthor = (TextView) v
						.findViewById(R.id.rss_item_author);
				String title = tvTitle.getText().toString();
				String url = tvUrl.getText().toString();
				boolean isCnblogs = tvIsCnblogs.getText().toString()
						.equals("1");
				String author = tvAuthor.getText().toString();

				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				if (isCnblogs) {// ����԰
					intent.setClass(RssListActivity.this,
							AuthorBlogActivity.class);
					bundle.putString("blogName", title);
					bundle.putString("author", author);
				} else {
					intent.setClass(RssListActivity.this,
							RssItemsActivity.class);
					bundle.putString("title", title);
					bundle.putString("url", url);
				}

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
	public class PageTask extends AsyncTask<String, Integer, List<RssList>> {

		@Override
		protected List<RssList> doInBackground(String... params) {
			List<RssList> list = RssListHelper.GetRssList(cateId);
			return list;
		}
		@Override
		protected void onPostExecute(List<RssList> result) {
			bodyProgressBar.setVisibility(View.GONE);
			topProgressBar.setVisibility(View.GONE);
			btnRefresh.setVisibility(View.VISIBLE);
			if (result == null || result.size() == 0) {
				txtNoData.setVisibility(View.VISIBLE);
			} else {
				adapter = new RssListAdapter(getApplicationContext(), result,
						listView, RssListAdapter.EnumSource.RssList, adapter);
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
