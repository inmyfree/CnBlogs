package com.cnblogs.android.parser;

import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.lang.StringEscapeUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import com.cnblogs.android.entity.News;
import com.cnblogs.android.utility.AppUtil;
/**
 * News����xml������
 * 
 * @author walkingp
 * 
 */
public class NewsListXmlParser extends DefaultHandler {
	final String ENTRY_TAG = "entry";// �����
	final String ENTRY_ID_TAG = "id";// ��ű��
	final String ENTRY_TITLE_TAG = "title";// ������
	final String ENTRY_SUMMARY_TAG = "summary";// �����
	final String ENTRY_PUBLISHED_TAG = "published";// ����ʱ����
	final String ENTRY_LINK_TAG = "link";// ʵ�����ӵ�ַ
	final String ENTRY_DIGG_TAG = "diggs";// �Ƽ�����
	final String ENTRY_VIEW_TAG = "views";// �鿴����
	final String ENTRY_COMMENTS_TAG = "comments";// ���۴���
	final String ENTRY_URL_TAG = "link";// ʵ����ַ��ǩ
	final String ENTRY_URL_ATTRIBUTE_TAG = "href";// ��ַ���Ա�ǩ

	private ArrayList<News> listNews;// ���󼯺�
	private News entity;// ��������
	private boolean isStartParse;// ��ʼ����
	private StringBuilder currentDataBuilder;// ��ǰȡ����ֵ
	/**
	 * Ĭ�Ϲ��캯��
	 */
	public NewsListXmlParser() {
		super();
	}
	/**
	 * ���캯��
	 * 
	 * @return
	 */
	public NewsListXmlParser(ArrayList<News> list) {
		this.listNews = list;
	}
	/**
	 * ���������
	 * 
	 * @return
	 */
	public ArrayList<News> GetNewsList() {
		return listNews;
	}
	/**
	 * �ĵ���ʼʱ����
	 */
	public void startDocument() throws SAXException {
		Log.i("News", "�ĵ�������ʼ");
		super.startDocument();
		listNews = new ArrayList<News>();
		currentDataBuilder = new StringBuilder();
	}
	/**
	 * ��ȡ������XML����
	 */
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		if (localName.equalsIgnoreCase(ENTRY_TAG)) {
			entity = new News();
			isStartParse = true;
		}
		if (isStartParse && localName.equalsIgnoreCase(ENTRY_URL_TAG)) {
			entity.SetNewsUrl(attributes.getValue(ENTRY_URL_ATTRIBUTE_TAG));
		}// ʵ����ַ
	}
	/**
	 * ��ȡԪ������
	 * 
	 * @param ch
	 * @param start
	 * @param length
	 * @throws SAXException
	 */
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		super.characters(ch, start, length);
		currentDataBuilder.append(ch, start, length);
	}
	/**
	 * Ԫ�ؽ���ʱ����
	 */
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		super.endElement(uri, localName, qName);
		if (isStartParse) {// ����Ŀ��

			String chars = currentDataBuilder.toString();
			Log.i("News", "���ڽ���" + localName);
			// ����
			if (localName.equalsIgnoreCase(ENTRY_TITLE_TAG)) {// ����
				try {
					chars = StringEscapeUtils.unescapeHtml(chars);// ���б��봦���������&gt;����html
				} catch (Exception ex) {
					Log.e("newsXml", "__________��������_____________");
				}
				entity.SetNewsTitle(chars);
			} else if (localName.equalsIgnoreCase(ENTRY_SUMMARY_TAG)) {// ժҪ
				try {
					chars = StringEscapeUtils.unescapeHtml(chars);// ���б��봦���������&gt;����html
				} catch (Exception ex) {
					Log.e("newsXml", "__________��������_____________");
				}
				entity.SetSummary(chars);
			} else if (localName.equalsIgnoreCase(ENTRY_ID_TAG)) {// ���
				int id = Integer.parseInt(chars);
				entity.SetNewsId(id);
			} else if (localName.equalsIgnoreCase(ENTRY_PUBLISHED_TAG)) {// ����ʱ��
				Date addTime = AppUtil.ParseUTCDate(chars);
				entity.SetAddTime(addTime);
			} else if (localName.equalsIgnoreCase(ENTRY_DIGG_TAG)) {// �Ƽ�����
				entity.SetDiggsNum(Integer.parseInt(chars));
			} else if (localName.equalsIgnoreCase(ENTRY_VIEW_TAG)) {// �鿴����
				entity.SetViewNum(Integer.parseInt(chars));
			} else if (localName.equalsIgnoreCase(ENTRY_COMMENTS_TAG)) {// ���۴���
				entity.SetCommentNum(Integer.parseInt(chars));
			} else if (localName.equalsIgnoreCase(ENTRY_TAG)) {// ��ֹ
				listNews.add(entity);
				isStartParse = false;
			}
		}

		currentDataBuilder.setLength(0);
	}
	/**
	 * �ĵ�����ʱ����
	 */
	public void endDocument() throws SAXException {
		Log.i("News", "�ĵ���������");
		super.endDocument();
	}
}
