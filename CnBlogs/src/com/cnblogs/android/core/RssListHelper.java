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
import com.cnblogs.android.entity.*;
import com.cnblogs.android.parser.RssListAddXmlParser;
import com.cnblogs.android.parser.RssListXmlParser;
import com.cnblogs.android.utility.NetHelper;

/**
 * Rss������
 * 
 * @author walkingp
 * 
 */
public class RssListHelper extends DefaultHandler {
	/**
	 * ������ַ����
	 * @param url
	 * @return
	 */
	public static RssList GetRssEntity(String url){
		String dataString = NetHelper.getData(url, "UTF-8");
		if (dataString.equals("")) {
			return null;
		}
		return ParseString2Entity(dataString);
	}
	/**
	 * ���ݷ���õ��Ƽ����б�
	 * 
	 * @return
	 */
	public static List<RssList> GetRssList(int cateId) {
		String url = Config.URL_RSS_LIST_URL.replace("{0}",
				String.valueOf(cateId));// ���ݵ�ַ
		String dataString = NetHelper.GetXmlContentFromUrl(url, "UTF-8");
		if (dataString.equals("")) {
			return null;
		}
		ArrayList<RssList> list = ParseString(dataString);

		return list;
	}
	/**
	 * ���ַ���ת��ΪRss����
	 * 
	 * @return
	 */
	private static ArrayList<RssList> ParseString(String dataString) {
		SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
		ArrayList<RssList> listRss = new ArrayList<RssList>();
		try {
			XMLReader xmlReader = saxParserFactory.newSAXParser()
					.getXMLReader();
			RssListXmlParser handler = new RssListXmlParser(listRss);
			xmlReader.setContentHandler(handler);

			xmlReader.parse(new InputSource(new StringReader(dataString)));
			listRss = handler.GetRssList();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return listRss;
	}

	/**
	 * ���ַ���ת��ΪRss����
	 * 
	 * @return
	 */
	private static RssList ParseString2Entity(String dataString) {
		SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
		RssList entity = new RssList();
		try {
			XMLReader xmlReader = saxParserFactory.newSAXParser()
					.getXMLReader();
			RssListAddXmlParser handler = new RssListAddXmlParser(entity);
			xmlReader.setContentHandler(handler);

			xmlReader.parse(new InputSource(new StringReader(dataString)));
			entity = handler.GetRssList();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return entity;
	}
}
