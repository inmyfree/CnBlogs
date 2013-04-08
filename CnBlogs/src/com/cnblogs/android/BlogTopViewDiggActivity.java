package com.cnblogs.android;

import java.util.ArrayList;
import java.util.List;

import com.cnblogs.android.adapter.BlogListAdapter;
import com.cnblogs.android.core.BlogHelper;
import com.cnblogs.android.entity.Blog;
import com.cnblogs.android.enums.EnumActivityType;
import com.cnblogs.android.utility.NetHelper;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
/**
 * 48Сʱ���Ķ�����+10�����Ƽ�����
 * @author walkingp
 * @date 2012-3
 */
public class BlogTopViewDiggActivity extends BaseActivity {
	List<Blog> listBlog = new ArrayList<Blog>();

	BlogListAdapter adapter;
	ListView listView;

	ProgressBar progressBar;// ����
	ProgressBar topProgressBar;//ͷ��������
	Resources res;// ��Դ
	SharedPreferences sharePreferencesSettings;// ����
	
	EnumActivityType.EnumOrderActivityType activityType;
	
	Button btnBack;//��ť
	ImageButton btnRefresh;//ˢ�°�ť
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.blog_top_view_digg_layout);
		res = this.getResources();

		BindControls();
		BindEvent();
		new PageTask().execute();
	}
	/**
	 * �ҵ��ؼ�
	 */
	private void BindControls() {
		activityType=EnumActivityType.EnumOrderActivityType.values()[getIntent().getIntExtra("type", 0)];
		
		TextView txtAppTitle=(TextView)findViewById(R.id.txtAppTitle);
		switch(activityType){
			case TopDiggBlogIn10Days:
				txtAppTitle.setText("10�����Ƽ�����");
				break;
			case TopViewBlogIn48Hours:
				txtAppTitle.setText("48Сʱ���Ķ�����");
				break;
		}
		
		listView = (ListView) findViewById(R.id.blog_list);
		// �����ת
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				RedirectDetailActivity(v);
			}
		});
		progressBar = (ProgressBar) findViewById(R.id.blogList_progressBar);
		topProgressBar=(ProgressBar)findViewById(R.id.blog_progressBar);	
		btnRefresh=(ImageButton)findViewById(R.id.blog_refresh_btn);
		btnBack=(Button)findViewById(R.id.btn_back);
		//����
		btnBack.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				finish();
			}			
		});
	}
	/**
	 * ���¼�
	 */
	private void BindEvent() {
		// �����ת
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {

				Intent intent = new Intent();
				try {
					// ���ݲ���
					intent.setClass(BlogTopViewDiggActivity.this,
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

					startActivityForResult(intent, 0);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
	}
	/**
	 * ���߳������������������ء���ʼ�������ؼ��ء�ˢ�£�
	 * 
	 */
	public class PageTask extends AsyncTask<String, Integer, List<Blog>> {
		public PageTask() {
		}

		protected List<Blog> doInBackground(String... params) {
			if (!NetHelper.networkIsAvailable(getApplicationContext())) {
				return null;
			}
			try {
				List<Blog> listTmp=new ArrayList<Blog>();
				List<Blog> listBlogNew =new ArrayList<Blog>();
				switch(activityType){
					case TopViewBlogIn48Hours:
						listBlogNew=BlogHelper.Get48HoursTopViewBlogList();
						break;
					case TopDiggBlogIn10Days:
						listBlogNew=BlogHelper.Get10DaysTopDiggBlogList();
						break;
				}
				int size = listBlogNew.size();
				for (int i = 0; i < size; i++) {
					if (!listBlog.contains(listBlogNew.get(i))) {// ��������ظ�
						listTmp.add(listBlogNew.get(i));
					}
				}
				return listTmp;
			} catch (Exception e) {
				e.printStackTrace();
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
		protected void onPostExecute(List<Blog> result) {
			btnRefresh.setVisibility(View.VISIBLE);
			topProgressBar.setVisibility(View.GONE);
			// ���粻����
			if (!NetHelper.networkIsAvailable(getApplicationContext())) {
				Toast.makeText(getApplicationContext(),
						R.string.sys_network_error, Toast.LENGTH_SHORT).show();
				return;
			}

			if (result == null || result.size() == 0) {// û��������
				return;
			}

			listBlog = result;

			progressBar.setVisibility(View.GONE);
			adapter = new BlogListAdapter(getApplicationContext(), listBlog,
					listView);
			listView.setAdapter(adapter);
		}
		@Override
		protected void onPreExecute() {
			// ���������
			if (listView.getCount() == 0) {
				progressBar.setVisibility(View.VISIBLE);
			}
			btnRefresh.setVisibility(View.GONE);
			topProgressBar.setVisibility(View.VISIBLE);
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
		}
	}
	/**
	 * ��ת������
	 * 
	 * @param v
	 */
	private void RedirectDetailActivity(View v) {

		Intent intent = new Intent();
		try {
			// ���ݲ���
			intent.setClass(BlogTopViewDiggActivity.this, BlogDetailActivity.class);
			Bundle bundle = new Bundle();
			TextView tvBlogId = (TextView) (v
					.findViewById(R.id.recommend_text_id));
			TextView tvBlogTitle = (TextView) (v
					.findViewById(R.id.recommend_text_title));
			TextView tvBlogAuthor = (TextView) (v
					.findViewById(R.id.recommend_text_author));
			TextView tvBlogDate = (TextView) (v
					.findViewById(R.id.recommend_text_date));
			TextView tvBlogUrl = (TextView) (v
					.findViewById(R.id.recommend_text_url));
			TextView tvBlogViewCount = (TextView) (v
					.findViewById(R.id.recommend_text_view));
			TextView tvBlogCommentCount = (TextView) (v
					.findViewById(R.id.recommend_text_comments));
			TextView tvBlogDomain = (TextView) (v
					.findViewById(R.id.recommend_text_domain));

			int blogId = Integer.parseInt(tvBlogId.getText().toString());
			String blogTitle = tvBlogTitle.getText().toString();
			String blogAuthor = tvBlogAuthor.getText().toString();
			String blogDate = tvBlogDate.getText().toString();
			String blogUrl = tvBlogUrl.getText().toString();
			String blogDomain = tvBlogDomain.getText().toString();
			int viewsCount = Integer.parseInt(tvBlogViewCount.getText()
					.toString());
			int commentCount = Integer.parseInt(tvBlogCommentCount.getText()
					.toString());

			bundle.putInt("blogId", blogId);
			bundle.putString("blogTitle", blogTitle);
			bundle.putString("author", blogAuthor);
			bundle.putString("date", blogDate);
			bundle.putString("blogUrl", blogUrl);
			bundle.putInt("view", viewsCount);
			bundle.putInt("comment", commentCount);
			bundle.putString("blogDomain", blogDomain);

			Log.d("blogId", String.valueOf(blogId));
			intent.putExtras(bundle);

			startActivity(intent);
			tvBlogTitle.setTextColor(R.color.gray);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
