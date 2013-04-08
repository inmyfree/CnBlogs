package com.cnblogs.android.parser;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;
/**
 * ʵ����ת��
 * 
 * @author walkingp
 * 
 */
public class NewsXmlParser extends DefaultHandler {
	final String ENTRY_TAG = "Content";// �����
	final String ENTRY_IMAGE_URL = "ImageUrl";// ͼƬ���

	private String newsContent;// ��������
	private boolean isStartParse;// ��ʼ����
	private StringBuilder currentDataBuilder;// ��ǰȡ����ֵ
	/**
	 * Ĭ�Ϲ��캯��
	 */
	public NewsXmlParser() {
		super();
	}
	/**
	 * ���캯��
	 * 
	 * @return
	 */
	public NewsXmlParser(String content) {
		this.newsContent = content;
	}
	/**
	 * ���������
	 * 
	 * @return
	 */
	public String GetNewsContent() {
		return newsContent;
	}
	/**
	 * �ĵ���ʼʱ����
	 */
	public void startDocument() throws SAXException {
		Log.i("News", "�ĵ�������ʼ");
		super.startDocument();
		currentDataBuilder = new StringBuilder();
	}
	/**
	 * ��ȡ������XML����
	 */
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		if (localName.equalsIgnoreCase(ENTRY_TAG)) {
			newsContent = "";
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
			Log.i("News", "���ڽ���" + localName);
			// ����
			if (localName.equalsIgnoreCase(ENTRY_TAG)) {// ����
				newsContent = chars;
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
