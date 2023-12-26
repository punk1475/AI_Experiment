package search;

import java.util.*;

/**
 * @author sunjia
 * @create 2023-11-21 19:12
 **/
public class AStar {
    Array puzzleStart;
    Array puzzleEnd;
    int size;
    AStar(Array puzzleStart,Array puzzleEnd,int size){
        this.puzzleEnd=puzzleEnd;
        this.puzzleStart=puzzleStart;
        this.size=size;
    }
    private int distanceH(Array state){
        int dis=0;
        //遍历状态数组，确定与终点状态不同的值的个数
        for(int i=0;i<size;i++){
            for(int j=0;j<size;j++){
                if(state.array[i][j]!=puzzleEnd.array[i][j]){//二者不同，dis++
                    dis++;
                }
            }
        }
        return dis;
    }

    /**
     * 拓展函数，用于拓展选中的状态
     * @param state 当前的状态值
     * @return 根据当前状态可以产生的状态集合
     * @throws Exception
     */
    private HashSet<Array> expand(Array state) throws Exception {

        HashSet<Array> newState=new HashSet<>();
        //遍历寻找0所在的位置
        for(int i=0;i<size;i++) {
            for (int j=0; j < size; j++) {
                if(state.array[i][j]==0){//找到0的位置后，将其上下左右移动
                    if(i!=0){//不在最上层
                        newState.add(stateExchange(state,i,j,0));
                    }
                    if(i!=size-1){//不在最下层
                        newState.add(stateExchange(state,i,j,1));
                    }
                    if(j!=0){//不在最左侧
                        newState.add(stateExchange(state,i,j,2));
                    }
                    if(j!=size-1){//不在最右侧
                        newState.add(stateExchange(state,i,j,3));
                    }
                    return newState;
                }
            }

        }
        return null;
    }
    //direct指明交换的方向，0、1、2、3分别表示向上下左右交换

    /**
     * 状态改变函数，基于一定规则得出当前状态的一个下一状态
     * @param state 当前状态
     * @param i 当前状态中0的i值
     * @param j 当前状态中0的j值
     * @param direct 指明交换的方向，0、1、2、3分别表示向上下左右交换
     * @return 按对应规则得出的下一个状态
     * @throws Exception
     */
    private Array stateExchange(Array state,int i,int j,int direct) throws Exception {
        Array copy=cloneState(state);//深拷贝一个原状态值出来，防止修改到原先的状态
        int buf;
        switch (direct){
            case(0)://将0向上移动
                buf=state.array[i-1][j];
                copy.array[i-1][j]=0;
                copy.array[i][j]=buf;
                break;
            case(1)://将0向下移动
                buf=state.array[i+1][j];
                copy.array[i+1][j]=0;
                copy.array[i][j]=buf;
                break;
            case (2)://将0向左移动
                buf=state.array[i][j-1];
                copy.array[i][j-1]=0;
                copy.array[i][j]=buf;
                break;
            case(3)://将0向右移动
                buf=state.array[i][j+1];
                copy.array[i][j+1]=0;
                copy.array[i][j]=buf;
                break;
            default://都不是就报错
                throw new Exception("未知的direct！");
        }
        return copy;
    }

    /**
     * 用于深拷贝一个状态值出来，方便在原状态基础上进行修改，同时不影响真正原状态的值
     * @param src 原状态
     * @return 深拷贝的一个，与原状态值相同的状态
     */
    private Array cloneState(Array src){
        Array dst=new Array(new int[src.array.length][src.array[0].length]);
        for(int i=0;i<size;i++){
            //对二重数组内的每一个数组进行深拷贝
            System.arraycopy(src.array[i],0,dst.array[i],0,src.array[i].length);
        }
        return dst;
    }

    /**
     * 选择拓展的状态
     * @param OPEN 选取拓展的open集
     * @param CLOSE 已经被选过的close集
     * @return 一个在f函数下表现最佳的状态
     */
    public Array chooseToExpand(Map<Array,Meta> OPEN,Map<Array,Meta> CLOSE){
        int min=Integer.MAX_VALUE;
        Meta chooseMeta=null;
        Array choose=null;
        Iterator<Array> it=OPEN.keySet().iterator();
        //根据OPEN集的迭代器，遍历OPEN集，选择f，即g+h最小的一个状态
        while (it.hasNext()){
            Array elem=it.next();
            Meta meta=OPEN.get(elem);
            if(min>(meta.distanceG+meta.distanceH)){
                min=meta.distanceG+meta.distanceH;
                chooseMeta=OPEN.get(elem);
                choose=elem;
            }
        }
        //将对应状态移出OPEN集并加入CLOSE集
        OPEN.remove(choose);
        CLOSE.put(choose,chooseMeta);
        return choose;
    }

    /**
     * 打印对应的一个状态
     * @param state 对应状态值
     */
    void printState(Array state){
        for(int i=0;i<size;i++){
            for(int j=0;j<size;j++){//遍历进行打印
                if(state.array[i][j]!=0){
                    System.out.print(state.array[i][j]+" ");
                }else {//将0用空格代替
                    System.out.print("  ");
                }
            }
            System.out.println();
        }
    }

    /**
     * 搜索算法
     * @throws Exception
     */
    public void search() throws Exception {
        //用于存储集合中每个状态以及到达该状态的路径，Meta用于保存到达该状态的路径的父节点
        Map<Array,Meta> OPEN=new HashMap<>();
        Map<Array,Meta> CLOSE=new HashMap<>();
        //将初始集合放入OPEN集
        OPEN.put(puzzleStart,new Meta(0,distanceH(puzzleStart),null));
        //如果OPEN集合不为空，说明还能拓展
        while(!OPEN.isEmpty()){
            //从OPEN集中选一个进行拓展
            Array choose=chooseToExpand(OPEN,CLOSE);
          //得到拓展后的状态集合
            Set<Array> expandedStates=expand(choose);
            //g为目前为止到达该状态经历的拓展次数
            int distanceG=CLOSE.get(choose).distanceG+1;
            //如果拓展出结束状态，将路径保存，直接退出循环
            if(expandedStates.contains(puzzleEnd)){
                Meta meta=new Meta(distanceG,0,choose);
                CLOSE.put(puzzleEnd,meta);
                break;
            }
            //如果没有拓展出结束状态，则遍历拓展出的每个状态，查看是否在OPEN集和CLOSE集中存在
            for(Array expState:expandedStates){
                if(OPEN.containsKey(expState)){
                    //如果在OPEN集中存在，且g大于本次的状态，那么使用此状态进行替换
                    Meta meta=OPEN.get(expState);
                    if(meta.distanceG>distanceG){
                        meta.distanceG=distanceG;
                        meta.fatherNode=choose;
                    }
                }
                //如果在CLOSE集中存在，且g也大于本次的状态，那么将状态重新放回OPEN集
                if(CLOSE.containsKey(expState)) {
                    Meta meta=CLOSE.get(expState);
                    if(meta.distanceG>distanceG){
                        meta.distanceG=distanceG;
                        meta.fatherNode=choose;
                        OPEN.put(expState,meta);
                        CLOSE.remove(expState);
                    }
                }
                //如果都没有，那么直接放入OPEN集合
                if((!OPEN.containsKey(expState))&&(!CLOSE.containsKey(expState))){
                    Meta meta=new Meta(distanceG,distanceH(expState),choose);
                    OPEN.put(expState,meta);
                }
            }
        }
        //如果OPEN集空了也没有找到，表明没有对应的路径通向终点状态
        if(OPEN.isEmpty()){
            System.out.println("没有找到合适的步骤实现转变！");
        }else{
            //如果知道了，将路径按终点到起点的顺序打印出来
            System.out.println("从终点状态追溯到起始状态：");
            Array state=puzzleEnd;
            int step=1;
            while(state!=null){
                System.out.println("倒数第"+step+"步：");
                printState(state);
                state=CLOSE.get(state).fatherNode;
                step++;
            }
            System.out.println("追溯结束！");
        }
    }


    public static void main(String[] args) throws Exception {
        int[][] start=new int[][]{{5,1,2,3},{6,10,4,0},{13,9,7,8},{14,15,11,12}};
        int[][] end=new int[][]{{1,2,3,4},{5,6,7,8},{9,10,11,12},{13,14,15,0}};
        AStar aStar=new AStar(new Array(start),new Array(end),4);
        aStar.search();
    }
}
//用于封装状态的一个类，包含了此状态与对应上一个状态的联系，即父节点
class Meta{
    int distanceG;
    int distanceH;
    Array fatherNode;

    Meta(int G,int H,Array father){
        distanceG=G;
        distanceH=H;
        fatherNode=father;
    }
}
//状态类
class Array{
    int[][] array;
    Array(int[][]array){
        this.array=array;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Array)) return false;

        Array array1 = (Array) o;

        return Arrays.deepEquals(array, array1.array);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(array);
    }
}

