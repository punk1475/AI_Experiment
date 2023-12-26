package classfy;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class DecisionTree {
    private ArrayList<Data> dataSet = new ArrayList<>(); // ԭʼ����
	private ArrayList<Data> testDataSet = new ArrayList<>();
	private int attrCount;
	private ComparatorChooser chooser;
	public static void main(String[] args) {
		DecisionTree d = new DecisionTree("resource/traindata.txt","resource/testdata.txt");
		d.predict();
	}
	
	public DecisionTree(String trainFilePath,String testFilePath)
	{
		readFile("traindata",new File(trainFilePath));
		readFile("testdata",new File(testFilePath));
	}

	/**
	 * ��ȡ�����ļ��������ɶ�Ӧ���ݼ���
	 * @param fileType
	 * @param file
	 */
	private void readFile(String fileType,File file){
		try {
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			String line;
			chooser=new ComparatorChooser();
			while ((line = br.readLine()) != null) {
				if (line.startsWith(fileType) || line.startsWith("];")) {
					continue;
				}
				String[] items=line.split("\t");
				attrCount=items.length;
				boolean[] unusedAttr=new boolean[attrCount-1];
				double[] values=new double[attrCount];
				for(int i=0;i<attrCount;i++){
					values[i]=Double.parseDouble(items[i]);
				}
				for(int i=0;i<attrCount-1;i++){
					unusedAttr[i]=true;
				}
				if(fileType.equals("testdata")){
					testDataSet.add(new Data(values,chooser,unusedAttr));
				}else{
					dataSet.add(new Data(values,chooser,unusedAttr));
				}
			}
			br.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	//����log2(x)

	/**
	 * ���ڼ���log2(x)
	 * @param value ֵ
	 * @param base ����
	 * @return ��baseΪ�ͣ�value�Ķ���ֵ
	 */
	static public double log(double value, double base)
	{
		return Math.log(value) / Math.log(base);
	}
	
	//������Ϣ��
	public double calcShannonEnt(ArrayList<Data> data){
		Map<Double,Integer> typeMap=new HashMap<>();
		//�������ݼ���
		for(Data valueList:data){
			//ȡÿ�����ݵ����ֵ
			Double type=valueList.dataValue[attrCount-1];
			//��������typeMap�н���ͳ��
			if(typeMap.containsKey(type)){
				int count=typeMap.get(type);
				count++;
				typeMap.put(type,count);
			}else {
				typeMap.put(type,1);
			}
		}
		double sum=0;
		Iterator<Double> it=typeMap.keySet().iterator();
		int size=data.size();
		//ͨ����������������ȡtypeMap�и��������������Դ˼�����ũ��
		while (it.hasNext()){
			int count=typeMap.get(it.next());
			double p=(double) count/(double) size;
			sum+=(-p)*log(p,2);
		}
		//���ؼ��������
		return sum;
	}

	


	/**
	 * ���ڻ������ݼ�
	 * @param data ��������ݼ���
	 * @param featureIndex ѡȡ����������ֵ
	 * @param splitPoint �����ݼ�����ѡȡ���Դ�С�������к���Ϊ���ѵ�����ݵ�����
	 * @return �����ѵ㻮�ֳ����������ݼ�
	 */
	private ArrayList<Data>[] splitDataSet(ArrayList<Data> data, int featureIndex, int splitPoint) {
		ArrayList<Data> sub1=new ArrayList<>();
		ArrayList<Data> sub2=new ArrayList<>();
		//�趨���ݽ��бȽϵ�ά��
		chooser.setIndex(featureIndex);
		//����ά�ȵ�ֵ������������
		Collections.sort(data);
		//���ڱ���δʹ�ù�������
		boolean[] boolArray=Arrays.copyOf(data.get(0).unusedAttr,data.get(0).unusedAttr.length);
		//������ʹ�õ����Դ����滮ȥ
		boolArray[featureIndex]=false;
		//�������ݼ���������С��splitPoint�ķ���sub1�������ڵķ���sub2
		for(int i=0;i<splitPoint;i++){
			Data item=data.get(i);
			Data newData=new Data(item.dataValue, chooser,boolArray);
			sub1.add(newData);
		}
		int size=data.size();
		for(int i=splitPoint;i<size;i++){
			Data item=data.get(i);
			Data newData=new Data(item.dataValue, chooser,boolArray);
			sub2.add(newData);
		}
		return new ArrayList[]{sub1,sub2};
	}
	


	/**
	 * ������Ϣ�������Ļ�������,����Ӧ�ķ���ֵ
	 * @param data ��Ӧ���ݼ�
	 * @return ��һ��Ԫ��Ϊ�����������ڶ���Ϊ��������
	 */
	int[] chooseBestFeatureToSplit(ArrayList<Data> data)
	{
		int selectedIndex=0;
		int splitPoint=-1;
		double minEnt=Double.MAX_VALUE;
		//����ȫ�������ԣ�ѡȡGain��󣬼����Ѻ��ƽ������С������
		for(int i=0;i<attrCount-1;i++){
			double[] res=getEnt(data,i);
			//Ϊ�գ�˵����Ӧ�����Ѿ�ʹ�ù�
			if(res==null){
				continue;
			}
			//ȡ����͵����Լ���Ӧ���ѵ�
			if(res[0]<minEnt){
				minEnt=res[0];
				splitPoint=(int)res[1];
				selectedIndex=i;
			}
		}
		//����û�п��õ�����
		if(minEnt==Double.MAX_VALUE){
			return null;
		}
		//���صõ��Ľ��
		return new int[]{selectedIndex,splitPoint};
	}

	/**
	 * ����ÿ�����Ի��ֺ����Ϣ����,�����ض�Ӧ�ķ��ѵ�
	 * @param dataList ���ݼ�
	 * @param index ��������
	 * @return ��һ��Ϊ���Ѻ��ƽ����Ϣ�أ��൱����Ϣ����
	 */
	double[] getEnt(ArrayList<Data> dataList,int index){
		if(!dataList.get(0).unusedAttr[index]){
			return null;
		}
		//���ñȽϵ�ά�Ȳ�����
		chooser.setIndex(index);
		Collections.sort(dataList);
		int size=dataList.size();
		double minEnt=Double.MAX_VALUE;
		int splitPoint=-1;
		//��1��ʼ���Է��ѣ�ѡ����Ѻ�ƽ����Ϣ����С�����ݵ������
		for(int i=1;i<size;i++){
			ArrayList<Data> sub1=new ArrayList<>(dataList.subList(0,i)) ;
			ArrayList<Data> sub2=new ArrayList<>(dataList.subList(i,size));
			double ent=(double) sub1.size()/(double)size*calcShannonEnt(sub1)+(double) sub2.size()/(double)size*calcShannonEnt(sub2);
			if(ent<minEnt){
				minEnt=ent;
				splitPoint=i;
			}
		}
		double[] res={minEnt,splitPoint};
		return res;
	}
	


	/**
	 * ͶƱ������֧�ֶ���ߵ����ǩ
	 * @param classList һ����Ŷ�Ӧ���ݼ���ȫ�����ֵ�ļ���
	 * @return ֧�ֶ���ߵ���
	 */
	Double majorityCount(Vector<Double> classList)
	{
		HashMap<Double, Integer> classCount = new HashMap<>();
		int maxVote = 0;
		Double majorityClass = null;
		//�������ϣ�ͳ�Ƴ�����������
		for (Double clazz : classList) {
			double classType=clazz;
			if (!classCount.containsKey(classType)) {
				classCount.put(classType, 1);
			}
			else 
			{
				classCount.put(classType, classCount.get(classType) + 1);
			}
		}
		//�ٱ���ѡ������������
		Iterator<Double> iterator = classCount.keySet().iterator();
		while (iterator.hasNext()) {
			double key = iterator.next();
			if (classCount.get(key) > maxVote) {
				maxVote = classCount.get(key);
				majorityClass = key;
			}
		}
		return majorityClass;	
	}


	/**
	 * �ݹ鴴��������
	 * @param dataSet ���ݼ�
	 * @return һ��Mapӳ�䣬ͨ���ݹ�Ĺ����γ���
	 */
	HashMap<String, Object> createTree(ArrayList<Data> dataSet)
	{
		Vector<Double> classList = new Vector<>();
		HashMap<String, Object> myTree = new HashMap<>();
		//�������ݼ�����ȡ��𼯺�
		for (Data data : dataSet) {
			classList.add(data.dataValue[attrCount - 1]);
		}
		//�жϸ÷�֧��ʵ��������Ƿ�ȫ����ͬ����ͬ��ֹͣ���֣������ظ÷�֧��Ҷ�ӣ�
		Set<Double> classSet=new HashSet<>(classList);
		if(classSet.size()==classList.size()){
			myTree.put("type",classList.get(0));
			myTree.put("isLeaf",true);
		}
		//�Ѿ�û�л������ԣ����ʹ�ö�����������ط�֧
		int[] res=chooseBestFeatureToSplit(dataSet);//ѡȡ��ѻ�������
		if(res==null){
			Double maxType=majorityCount(classList);
			myTree.put("type",maxType);
			myTree.put("isLeaf",true);
		}
		//ѡȡ��ѵĻ�������
		else{
			myTree.put("isLeaf",false);
			myTree.put("chooseAttr",res[0]);
			//����ѻ�����������
			dataSet.get(0).chooser.setIndex(res[0]);
			Collections.sort(dataSet);
			double splitValue=dataSet.get(res[1]).dataValue[res[0]];
			//����spltValue������Ϊ�������ʱ�Ƚϵ����ݣ���ֵΪ������Ӧ�����ĸ�����ֵ
			myTree.put("splitValue",splitValue);
			//����ѻ������Խ��л���
			ArrayList<Data>[] childDataSet=splitDataSet(dataSet,res[0],res[1]);
			//���ڻ��ֳ��������Ӽ�
			for(int i=0;i<2;i++){
				HashMap<String,Object>childTree;
				//����Ӽ�Ϊ�գ���ΪҶ�ӽڵ㣬�Ա��μ����еĶ�������Ϊ�����
				if(childDataSet[i].size()==0){
					childTree=new HashMap<>();
					childTree.put("type",majorityCount(classList));
					childTree.put("isLeaf",true);
				}else {
					//������еݹ�Ĺ���
					childTree=createTree(childDataSet[i]);
				}
				//���Ӽ��γɵĽڵ�ӳ�䵽���ڵ���
				if(myTree.containsKey("child")){
					HashMap<String,Object>childTrees= (HashMap<String, Object>) myTree.get("child");
					if(i==0){
						childTrees.put("less",childTree);
					}else {
						childTrees.put("more",childTree);
					}
				}else {
					HashMap<String,Object>childTrees= new HashMap<>();
					if(i==0){
						childTrees.put("less",childTree);
					}else {
						childTrees.put("more",childTree);
					}
					myTree.put("child",childTrees);
				}
			}
		}
		//�ݹ�ع���������
		return myTree;
	}

	/**
	 * ����Ԥ�⣬�������Ը���Ҷ�ӣ��Ӷ��õ�Ԥ����
	 * @param tree �γɵ���
	 * @param testData ��������
	 * @return Ԥ������
	 */
	Double classify(HashMap<String, Object> tree, Data testData)
	{
		//�������Ҷ�ӣ�����������
		while (!(boolean) tree.get("isLeaf")){
			//�õ��������Ե���������ѡ���Ӧ����ֵ��ڵ��вο�ֵ�Ƚ�
			int chooseAttr=(int) tree.get("chooseAttr");
			double splitValue=(double)tree.get("splitValue");
			HashMap<String,Object> children= (HashMap<String, Object>) tree.get("child");
			//���ݱȽϵĽ�������Ӧ�ڵ�
			if(testData.dataValue[chooseAttr]<splitValue){
				tree= (HashMap<String, Object>) children.get("less");
			}else{
				tree= (HashMap<String, Object>) children.get("more");
			}
		}
		return (Double) tree.get("type");
	}

	/**
	 * ���ڼ�����Լ�Ԥ���׼ȷ��
	 */
	public  void predict(){
		int correct=0;
		//��ѵ��������������
		HashMap<String,Object> decisionTree=createTree(dataSet);
		//���ζԲ��Լ�ȫ�����ݽ���Ԥ�⣬����¼���
		for(Data test:testDataSet){
			if(classify(decisionTree,test)==test.dataValue[attrCount-1]){
				correct++;
			}
		}
		//��ӡ����¼��׼ȷ��
		System.out.println("Ԥ����ȷ��Ϊ:"+(double)((double)correct/(double)testDataSet.size()));
	}
}

/**
 * ��װ����һ��������
 */
class Data implements Comparable<Data>{
	double[] dataValue;
	//�Ƚ����࣬����ѡ�����ݱȽϵ�ά��
	ComparatorChooser chooser;
	boolean[] unusedAttr;
	Data(double[] data,ComparatorChooser chooser,boolean[] unusedAttr){
		dataValue=data;
		this.chooser=chooser;
		this.unusedAttr=unusedAttr;
	}

	@Override
	public int compareTo(Data o) {
		int index= chooser.attributeIndex;
		if(dataValue[index]<o.dataValue[index]){
			return -1;
		}else if(dataValue[index]>o.dataValue[index]){
			return 1;
		}
		return 0;
	}
}

/**
 * ����ѡ�����ݱȽϵ�ά�ȣ�����������ڲ���
 */
class ComparatorChooser{
	int attributeIndex;

	ComparatorChooser(){
		attributeIndex=0;
	}

	public void setIndex(int index){
		attributeIndex=index;
	}
}
