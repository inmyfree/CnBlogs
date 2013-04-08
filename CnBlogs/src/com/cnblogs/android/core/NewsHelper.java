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

import com.cnblogs.android.dal.NewsDalHelper;
import com.cnblogs.android.entity.*;
import com.cnblogs.android.parser.NewsListXmlParser;
import com.cnblogs.android.parser.NewsXmlParser;
import com.cnblogs.android.utility.NetHelper;

/**
 * News������
 * 
 * @author walkingp
 * 
 */
public class NewsHelper extends DefaultHandler {
	/**
	 * ����ҳ�뷵��News���󼯺�
	 * 
	 * @return pageIndex:ҳ�룬��1��ʼ
	 */
	public static ArrayList<News> GetNewsList(int pageIndex) {
		int pageSize = Config.NEWS_PAGE_SIZE;
		String url = Config.URL_GET_NEWS_LIST.replace("{pageIndex}",
				String.valueOf(pageIndex)).replace("{pageSize}",
				String.valueOf(pageSize));// ���ݵ�ַ
		String dataString = NetHelper.GetContentFromUrl(url);

		ArrayList<News> list = ParseString(dataString);

		return list;
	}
	/**
	 * �Ƽ�����
	 * @param pageIndex��ҳ�룬��1��ʼ
	 * @return
	 */
	public static List<News> GetRecommendNewsList(int pageIndex){
		int pageSize=Config.NEWS_PAGE_SIZE;
		String url = Config.URL_RECOMMEND_NEWS_LIST.replace("{pageIndex}",
				String.valueOf(pageIndex)).replace("{pageSize}",
				String.valueOf(pageSize));// ���ݵ�ַ
		String dataString = NetHelper.GetContentFromUrl(url);

		List<News> list = ParseString(dataString);

		return list;
	}
	/**
	 * ���ַ���ת��ΪNews����
	 * 
	 * @return
	 */
	private static ArrayList<News> ParseString(String dataString) {
		SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
		ArrayList<News> listNews = new ArrayList<News>();
		try {
			XMLReader xmlReader = saxParserFactory.newSAXParser()
					.getXMLReader();
			NewsListXmlParser handler = new NewsListXmlParser(listNews);
			xmlReader.setContentHandler(handler);

			xmlReader.parse(new InputSource(new StringReader(dataString)));
			listNews = handler.GetNewsList();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return listNews;
	}
	/**
	 * ���ݱ�Ż�ȡ��������
	 * 
	 * @param blogId
	 * @return
	 */
	public static String GetNewsContentByIdWithNet(int newsId) {
		String newsContent = "";
		String url = Config.URL_GET_NEWS_DETAIL.replace("{0}",
				String.valueOf(newsId));// ��ַ
		String xml = NetHelper.GetContentFromUrl(url);
		if (xml == "") {
			return "";
		}
		newsContent = ParseNewsString(xml);

		return newsContent;
	}

	/**
	 * ���ݱ�Ż�ȡ��������(��ȡ���أ���ȡ����)
	 * 
	 * @param blogId
	 * @return
	 */
	public static String GetNewsContentById(int newsId, Context context) {
		String newsContent = "";
		// ���ȿ��Ǳ�������
		NewsDalHelper helper = new NewsDalHelper(context);
		News entity = helper.GetNewsEntity(newsId);
		if (null == entity || entity.GetNewsContent().equals("")) {
			newsContent = GetNewsContentByIdWithNet(newsId);
			//String _newsContent=ImageCacher.FormatLocalHtmlWithImg(ImageCacher.EnumImageType.News, newsContent);
			//if (Config.IS_SYNCH2DB_AFTER_READ) {
			//	helper.SynchronyContent2DB(newsId, _newsContent);// ͬ�������ݿ�
			//}
		} else {
			newsContent = entity.GetNewsContent();
		}
		helper.Close();
		return newsContent;
	}
	/**
	 * ���ַ���ת��Ϊ��������
	 * 
	 * @return
	 */
	private static String ParseNewsString(String dataString) {
		SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
		String newsContent = "";
		try {
			XMLReader xmlReader = saxParserFactory.newSAXParser()
					.getXMLReader();
			NewsXmlParser handler = new NewsXmlParser(newsContent);
			xmlReader.setContentHandler(handler);

			xmlReader.parse(new InputSource(new StringReader(dataString)));
			newsContent = handler.GetNewsContent();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return newsContent;
	}
}
