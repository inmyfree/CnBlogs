package com.cnblogs.android.core;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import com.cnblogs.android.entity.*;
import com.cnblogs.android.parser.RssCateXmlParser;
import com.cnblogs.android.utility.NetHelper;

/**
 * News������
 * @author walkingp
 *
 */
public class RssCateHelper extends DefaultHandler {
	/**
	 * ����ҳ�뷵��News���󼯺�
	 * @return
	 * pageIndex:ҳ�룬��1��ʼ
	 */
	public static ArrayList<RssCate> GetRssCates(){
		String url=Config.URL_RSS_CATE_URL;//��ѡ��ַ
		String dataString=NetHelper.GetXmlContentFromUrl(url,"UTF-8");
		if(dataString.equals("")){
			return null;
		}
		ArrayList<RssCate> list=ParseString(dataString);
		
		return list;
	}
	/**
	 * ���ַ���ת��ΪRssCate����
	 * @return
	 */
	private static ArrayList<RssCate> ParseString(String dataString) {
	      SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
	      ArrayList<RssCate> listCate=new ArrayList<RssCate>();
	      try {
	          XMLReader xmlReader = saxParserFactory.newSAXParser().getXMLReader();
	          RssCateXmlParser handler = new RssCateXmlParser(listCate);
	          xmlReader.setContentHandler(handler);
	          
	          xmlReader.parse(new InputSource(new StringReader(dataString)));
	          listCate=handler.GetRssCateList();
	      } catch (SAXException e) {
	          e.printStackTrace();
	      } catch (ParserConfigurationException e) {
	          e.printStackTrace();
	      } catch (IOException e) {
	          e.printStackTrace();
	      }

	      return listCate;
    }
}





















