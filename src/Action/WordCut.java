package Action;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Iterator;

import Base.BaseWordCut;
import Helper.FileHelper;

public class WordCut extends BaseWordCut {
	/**
	 * ��ŷִʺ��������µĵ���
	 */
	private HashMap<File, HashMap<String, Integer>> articleWordsMap = new HashMap<File, HashMap<String, Integer>>();

	/**
	 * articleWorsMap��Ӧtf-idf��ʽ
	 */
	private HashMap<File, HashMap<Integer, Double>> tfIdfMap = new HashMap<File, HashMap<Integer, Double>>(); 
	
	/**
	 * �ֵ�����д����label 
	 */
	private HashMap<String, Integer> wordsDict = new HashMap<String, Integer>();
	
	private HashMap<String, Integer> classLabel = new HashMap<String, Integer>();
	
	public WordCut() throws IOException{
		this.loadWordsDict(new File("trainfile/dictionary.txt"));
		this.classLabel = super.loadClassFromFile(new File("trainfile/classLabel.txt"));
	}
	
	/**
	 * ���ļ��м��ص��� �ֵ�
	 * @param file
	 */
	private void loadWordsDict(File file){
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String temp = null;
			while((temp = reader.readLine()) != null){
				String[] str = temp.split(" ");
				wordsDict.put(str[1], Integer.parseInt(str[0]));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private HashMap<File, String> readFile(File[] files) throws Exception{
		HashMap<File, String> articles = new HashMap<File, String>();
		for (File file : files) {
			String content = FileHelper.readTxtOrDoc(file);
			articles.put(file, content);
		}
		return articles;
	}
	
	/**
	 * ͨ���ļ���������� �磺����_1.txt ���ڵ����Ϊ�����Ρ�
	 * @param className
	 * @return
	 */
	private int getClassLabel(String className){
		String[] arr = className.split("_");
		if (classLabel.containsKey(arr[0])) {
			return classLabel.get(arr[0]);
		}else{
			return -1;
		}
	}
	
	/**
	 * ���������½��зִʴ���
	 * @param files
	 * @throws Exception
	 */
	private void cutWord(File[] files) throws Exception{
		HashMap<File, String> articles = this.readFile(files); 
		Iterator<File> artIterator = articles.keySet().iterator();
		while (artIterator.hasNext()) {
			File file = artIterator.next();
			String name = file.getName();
			String content = articles.get(file);
			HashMap<String, Integer> artWords = this.doCutWord(content);
			this.articleWordsMap.put(file, artWords);
			System.out.println(name);
		}
	}
	
	/**
	 * ת��svm���ϸ�ʽ
	 * @param outFile �����·��
	 */
	private void convertToSvmFormat(File outFile){
		try {
			PrintWriter writer = new PrintWriter(outFile);
			Iterator<File> artIterator = articleWordsMap.keySet().iterator();
			while (artIterator.hasNext()) {
				File file = artIterator.next();
				//д��svm���ϵ�����
				writer.print(getClassLabel(file.getName()) + " ");
				
				HashMap<String, Integer> artWords = articleWordsMap.get(file);
				Iterator<String> wordsIterator = artWords.keySet().iterator();
				while (wordsIterator.hasNext()) {
					String item = (String) wordsIterator.next();
					int index = -1;
					if(wordsDict.containsKey(item)){
						index = wordsDict.get(item);
					}				
					int value = artWords.get(item);
					//д��index��value
					writer.print(index + ":" + value + " ");
				}
				writer.println();//д�뻻�з�
			}
			writer.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * �ִʡ�����libsvm���ϵĹ����ӿ�
	 * ��svm���ϱ�����testfile/svm.test��
	 * @param files
	 * @return �������ɵ�svm.test�����ļ�
	 * @throws Exception
	 */
	public static File run(File[] files) throws Exception{
		WordCut model = new WordCut();
		model.cutWord(files);
		File outFile = new File("testfile/svm.test");
		model.convertToSvmFormat(outFile);
		return outFile;
	}
	
	public static void main(String[] args) throws Exception{
		WordCut model = new WordCut();
		model.cutWord(new File[]{
				new File("article/����_1.txt"),
				new File("article/����_2.txt")
				});
		model.convertToSvmFormat(new File("testfile/svm.test"));
	}
	
	
	
}