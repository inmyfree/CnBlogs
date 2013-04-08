package com.cnblogs.android.parser;

import java.util.ArrayList;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import android.util.Log;
import org.xml.sax.Attributes;

import com.cnblogs.android.entity.*;
/**
 * Blog����xml������
 * 
 * @author walkingp
 * 
 */
public class RssCateXmlParser extends DefaultHandler {
	final String ENTRY_TAG = "Item";// �����
	final String ENTRY_ID_TAG = "CateId";// ��ű��
	final String ENTRY_TITLE_TAG = "CateName";// ������
	final String ENTRY_ICON_TAG = "Icon";// ͼƬ���
	final String ENTRY_SUMMARY_TAG = "Summary";// ���

	private ArrayList<RssCate> listRss;// ���󼯺�
	private RssCate entity;// ��������
	private boolean isStartParse;// ��ʼ����
	private StringBuilder currentDataBuilder;// ��ǰȡ����ֵ
	/**
	 * Ĭ�Ϲ��캯��
	 */
	public RssCateXmlParser() {
		super();
	}
	/**
	 * ���캯��
	 * 
	 * @return
	 */
	public RssCateXmlParser(ArrayList<RssCate> list) {
		this.listRss = list;
	}
	/**
	 * ���������
	 * 
	 * @return
	 */
	public ArrayList<RssCate> GetRssCateList() {
		return listRss;
	}
	/**
	 * �ĵ���ʼʱ����
	 */
	public void startDocument() throws SAXException {
		Log.i("Blog", "�ĵ�������ʼ");
		super.startDocument();
		listRss = new ArrayList<RssCate>();
		currentDataBuilder = new StringBuilder();
	}
	/**
	 * ��ȡ������XML����
	 */
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		if (localName.equalsIgnoreCase(ENTRY_TAG)) {
			entity = new RssCate();
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
				entity.SetCateName(chars);
			} else if (localName.equalsIgnoreCase(ENTRY_ICON_TAG)) {// ͼƬ
				entity.SetIcon(chars);
			} else if (localName.equalsIgnoreCase(ENTRY_SUMMARY_TAG)) {// ���
				entity.SetSummary(chars);
			} else if (localName.equalsIgnoreCase(ENTRY_ID_TAG)) {// ���
				int id = Integer.parseInt(chars);
				entity.SetCateId(id);
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
