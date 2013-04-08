package com.cnblogs.android.parser;

import java.util.ArrayList;
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
public class RssListXmlParser extends DefaultHandler {
	final String ENTRY_TAG = "Item";// �����
	final String ENTRY_ID_TAG = "RssId";// ��ű��
	final String ENTRY_TITLE_TAG = "Title";// ������
	final String ENTRY_SUMMARY_TAG = "Description";// �����
	final String ENTRY_PUBLISHED_TAG = "AddTime";// ����ʱ����
	final String ENTRY_UPDATED_TAG = "Updated";// ����ʱ����
	final String ENTRY_AUTHOR_NAME_TAG = "Author";// ����������
	final String ENTRY_LINK_TAG = "Link";// ʵ�����ӵ�ַ
	final String ENTRY_AVATOR_TAG = "Image";// Logo��ַ
	final String ENTRY_RSS_NUM_TAG = "RssNum";// ���Ĵ���
	final String ENTRY_IS_CNBLOGS_TAG = "IsCnblogs";// �Ƿ����Բ���԰

	private ArrayList<RssList> listRss;// ���󼯺�
	private RssList entity;// ��������
	private boolean isStartParse;// ��ʼ����
	private StringBuilder currentDataBuilder;// ��ǰȡ����ֵ
	/**
	 * Ĭ�Ϲ��캯��
	 */
	public RssListXmlParser() {
		super();
	}
	/**
	 * ���캯��
	 * 
	 * @return
	 */
	public RssListXmlParser(ArrayList<RssList> list) {
		this.listRss = list;
	}
	/**
	 * ���������
	 * 
	 * @return
	 */
	public ArrayList<RssList> GetRssList() {
		return listRss;
	}
	/**
	 * �ĵ���ʼʱ����
	 */
	public void startDocument() throws SAXException {
		Log.i("Blog", "�ĵ�������ʼ");
		super.startDocument();
		listRss = new ArrayList<RssList>();
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
			} else if (localName.equalsIgnoreCase(ENTRY_ID_TAG)) {// ���
				int id = Integer.parseInt(chars);
				entity.SetRssId(id);
			} else if (localName.equalsIgnoreCase(ENTRY_RSS_NUM_TAG)) {// ���
				int id = Integer.parseInt(chars);
				entity.SetRssNum(id);
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
			} else if (localName.equalsIgnoreCase(ENTRY_IS_CNBLOGS_TAG)) {// �Ƿ����Բ���԰
				int id = Integer.parseInt(chars);
				boolean isCnblogs = id == 1;
				entity.SetIsCnblogs(isCnblogs);
			} else if (localName.equalsIgnoreCase(ENTRY_TAG)) {// ��ֹ
				listRss.add(entity);
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
