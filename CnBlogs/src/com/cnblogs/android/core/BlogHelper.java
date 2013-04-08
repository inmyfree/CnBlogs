package com.cnblogs.android.core;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;
import com.cnblogs.android.dal.BlogDalHelper;
import com.cnblogs.android.entity.*;
import com.cnblogs.android.parser.BlogListXmlParser;
import com.cnblogs.android.parser.BlogXmlParser;
import com.cnblogs.android.utility.NetHelper;

/**
 * Blog������
 * 
 * @author walkingp
 * 
 */
public class BlogHelper extends DefaultHandler {
	/**
	 * ��������
	 * 
	 * @param top
	 * @return
	 */
	@SuppressWarnings("null")
	public static List<Blog> DownloadOfflineBlogList(int top) {
		int pageSize = top / Config.BLOG_PAGE_SIZE;
		int lastNum = pageSize % Config.BLOG_PAGE_SIZE;

		List<Blog> listBlogs = null;
		// ����ǰ��ҳ
		for (int i = 0; i < pageSize; i++) {
			List<Blog> list = GetBlogList(i);

			listBlogs.addAll(list);
		}
		// ����ʣ������
		List<Blog> list = GetBlogList(pageSize);// �������һҳ
		for (int i = 0, len = list.size(); i < len; i++) {
			listBlogs.addAll(list);
			if (list.get(i).GetBlogId() == lastNum) {
				break;
			}
		}
		// ����
		for (int i = 0, len = listBlogs.size(); i < len; i++) {
			String content = GetBlogContentByIdWithNet(listBlogs.get(i)
					.GetBlogId());
			listBlogs.get(i).SetBlogContent(content);

			listBlogs.get(i).SetIsFullText(true);// ����ȫ�ı�־
		}

		return listBlogs;
	}
	/**
	 * ����ҳ�뷵��Blog���󼯺�
	 * 
	 * @return pageIndex:ҳ�룬��1��ʼ
	 */
	public static ArrayList<Blog> GetBlogList(int pageIndex) {
		int pageSize = Config.BLOG_PAGE_SIZE;
		String url = Config.URL_GET_BLOG_LIST.replace("{pageIndex}",
				String.valueOf(pageIndex)).replace("{pageSize}",
				String.valueOf(pageSize));// ���ݵ�ַ
		String dataString = NetHelper.GetContentFromUrl(url);

		ArrayList<Blog> list = ParseString(dataString);

		return list;
	}
	/**
	 * ����48Сʱ���Ķ�����Blog���󼯺�
	 */
	public static ArrayList<Blog> Get48HoursTopViewBlogList() {
		int size = Config.NUM_48HOURS_TOP_VIEW;
		String url = Config.URL_48HOURS_TOP_VIEW_LIST.replace("{size}",
				String.valueOf(size));// ���ݵ�ַ
		String dataString = NetHelper.GetContentFromUrl(url);

		ArrayList<Blog> list = ParseString(dataString);

		return list;
	}
	/**
	 * ����10�����Ƽ�����Blog���󼯺�
	 */
	public static ArrayList<Blog> Get10DaysTopDiggBlogList() {
		int size = Config.NUM_TENDAYS_TOP_DIGG;
		String url = Config.URL_TENDAYS_TOP_DIGG_LIST.replace("{size}",
				String.valueOf(size));// ���ݵ�ַ
		String dataString = NetHelper.GetContentFromUrl(url);

		ArrayList<Blog> list = ParseString(dataString);

		return list;
	}
	/**
	 * ���ݲ����û���ź�ҳ�뷵��Blog���󼯺�
	 * 
	 * @param userId
	 *            :�û����
	 * @param pageIndex
	 *            :ҳ�룬��1��ʼ
	 * @return
	 */
	public static ArrayList<Blog> GetAuthorBlogList(String author, int pageIndex) {
		int pageSize = Config.BLOG_LIST_BY_AUTHOR_PAGE_SIZE;
		String url = Config.URL_GET_BLOG_LIST_BY_AUTHOR
				.replace("{author}", author)
				.replace("{pageIndex}", String.valueOf(pageIndex))
				.replace("{pageSize}", String.valueOf(pageSize));// ���ݵ�ַ
		String dataString = NetHelper.GetContentFromUrl(url);

		ArrayList<Blog> list = ParseString(dataString);

		return list;
	}
	/**
	 * ���ַ���ת��ΪBlog����
	 * 
	 * @return
	 */
	private static ArrayList<Blog> ParseString(String dataString) {
		SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
		ArrayList<Blog> listBlog = new ArrayList<Blog>();
		try {
			XMLReader xmlReader = saxParserFactory.newSAXParser()
					.getXMLReader();
			BlogListXmlParser handler = new BlogListXmlParser(listBlog);
			xmlReader.setContentHandler(handler);

			xmlReader.parse(new InputSource(new StringReader(dataString)));
			listBlog = handler.GetBlogList();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return listBlog;
	}
	/**
	 * ���ݱ�Ż�ȡ��������
	 * 
	 * @param blogId
	 * @return
	 */
	public static String GetBlogContentByIdWithNet(int blogId) {
		String blogContent = "";
		String url = Config.URL_GET_BLOG_DETAIL.replace("{0}",
				String.valueOf(blogId));// ��ַ
		String xml = NetHelper.GetContentFromUrl(url);
		if (xml == "") {
			return "";
		}
		blogContent = ParseBlogString(xml);

		return blogContent;
	}
	/**
	 * ���ݱ�Ż�ȡ��������(��ȡ���أ���ȡ����)
	 * 
	 * @param blogId
	 * @return
	 */
	public static String GetBlogById(int blogId, Context context) {
		String blogContent = "";
		// ���ȿ��Ǳ�������
		BlogDalHelper helper = new BlogDalHelper(context);
		Blog entity = helper.GetBlogEntity(blogId);
		if (null == entity || entity.GetBlogContent().equals("")) {
			blogContent = GetBlogContentByIdWithNet(blogId);
			/*String _blogContent=ImageCacher.FormatLocalHtmlWithImg(ImageCacher.EnumImageType.Blog, blogContent);
			if (Config.IS_SYNCH2DB_AFTER_READ) {
				helper.SynchronyContent2DB(blogId, _blogContent);// ͬ�������ݿ�
			}*/
		} else {
			blogContent = entity.GetBlogContent();
		}
		helper.Close();
		return blogContent;
	}
	/**
	 * ���ַ���ת��ΪBlog����
	 * 
	 * @return
	 */
	private static String ParseBlogString(String dataString) {
		SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
		String blogContent = "";
		try {
			XMLReader xmlReader = saxParserFactory.newSAXParser()
					.getXMLReader();
			BlogXmlParser handler = new BlogXmlParser(blogContent);
			xmlReader.setContentHandler(handler);

			xmlReader.parse(new InputSource(new StringReader(dataString)));
			blogContent = handler.GetBlogContent();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return blogContent;
	}
}
