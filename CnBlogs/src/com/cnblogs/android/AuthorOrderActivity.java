package com.cnblogs.android;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import com.cnblogs.android.adapter.UserListAdapter;
import com.cnblogs.android.controls.PullToRefreshListView;
import com.cnblogs.android.controls.PullToRefreshListView.OnRefreshListener;
import com.cnblogs.android.core.Config;
import com.cnblogs.android.core.UserHelper;
import com.cnblogs.android.entity.Users;
import com.cnblogs.android.utility.NetHelper;
/**
 * ��������
 * @author walkingp
 * @date:2012-3
 *
 */
public class AuthorOrderActivity extends BaseActivity{
	List<Users> listUsers = new ArrayList<Users>();

	int pageIndex = 1;// ҳ��

	TextView txtAppTitle;
	
	ListView listView;
	private UserListAdapter adapter;// ����Դ

	ProgressBar blogBody_progressBar;// ����ListView���ؿ�
	ImageButton blog_refresh_btn;// ˢ�°�ť
	ProgressBar blog_progress_bar;// ���ذ�ť
	
	Button btnBack;//���ذ�ť

	private LinearLayout viewFooter;// footer view
	TextView tvFooterMore;// �ײ�������ʾ
	ProgressBar list_footer_progress;// �ײ�������

	Resources res;// ��Դ

	private int lastItem;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.author_order_recommend_news_layout);

		res = this.getResources();
		InitialControls();
		BindControls();
		new PageTask(0, true).execute();
	}
	/**
	 * ��ʼ���б�
	 */
	private void InitialControls() {
		txtAppTitle=(TextView)findViewById(R.id.txtAppTitle);
		txtAppTitle.setText("�Ƽ���������");
		btnBack=(Button)findViewById(R.id.btn_back);
		btnBack.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				finish();
			}			
		});
		
		listView = (ListView) findViewById(R.id.blog_list);
		blogBody_progressBar = (ProgressBar) findViewById(R.id.blogList_progressBar);
		blogBody_progressBar.setVisibility(View.VISIBLE);

		blog_refresh_btn = (ImageButton) findViewById(R.id.blog_refresh_btn);
		blog_progress_bar = (ProgressBar) findViewById(R.id.blog_progressBar);
		// �ײ�view
		LayoutInflater mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		viewFooter = (LinearLayout) mInflater.inflate(R.layout.listview_footer,
				null, false);
	}
	/**
	 * ���¼�
	 */
	private void BindControls() {
		// ˢ��
		blog_refresh_btn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				new PageTask(1, true).execute();
			}
		});
		// ����ˢ��
		((PullToRefreshListView) listView)
				.setOnRefreshListener(new OnRefreshListener() {
					@Override
					public void onRefresh() {
						new PageTask(-1, true).execute();
					}
				});
		// ����ˢ��
		listView.setOnScrollListener(new OnScrollListener() {
			/**
			 * ���������һ��
			 */
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (lastItem == adapter.getCount()
						&& scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					pageIndex = pageIndex + 1;
					new PageTask(pageIndex, false).execute();
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				lastItem = firstVisibleItem - 2 + visibleItemCount;
			}
		});// �����ת
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {

				Intent intent = new Intent();
				try {
					// ���ݲ���
					intent.setClass(AuthorOrderActivity.this,
							AuthorBlogActivity.class);
					Bundle bundle = new Bundle();
					TextView tvBlogTitle = (TextView) (v
							.findViewById(R.id.author_list_title));
					TextView tvBlogAuthor = (TextView) (v
							.findViewById(R.id.author_list_username));

					String blogTitle = tvBlogTitle.getText().toString();
					String blogAuthor = tvBlogAuthor.getText().toString();

					bundle.putString("blogName", blogTitle);
					bundle.putString("author", blogAuthor);

					intent.putExtras(bundle);

					startActivity(intent);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
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
	 * ���߳������������������ء���ʼ�������ؼ��ء�ˢ�£�
	 * 
	 */
	public class PageTask extends AsyncTask<String, Integer, List<Users>> {
		boolean isRefresh = false;
		int curPageIndex = 0;
		public PageTask(int page, boolean isRefresh) {
			curPageIndex = page;
			this.isRefresh = isRefresh;
		}

		protected List<Users> doInBackground(String... params) {
			boolean isNetworkAvailable = NetHelper.networkIsAvailable(getApplicationContext());

			int _pageIndex = curPageIndex;
			if (_pageIndex <= 0) {
				_pageIndex = 1;
			}

			if (isNetworkAvailable) {// ���������
				List<Users> listUserNew = UserHelper.GetTopUserList(_pageIndex);
				switch (curPageIndex) {
					case -1 :// ����\
						List<Users> listTmp = new ArrayList<Users>();
						if (listUsers != null && listUsers.size() > 0) {// ������ҳ������ʱ
							if (listUserNew != null && listUserNew.size() > 0) {
								int size = listUserNew.size();
								for (int i = 0; i < size; i++) {
									if (!listUsers.contains(listUserNew.get(i))) {// ��������ظ�
										listTmp.add(listUserNew.get(i));
									}
								}
							}
						}
						return listTmp;
					case 0 :// �״μ���
					case 1 :// ˢ��
						if (listUserNew != null && listUserNew.size() > 0) {
							return listUserNew;
						}
						break;
					default :// ����
						List<Users> listT = new ArrayList<Users>();
						if (listUsers != null && listUsers.size() > 0) {// ������ҳ������ʱ
							if (listUserNew != null && listUserNew.size() > 0) {
								int size = listUserNew.size();
								for (int i = 0; i < size; i++) {
									if (!listUsers.contains(listUserNew.get(i))) {// ��������ظ�
										listT.add(listUserNew.get(i));
									}
								}
							}
						}
						return listT;
				}
			}

			return null;
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}
		/**
		 * ��������
		 */
		@Override
		protected void onPostExecute(List<Users> result) {
			// ���Ͻ�
			blog_progress_bar.setVisibility(View.GONE);
			blog_refresh_btn.setVisibility(View.VISIBLE);

			// ���粻���ò��ұ���û�б�������
			if (result == null || result.size() == 0) {// û��������
				((PullToRefreshListView) listView).onRefreshComplete();
				if (!NetHelper.networkIsAvailable(getApplicationContext())
						&& curPageIndex > 1) {// ��������û������
					Toast.makeText(getApplicationContext(),
							R.string.sys_network_error, Toast.LENGTH_SHORT)
							.show();
					// listView.removeFooterView(viewFooter);
				}
				return;
			}
			int size = result.size();
			if (size >= Config.BLOG_PAGE_SIZE
					&& listView.getFooterViewsCount() == 0) {
				listView.addFooterView(viewFooter);
			}

			if (curPageIndex == -1) {// ����ˢ��
				adapter.InsertData(result);
			} else if (curPageIndex == 0) {// �״μ���
				listUsers = result;// dbHelper.GetTopBlogList();

				blogBody_progressBar.setVisibility(View.GONE);
				adapter = new UserListAdapter(getApplicationContext(),
						listUsers, listView);
				listView.setAdapter(adapter);

				// ���ݲ���
				((PullToRefreshListView) listView).SetDataRow(listUsers.size());
				((PullToRefreshListView) listView).SetPageSize(Config.BLOG_PAGE_SIZE);
			} else if (curPageIndex == 1) {// ˢ��
				try {// ������ҳ��������أ���ˢ�°�ť
					if (adapter != null && adapter.GetData() != null) {
						adapter.GetData().clear();
						adapter.AddMoreData(result);
					} else if (result != null) {
						adapter = new UserListAdapter(getApplicationContext(),result, listView);
						listView.setAdapter(adapter);
					}
					blogBody_progressBar.setVisibility(View.GONE);
				} catch (Exception ex) {
					// Log.e("BlogActivity", ex.getMessage());
				}
			} else {// ����
				adapter.AddMoreData(result);
			}

			if (isRefresh) {// ˢ��ʱ����
				((PullToRefreshListView) listView).onRefreshComplete();
			}
		}
		@Override
		protected void onPreExecute() {
			// ���������
			if (listView.getCount() == 0) {
				blogBody_progressBar.setVisibility(View.VISIBLE);
			}
			// ���Ͻ�
			blog_progress_bar.setVisibility(View.VISIBLE);
			blog_refresh_btn.setVisibility(View.GONE);

			if (!isRefresh) {// �ײ��ؼ���ˢ��ʱ��������
				TextView tvFooterMore = (TextView) findViewById(R.id.tvFooterMore);
				tvFooterMore.setText(R.string.pull_to_refresh_refreshing_label);
				tvFooterMore.setVisibility(View.VISIBLE);
				ProgressBar list_footer_progress = (ProgressBar) findViewById(R.id.list_footer_progress);
				list_footer_progress.setVisibility(View.VISIBLE);
			}
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
		}
	}
}
