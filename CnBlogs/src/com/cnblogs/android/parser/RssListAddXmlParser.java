package com.cnblogs.android.parser;
import java.util.Date;

import org.apache.commons.lang.StringEscapeUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import com.cnblogs.android.entity.RssList;
import com.cnblogs.android.utility.AppUtil;
/**
 * Blog����xml������
 * 
 * @author walkingp
 * 
 */
public class RssListAddXmlParser extends DefaultHandler {
	final String ENTRY_TAG = "channel";// �����
	final String ENTRY_TITLE_TAG = "title";// ������
	final String ENTRY_PUBLISHED_TAG="pubDate";//����ʱ��
	final String ENTRY_SUMMARY_TAG = "description";// �����
	final String ENTRY_UPDATED_TAG = "lastBuildDate";// ����ʱ����
	final String ENTRY_AUTHOR_NAME_TAG = "webMaster";// ����������
	final String ENTRY_LINK_TAG = "link";// ʵ�����ӵ�ַ
	final String ENTRY_AVATOR_TAG = "image";// Logo��ַ

	private RssList entity;// ��������
	private boolean isStartParse;// ��ʼ����
	private StringBuilder currentDataBuilder;// ��ǰȡ����ֵ
	/**
	 * Ĭ�Ϲ��캯��
	 */
	public RssListAddXmlParser() {
		super();
	}
	/**
	 * ���캯��
	 * 
	 * @return
	 */
	public RssListAddXmlParser(RssList entity) {
		this.entity = entity;
	}
	/**
	 * ���������
	 * 
	 * @return
	 */
	public RssList GetRssList() {
		return entity;
	}
	/**
	 * �ĵ���ʼʱ����
	 */
	public void startDocument() throws SAXException {
		Log.i("Blog", "�ĵ�������ʼ");
		super.startDocument();
		entity = new RssList();
		currentDataBuilder = new StringBuilder();
	}
	/**
	 * ��ȡ������XML����
	 */
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		if (localName.equalsIgnoreCase(ENTRY_TAG)) {
			entity = new RssList();
			isStartParse = true;
		}
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
			Log.i("Blog", "���ڽ���" + localName);
			// ����
			if (localName.equalsIgnoreCase(ENTRY_TITLE_TAG)) {// ����
				try {
					chars = StringEscapeUtils.unescapeHtml(chars);// ���б��봦���������&gt;����html
				} catch (Exception ex) {
					Log.e("rssXml", "__________��������_____________");
				}
				entity.SetTitle(chars);
			} else if (localName.equalsIgnoreCase(ENTRY_SUMMARY_TAG)) {// ժҪ
				try {
					chars = StringEscapeUtils.unescapeHtml(chars);// ���б��봦���������&gt;����html
				} catch (Exception ex) {
					Log.e("rssXml", "__________��������_____________");
				}
				entity.SetDescription(chars);
			} else if (localName.equalsIgnoreCase(ENTRY_PUBLISHED_TAG)) {// ����ʱ��
				Date addTime = AppUtil.ParseUTCDate(chars);
				entity.SetAddTime(addTime);
			} else if (localName.equalsIgnoreCase(ENTRY_UPDATED_TAG)) {// �޸�ʱ��
				Date updateTime = AppUtil.ParseUTCDate(chars);
				entity.SetUpdated(updateTime);
			} else if (localName.equalsIgnoreCase(ENTRY_AUTHOR_NAME_TAG)) {// ��������
				entity.SetAuthor(chars);
			} else if (localName.equalsIgnoreCase(ENTRY_AVATOR_TAG)) {// Logo��ַ
				entity.SetImage(chars);
			} else if (localName.equalsIgnoreCase(ENTRY_LINK_TAG)) {// ʵ��
				entity.SetLink(chars);
			} else if (localName.equalsIgnoreCase(ENTRY_TAG)) {// ��ֹ
				entity.SetIsActive(true);
				isStartParse = false;
			}
		}

		currentDataBuilder.setLength(0);
	}
	/**
	 * �ĵ�����ʱ����
	 */
	public void endDocument() throws SAXException {
		Log.i("Rss", "�ĵ���������");
		super.endDocument();
	}
}
