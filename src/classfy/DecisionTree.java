package classfy;


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

	/**
	 * 读取数据文件，并生成对应数据集合
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

	//计算log2(x)

	/**
	 * 用于计算log2(x)
	 * @param value 值
	 * @param base 基数
	 * @return 以base为低，value的对数值
	 */
	static public double log(double value, double base)
	{
		return Math.log(value) / Math.log(base);
	}
	
	//计算信息熵
	public double calcShannonEnt(ArrayList<Data> data){
		Map<Double,Integer> typeMap=new HashMap<>();
		//遍历数据集合
		for(Data valueList:data){
			//取每个数据的类别值
			Double type=valueList.dataValue[attrCount-1];
			//将类别放入typeMap中进行统计
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
		//通过迭代器遍历，获取typeMap中各类别的数量，并以此计算香农熵
		while (it.hasNext()){
			int count=typeMap.get(it.next());
			double p=(double) count/(double) size;
			sum+=(-p)*log(p,2);
		}
		//返回计算出的熵
		return sum;
	}

	


	/**
	 * 用于划分数据集
	 * @param data 输入的数据集合
	 * @param featureIndex 选取的属性索引值
	 * @param splitPoint 该数据集按照选取属性从小到大排列后，作为分裂点的数据的索引
	 * @return 按分裂点划分出的两个数据集
	 */
	private ArrayList<Data>[] splitDataSet(ArrayList<Data> data, int featureIndex, int splitPoint) {
		ArrayList<Data> sub1=new ArrayList<>();
		ArrayList<Data> sub2=new ArrayList<>();
		//设定数据进行比较的维度
		chooser.setIndex(featureIndex);
		//按此维度的值进行排序，升序
		Collections.sort(data);
		//用于保存未使用过的属性
		boolean[] boolArray=Arrays.copyOf(data.get(0).unusedAttr,data.get(0).unusedAttr.length);
		//将本次使用的属性从上面划去
		boolArray[featureIndex]=false;
		//遍历数据集，将索引小于splitPoint的放入sub1，将大于的放入sub2
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
	 * 返回信息增益最大的划分属性,及对应的分裂值
	 * @param data 对应数据集
	 * @return 第一个元素为属性索引，第二个为分裂索引
	 */
	int[] chooseBestFeatureToSplit(ArrayList<Data> data)
	{
		int selectedIndex=0;
		int splitPoint=-1;
		double minEnt=Double.MAX_VALUE;
		//遍历全部的属性，选取Gain最大，即分裂后的平均熵最小的属性
		for(int i=0;i<attrCount-1;i++){
			double[] res=getEnt(data,i);
			//为空，说明对应属性已经使用过
			if(res==null){
				continue;
			}
			//取熵最低的属性及对应分裂点
			if(res[0]<minEnt){
				minEnt=res[0];
				splitPoint=(int)res[1];
				selectedIndex=i;
			}
		}
		//表明没有可用的属性
		if(minEnt==Double.MAX_VALUE){
			return null;
		}
		//返回得到的结果
		return new int[]{selectedIndex,splitPoint};
	}

	/**
	 * 计算每种属性划分后的信息增益,并返回对应的分裂点
	 * @param dataList 数据集
	 * @param index 属性索引
	 * @return 第一个为分裂后的平均信息熵，相当于信息增益
	 */
	double[] getEnt(ArrayList<Data> dataList,int index){
		if(!dataList.get(0).unusedAttr[index]){
			return null;
		}
		//设置比较的维度并排序
		chooser.setIndex(index);
		Collections.sort(dataList);
		int size=dataList.size();
		double minEnt=Double.MAX_VALUE;
		int splitPoint=-1;
		//从1开始尝试分裂，选择分裂后平均信息熵最小的数据点的索引
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
	 * 投票，返回支持度最高的类标签
	 * @param classList 一个存放对应数据集的全部类别值的集合
	 * @return 支持度最高的类
	 */
	Double majorityCount(Vector<Double> classList)
	{
		HashMap<Double, Integer> classCount = new HashMap<>();
		int maxVote = 0;
		Double majorityClass = null;
		//遍历集合，统计出各类别的数量
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
		//再遍历选出数量最多的类
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
	 * 递归创建决策树
	 * @param dataSet 数据集
	 * @return 一个Map映射，通过递归的构造形成树
	 */
	HashMap<String, Object> createTree(ArrayList<Data> dataSet)
	{
		Vector<Double> classList = new Vector<>();
		HashMap<String, Object> myTree = new HashMap<>();
		//遍历数据集，获取类别集合
		for (Data data : dataSet) {
			classList.add(data.dataValue[attrCount - 1]);
		}
		//判断该分支下实例的类别是否全部相同，相同则停止划分，并返回该分支（叶子）
		Set<Double> classSet=new HashSet<>(classList);
		if(classSet.size()==classList.size()){
			myTree.put("type",classList.get(0));
			myTree.put("isLeaf",true);
		}
		//已经没有划分属性，类别使用多数表决，返回分支
		int[] res=chooseBestFeatureToSplit(dataSet);//选取最佳划分属性
		if(res==null){
			Double maxType=majorityCount(classList);
			myTree.put("type",maxType);
			myTree.put("isLeaf",true);
		}
		//选取最佳的划分特征
		else{
			myTree.put("isLeaf",false);
			myTree.put("chooseAttr",res[0]);
			//按最佳划分属性排序
			dataSet.get(0).chooser.setIndex(res[0]);
			Collections.sort(dataSet);
			double splitValue=dataSet.get(res[1]).dataValue[res[0]];
			//设置spltValue属性作为后面分类时比较的依据，其值为排序后对应索引的该属性值
			myTree.put("splitValue",splitValue);
			//按最佳划分属性进行划分
			ArrayList<Data>[] childDataSet=splitDataSet(dataSet,res[0],res[1]);
			//对于划分出的俩个子集
			for(int i=0;i<2;i++){
				HashMap<String,Object>childTree;
				//如果子集为空，作为叶子节点，以本次集合中的多数类作为其类别
				if(childDataSet[i].size()==0){
					childTree=new HashMap<>();
					childTree.put("type",majorityCount(classList));
					childTree.put("isLeaf",true);
				}else {
					//否则进行递归的构建
					childTree=createTree(childDataSet[i]);
				}
				//将子集形成的节点映射到本节点中
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

	/**
	 * 用于预测，遍历树丛根到叶子，从而得到预测结果
	 * @param tree 形成的树
	 * @param testData 测试数据
	 * @return 预测的类别
	 */
	Double classify(HashMap<String, Object> tree, Data testData)
	{
		//如果不是叶子，继续向下走
		while (!(boolean) tree.get("isLeaf")){
			//得到划分属性的索引。并选择对应属性值与节点中参考值比较
			int chooseAttr=(int) tree.get("chooseAttr");
			double splitValue=(double)tree.get("splitValue");
			HashMap<String,Object> children= (HashMap<String, Object>) tree.get("child");
			//根据比较的结果进入对应节点
			if(testData.dataValue[chooseAttr]<splitValue){
				tree= (HashMap<String, Object>) children.get("less");
			}else{
				tree= (HashMap<String, Object>) children.get("more");
			}
		}
		return (Double) tree.get("type");
	}

	/**
	 * 用于计算测试集预测的准确率
	 */
	public  void predict(){
		int correct=0;
		//用训练集构建决策树
		HashMap<String,Object> decisionTree=createTree(dataSet);
		//依次对测试集全部数据进行预测，并记录结果
		for(Data test:testDataSet){
			if(classify(decisionTree,test)==test.dataValue[attrCount-1]){
				correct++;
			}
		}
		//打印出记录的准确率
		System.out.println("预测正确率为:"+(double)((double)correct/(double)testDataSet.size()));
	}
}

/**
 * 封装出的一个数据类
 */
class Data implements Comparable<Data>{
	double[] dataValue;
	//比较器类，用于选择数据比较的维度
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
 * 用于选择数据比较的维度，是数据类的内部类
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
