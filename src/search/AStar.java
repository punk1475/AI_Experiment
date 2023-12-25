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

    private HashSet<Array> expand(Array state) throws Exception {

        HashSet<Array> newState=new HashSet<>();
        for(int i=0;i<size;i++) {
            for (int j=0; j < size; j++) {
                if(state.array[i][j]==0){
                    if(i!=0){
                        newState.add(stateExchange(state,i,j,0));
                    }
                    if(i!=size-1){
                        newState.add(stateExchange(state,i,j,1));
                    }
                    if(j!=0){
                        newState.add(stateExchange(state,i,j,2));
                    }
                    if(j!=size-1){
                        newState.add(stateExchange(state,i,j,3));
                    }
                    return newState;
                }
            }

        }
        return null;
    }
//direct指明交换的方向，0、1、2、3分别表示向上下左右交换
    private Array stateExchange(Array state,int i,int j,int direct) throws Exception {
        Array copy=cloneState(state);
        int buf;
        switch (direct){

            case(0):
                buf=state.array[i-1][j];
                copy.array[i-1][j]=0;
                copy.array[i][j]=buf;
                break;
            case(1):
                buf=state.array[i+1][j];
                copy.array[i+1][j]=0;
                copy.array[i][j]=buf;
                break;
            case (2):
                buf=state.array[i][j-1];
                copy.array[i][j-1]=0;
                copy.array[i][j]=buf;
                break;
            case(3):
                buf=state.array[i][j+1];
                copy.array[i][j+1]=0;
                copy.array[i][j]=buf;
                break;
            default:
                throw new Exception("未知的direct！");
        }
        return copy;
    }

    private Array cloneState(Array src){
        Array dst=new Array(new int[src.array.length][src.array[0].length]);
        for(int i=0;i<size;i++){
            System.arraycopy(src.array[i],0,dst.array[i],0,src.array[i].length);
        }
        return dst;
    }

    public Array chooseToExpand(Map<Array,Meta> OPEN,Map<Array,Meta> CLOSE){
        int min=Integer.MAX_VALUE;
        Meta chooseMeta=null;
        Array choose=null;
        Iterator<Array> it=OPEN.keySet().iterator();
        while (it.hasNext()){
            Array elem=it.next();
            Meta meta=OPEN.get(elem);
            if(min>(meta.distanceG+meta.distanceH)){
                min=meta.distanceG+meta.distanceH;
                chooseMeta=OPEN.get(elem);
                choose=elem;
            }
        }
        OPEN.remove(choose);
        CLOSE.put(choose,chooseMeta);
        return choose;
    }
    void printState(Array state){
        for(int i=0;i<size;i++){
            for(int j=0;j<size;j++){
                if(state.array[i][j]!=0){
                    System.out.print(state.array[i][j]+" ");
                }else {
                    System.out.print("  ");
                }
            }
            System.out.println();
        }
    }
    public void search() throws Exception {
        Map<Array,Meta> OPEN=new HashMap<>();
        Map<Array,Meta> CLOSE=new HashMap<>();
        OPEN.put(puzzleStart,new Meta(0,distanceH(puzzleStart),null));
        while(!OPEN.isEmpty()){
            Array choose=chooseToExpand(OPEN,CLOSE);
            //printState(choose);
            //System.out.println(CLOSE.get(choose).distanceG+"--open size "+OPEN.size());
            Set<Array> expandedStates=expand(choose);
            int distanceG=CLOSE.get(choose).distanceG+1;
            if(expandedStates.contains(puzzleEnd)){
                Meta meta=new Meta(distanceG,0,choose);
                CLOSE.put(puzzleEnd,meta);
                break;
            }
            for(Array expState:expandedStates){
                if(OPEN.containsKey(expState)){
                    Meta meta=OPEN.get(expState);
                    if(meta.distanceG>distanceG){
                        meta.distanceG=distanceG;
                        meta.fatherNode=choose;
                    }
                }
                if(CLOSE.containsKey(expState)) {
                    Meta meta=CLOSE.get(expState);
                    if(meta.distanceG>distanceG){
                        meta.distanceG=distanceG;
                        meta.fatherNode=choose;
                        OPEN.put(expState,meta);
                        CLOSE.remove(expState);
                    }
                }
                if((!OPEN.containsKey(expState))&&(!CLOSE.containsKey(expState))){
                    Meta meta=new Meta(distanceG,distanceH(expState),choose);
                    OPEN.put(expState,meta);
                }
            }
        }
        if(OPEN.isEmpty()){
            System.out.println("没有找到合适的步骤实现转变！");
        }else{
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
        int[][] start=new int[][]{{1,3,0},{8,2,4},{7,6,5}};
        int[][] end=new int[][]{{1,2,3},{8,0,4},{7,6,5}};
        AStar aStar=new AStar(new Array(start),new Array(end),3);
        aStar.search();
    }
}

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

