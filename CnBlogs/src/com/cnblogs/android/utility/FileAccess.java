package com.cnblogs.android.utility;

import java.io.File;
import java.text.DecimalFormat;

import com.cnblogs.android.core.Config;
import android.os.Environment;

public class FileAccess {
	/**
	 * �����ļ���
	 * 
	 * @param dirName
	 */
	public static void MakeDir(String dirName) {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			File destDir = new File(dirName);
			if (!destDir.exists()) {
				destDir.mkdirs();
			}
		}
	}

	/**
	 * ɾ����·������ʾ���ļ���Ŀ¼�� �����·������ʾһ��Ŀ¼�������ɾ��Ŀ¼�µ������ٽ�Ŀ¼ɾ�������Ըò�������ԭ���Եġ�
	 * ���Ŀ¼�л���Ŀ¼����������ݹ鶯����
	 * 
	 * @param filePath
	 *            Ҫɾ���ļ���Ŀ¼��·����
	 * @return ���ҽ����ɹ�ɾ���ļ���Ŀ¼ʱ������ true�����򷵻� false��
	 */
	public static boolean DeleteFile(String filePath) {
		File file = new File(filePath);
		if (file.listFiles() == null)
			return true;
		else {
			File[] files = file.listFiles();
			for (File deleteFile : files) {
				if (deleteFile.isDirectory())
					DeleteAllFile(deleteFile);
				else
					deleteFile.delete();
			}
		}
		return true;
	}
	/**
	 * ɾ��ȫ���ļ�
	 * 
	 * @param file
	 * @return
	 */
	private static boolean DeleteAllFile(File file) {
		File[] files = file.listFiles();
		for (File deleteFile : files) {
			if (deleteFile.isDirectory()) {
				// ������ļ��У���ݹ�ɾ��������ļ�����ɾ�����ļ���
				if (!DeleteAllFile(deleteFile)) {
					// ���ʧ���򷵻�
					return false;
				}
			} else {
				if (!deleteFile.delete()) {
					// ���ʧ���򷵻�
					return false;
				}
			}
		}
		return file.delete();
	}
	/**
	 * �õ����ݿ��ļ�·��
	 * @return
	 */
	public static String GetDbFileAbsolutePath(){
		String dbPath="/data/data/" + Config.APP_PACKAGE_NAME + "/databases/" + Config.DB_FILE_NAME;
		return dbPath;
	}
	/**
	 * ��ȡ�ļ���С
	 * @param filePath
	 * @return
	 */
	public static long GetFileLength(String filePath){
		File file=new File(filePath);
		return file.length();
	}
	/**
	 * ��ȡ�ļ��д�С
	 * @param dirPath
	 * @return
	 */
	public static long GetPathLength(String dirPath){
		File dir=new File(dirPath);
		return getDirSize(dir);
	}
	/**
	 * ��ȡ�ļ��д�С
	 * @param dir
	 * @return
	 */
	private static long getDirSize(File dir) {  
	    if (dir == null) {  
	        return 0;  
	    }  
	    if (!dir.isDirectory()) {  
	        return 0;  
	    }  
	    long dirSize = 0;  
	    File[] files = dir.listFiles();  
	    for (File file : files) {  
	        if (file.isFile()) {  
	            dirSize += file.length();  
	        } else if (file.isDirectory()) {  
	            dirSize += file.length();  
	            dirSize += getDirSize(file); // �������Ŀ¼��ͨ���ݹ���ü���ͳ��  
	        }  
	    }  
	    return dirSize;  
	} 
	/**
	 * ���ֳ�����ת��ΪKB/MB
	 * @param size
	 * @return
	 */
	public static String GetFileSize(long size){
		int kbSize=(int)size/1024;
		if(kbSize>1024){
			float mbSize=kbSize/1024;
			DecimalFormat formator=new DecimalFormat( "##,###,###.## ");
			return formator.format(mbSize) + "M";
		}
		return kbSize + "K";
	}
}
