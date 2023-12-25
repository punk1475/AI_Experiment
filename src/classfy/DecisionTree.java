package classfy;

import com.sun.jdi.ClassType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class DecisionTree {
    private ArrayList<Data> dataSet = new ArrayList<>(); // 原始数据
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

	//计算log2(x)
	static public double log(double value, double base)
	{
		return Math.log(value) / Math.log(base);
	}
	
	//计算信息熵
	public double calcShannonEnt(ArrayList<Data> data){
		Map<Double,Integer> typeMap=new HashMap<>();
		for(Data valueList:data){
			Double type=valueList.dataValue[attrCount-1];
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
		while (it.hasNext()){
			int count=typeMap.get(it.next());
			double p=(double) count/(double) size;
			sum+=(-p)*log(p,2);
		}
		return sum;
	}

	
	//划分数据集
	private ArrayList<Data>[] splitDataSet(ArrayList<Data> data, int featureIndex, int splitPoint) {
		ArrayList<Data> sub1=new ArrayList<>();
		ArrayList<Data> sub2=new ArrayList<>();
		chooser.setIndex(featureIndex);
		Collections.sort(data);
		boolean[] boolArray=Arrays.copyOf(data.get(0).unusedAttr,data.get(0).unusedAttr.length);
		boolArray[featureIndex]=false;
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
	
	//返回信息增益最大的划分属性,及对应的分裂值
	int[] chooseBestFeatureToSplit(ArrayList<Data> data)
	{
		int selectedIndex=0;
		int splitPoint=-1;
		double minEnt=Double.MAX_VALUE;
		for(int i=0;i<attrCount-1;i++){
			double[] res=getEnt(data,i);
			if(res==null){
				continue;
			}
			if(res[0]<minEnt){
				minEnt=res[0];
				splitPoint=(int)res[1];
				selectedIndex=i;
			}
		}
		if(minEnt==Double.MAX_VALUE){
			return null;
		}
		return new int[]{selectedIndex,splitPoint};
	}

	//计算每种属性划分后的信息增益,并返回对应的分裂点
	double[] getEnt(ArrayList<Data> dataList,int index){
		if(!dataList.get(0).unusedAttr[index]){
			return null;
		}
		chooser.setIndex(index);
		Collections.sort(dataList);
		int size=dataList.size();
		double minEnt=Double.MAX_VALUE;
		int splitPoint=-1;
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
	
	//投票，返回支持度最高的类标签
	Double majorityCount(Vector<Double> classList)
	{
		HashMap<Double, Integer> classCount = new HashMap<>();
		int maxVote = 0;
		Double majorityClass = null;
		
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

	//创建决策树
	HashMap<String, Object> createTree(ArrayList<Data> dataSet)
	{
		Vector<Double> classList = new Vector<>();
		HashMap<String, Object> myTree = new HashMap<>();

		for (Data data : dataSet) {
			classList.add(data.dataValue[attrCount - 1]);
		}
		//TO-DO:
		//判断该分支下实例的类别是否全部相同，相同则停止划分，并返回该分支（叶子）
		Set<Double> classSet=new HashSet<>(classList);
		if(classSet.size()==classList.size()){
			myTree.put("type",classList.get(0));
			myTree.put("isLeaf",true);
		}
		//已经没有划分属性，类别使用多数表决，返回分支
		int[] res=chooseBestFeatureToSplit(dataSet);
		if(res==null){
			Double maxType=majorityCount(classList);
			myTree.put("type",maxType);
			myTree.put("isLeaf",true);
		}
		
		//选取最佳的划分特征
		else{
			myTree.put("isLeaf",false);
			myTree.put("chooseAttr",res[0]);
			dataSet.get(0).chooser.setIndex(res[0]);
			Collections.sort(dataSet);
			double splitValue=dataSet.get(res[1]).dataValue[res[0]];
			myTree.put("splitValue",splitValue);
			ArrayList<Data>[] childDataSet=splitDataSet(dataSet,res[0],res[1]);
			for(int i=0;i<2;i++){
				HashMap<String,Object>childTree;
				if(childDataSet[i].size()==0){
					childTree=new HashMap<>();
					childTree.put("type",majorityCount(classList));
					childTree.put("isLeaf",true);
				}else {
					childTree=createTree(childDataSet[i]);
				}
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
		//递归地构建决策树
		return myTree;
	}
	
	//遍历树丛根到叶子，从而得到预测结果
	Double classify(HashMap<String, Object> tree, Data testData)
	{
		while (!(boolean) tree.get("isLeaf")){
			int chooseAttr=(int) tree.get("chooseAttr");
			double splitValue=(double)tree.get("splitValue");
			HashMap<String,Object> children= (HashMap<String, Object>) tree.get("child");
			if(testData.dataValue[chooseAttr]<splitValue){
				tree= (HashMap<String, Object>) children.get("less");
			}else{
				tree= (HashMap<String, Object>) children.get("more");
			}
		}
		return (Double) tree.get("type");
	}

	public  void predict(){
		int correct=0;
		HashMap<String,Object> decisionTree=createTree(dataSet);
		for(Data test:testDataSet){
			if(classify(decisionTree,test)==test.dataValue[attrCount-1]){
				correct++;
			}
		}
		System.out.println("预测正确率为:"+(double)((double)correct/(double)testDataSet.size()));
	}
}

class Data implements Comparable<Data>{
	double[] dataValue;
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

class ComparatorChooser{
	int attributeIndex;

	ComparatorChooser(){
		attributeIndex=0;
	}

	public void setIndex(int index){
		attributeIndex=index;
	}
}
