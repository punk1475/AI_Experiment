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
        //����״̬���飬ȷ�����յ�״̬��ͬ��ֵ�ĸ���
        for(int i=0;i<size;i++){
            for(int j=0;j<size;j++){
                if(state.array[i][j]!=puzzleEnd.array[i][j]){//���߲�ͬ��dis++
                    dis++;
                }
            }
        }
        return dis;
    }

    /**
     * ��չ������������չѡ�е�״̬
     * @param state ��ǰ��״ֵ̬
     * @return ���ݵ�ǰ״̬���Բ�����״̬����
     * @throws Exception
     */
    private HashSet<Array> expand(Array state) throws Exception {

        HashSet<Array> newState=new HashSet<>();
        //����Ѱ��0���ڵ�λ��
        for(int i=0;i<size;i++) {
            for (int j=0; j < size; j++) {
                if(state.array[i][j]==0){//�ҵ�0��λ�ú󣬽������������ƶ�
                    if(i!=0){//�������ϲ�
                        newState.add(stateExchange(state,i,j,0));
                    }
                    if(i!=size-1){//�������²�
                        newState.add(stateExchange(state,i,j,1));
                    }
                    if(j!=0){//���������
                        newState.add(stateExchange(state,i,j,2));
                    }
                    if(j!=size-1){//�������Ҳ�
                        newState.add(stateExchange(state,i,j,3));
                    }
                    return newState;
                }
            }

        }
        return null;
    }
    //directָ�������ķ���0��1��2��3�ֱ��ʾ���������ҽ���

    /**
     * ״̬�ı亯��������һ������ó���ǰ״̬��һ����һ״̬
     * @param state ��ǰ״̬
     * @param i ��ǰ״̬��0��iֵ
     * @param j ��ǰ״̬��0��jֵ
     * @param direct ָ�������ķ���0��1��2��3�ֱ��ʾ���������ҽ���
     * @return ����Ӧ����ó�����һ��״̬
     * @throws Exception
     */
    private Array stateExchange(Array state,int i,int j,int direct) throws Exception {
        Array copy=cloneState(state);//���һ��ԭ״ֵ̬��������ֹ�޸ĵ�ԭ�ȵ�״̬
        int buf;
        switch (direct){
            case(0)://��0�����ƶ�
                buf=state.array[i-1][j];
                copy.array[i-1][j]=0;
                copy.array[i][j]=buf;
                break;
            case(1)://��0�����ƶ�
                buf=state.array[i+1][j];
                copy.array[i+1][j]=0;
                copy.array[i][j]=buf;
                break;
            case (2)://��0�����ƶ�
                buf=state.array[i][j-1];
                copy.array[i][j-1]=0;
                copy.array[i][j]=buf;
                break;
            case(3)://��0�����ƶ�
                buf=state.array[i][j+1];
                copy.array[i][j+1]=0;
                copy.array[i][j]=buf;
                break;
            default://�����Ǿͱ���
                throw new Exception("δ֪��direct��");
        }
        return copy;
    }

    /**
     * �������һ��״ֵ̬������������ԭ״̬�����Ͻ����޸ģ�ͬʱ��Ӱ������ԭ״̬��ֵ
     * @param src ԭ״̬
     * @return �����һ������ԭ״ֵ̬��ͬ��״̬
     */
    private Array cloneState(Array src){
        Array dst=new Array(new int[src.array.length][src.array[0].length]);
        for(int i=0;i<size;i++){
            //�Զ��������ڵ�ÿһ������������
            System.arraycopy(src.array[i],0,dst.array[i],0,src.array[i].length);
        }
        return dst;
    }

    /**
     * ѡ����չ��״̬
     * @param OPEN ѡȡ��չ��open��
     * @param CLOSE �Ѿ���ѡ����close��
     * @return һ����f�����±�����ѵ�״̬
     */
    public Array chooseToExpand(Map<Array,Meta> OPEN,Map<Array,Meta> CLOSE){
        int min=Integer.MAX_VALUE;
        Meta chooseMeta=null;
        Array choose=null;
        Iterator<Array> it=OPEN.keySet().iterator();
        //����OPEN���ĵ�����������OPEN����ѡ��f����g+h��С��һ��״̬
        while (it.hasNext()){
            Array elem=it.next();
            Meta meta=OPEN.get(elem);
            if(min>(meta.distanceG+meta.distanceH)){
                min=meta.distanceG+meta.distanceH;
                chooseMeta=OPEN.get(elem);
                choose=elem;
            }
        }
        //����Ӧ״̬�Ƴ�OPEN��������CLOSE��
        OPEN.remove(choose);
        CLOSE.put(choose,chooseMeta);
        return choose;
    }

    /**
     * ��ӡ��Ӧ��һ��״̬
     * @param state ��Ӧ״ֵ̬
     */
    void printState(Array state){
        for(int i=0;i<size;i++){
            for(int j=0;j<size;j++){//�������д�ӡ
                if(state.array[i][j]!=0){
                    System.out.print(state.array[i][j]+" ");
                }else {//��0�ÿո����
                    System.out.print("  ");
                }
            }
            System.out.println();
        }
    }

    /**
     * �����㷨
     * @throws Exception
     */
    public void search() throws Exception {
        //���ڴ洢������ÿ��״̬�Լ������״̬��·����Meta���ڱ��浽���״̬��·���ĸ��ڵ�
        Map<Array,Meta> OPEN=new HashMap<>();
        Map<Array,Meta> CLOSE=new HashMap<>();
        //����ʼ���Ϸ���OPEN��
        OPEN.put(puzzleStart,new Meta(0,distanceH(puzzleStart),null));
        //���OPEN���ϲ�Ϊ�գ�˵��������չ
        while(!OPEN.isEmpty()){
            //��OPEN����ѡһ��������չ
            Array choose=chooseToExpand(OPEN,CLOSE);
          //�õ���չ���״̬����
            Set<Array> expandedStates=expand(choose);
            //gΪĿǰΪֹ�����״̬��������չ����
            int distanceG=CLOSE.get(choose).distanceG+1;
            //�����չ������״̬����·�����棬ֱ���˳�ѭ��
            if(expandedStates.contains(puzzleEnd)){
                Meta meta=new Meta(distanceG,0,choose);
                CLOSE.put(puzzleEnd,meta);
                break;
            }
            //���û����չ������״̬���������չ����ÿ��״̬���鿴�Ƿ���OPEN����CLOSE���д���
            for(Array expState:expandedStates){
                if(OPEN.containsKey(expState)){
                    //�����OPEN���д��ڣ���g���ڱ��ε�״̬����ôʹ�ô�״̬�����滻
                    Meta meta=OPEN.get(expState);
                    if(meta.distanceG>distanceG){
                        meta.distanceG=distanceG;
                        meta.fatherNode=choose;
                    }
                }
                //�����CLOSE���д��ڣ���gҲ���ڱ��ε�״̬����ô��״̬���·Ż�OPEN��
                if(CLOSE.containsKey(expState)) {
                    Meta meta=CLOSE.get(expState);
                    if(meta.distanceG>distanceG){
                        meta.distanceG=distanceG;
                        meta.fatherNode=choose;
                        OPEN.put(expState,meta);
                        CLOSE.remove(expState);
                    }
                }
                //�����û�У���ôֱ�ӷ���OPEN����
                if((!OPEN.containsKey(expState))&&(!CLOSE.containsKey(expState))){
                    Meta meta=new Meta(distanceG,distanceH(expState),choose);
                    OPEN.put(expState,meta);
                }
            }
        }
        //���OPEN������Ҳû���ҵ�������û�ж�Ӧ��·��ͨ���յ�״̬
        if(OPEN.isEmpty()){
            System.out.println("û���ҵ����ʵĲ���ʵ��ת�䣡");
        }else{
            //���֪���ˣ���·�����յ㵽����˳���ӡ����
            System.out.println("���յ�״̬׷�ݵ���ʼ״̬��");
            Array state=puzzleEnd;
            int step=1;
            while(state!=null){
                System.out.println("������"+step+"����");
                printState(state);
                state=CLOSE.get(state).fatherNode;
                step++;
            }
            System.out.println("׷�ݽ�����");
        }
    }


    public static void main(String[] args) throws Exception {
        int[][] start=new int[][]{{5,1,2,3},{6,10,4,0},{13,9,7,8},{14,15,11,12}};
        int[][] end=new int[][]{{1,2,3,4},{5,6,7,8},{9,10,11,12},{13,14,15,0}};
        AStar aStar=new AStar(new Array(start),new Array(end),4);
        aStar.search();
    }
}
//���ڷ�װ״̬��һ���࣬�����˴�״̬���Ӧ��һ��״̬����ϵ�������ڵ�
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
//״̬��
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

