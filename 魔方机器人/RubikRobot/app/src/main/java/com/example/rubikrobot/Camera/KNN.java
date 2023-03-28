package com.example.rubikrobot.Camera;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * KNN又名临近算法
 * 实现步骤：
 * 1. 首先计算出所有的临近距离值
 * 2. 对临近值进行排序
 * 3. 选出临近值最小的K个数
 * 4. 投票选出结果
 */
public class KNN {


    //类别
    private final static String RED = "RED";
    private final static String BLACK = "BLACK";


    public static void main(String[] args) {
        //初始化所有节点坐标
        List<KnnNode> totalType = init();
        //验证此节点属于哪个类别
//        KnnNode knnNode = new KnnNode(4,5,""); //所属类别为：black
        KnnNode knnNode = new KnnNode(3,2,""); //所属类别为：red

        //计算所有节点到目标节点的距离，欧式距离
        totalType = getDistance(totalType,knnNode);

        //计算距离目标节点最近的K个节点
        int k = 3;
        List<KnnNode> kList = getKList(totalType,k);

        //计算提供的节点属于那种类别
        String resultType = getNodeType(kList);
        System.out.println("目标节点所属类别为：" + resultType);

    }

    /**
     * 查找距离目标节点最近的K个节点
     * @param totalType
     * @param k
     * @return
     */
    private static List<KnnNode> getKList(List<KnnNode> totalType, int k) {
        List<KnnNode> kList = new ArrayList<>(k);
        //选出距离目标节点最近的K个节点
        for (int i = 0 ; i < totalType.size() ; i++ ) {
            KnnNode type = totalType.get(i);
            if (i < k){
                kList.add(type);
            }else {
                boolean flag = false;
                //判断当前节点小于K个节点集合中的节点
                for (KnnNode knnNode1 : kList) {
                    if (type.getDistance() < knnNode1.getDistance()){
                        flag = true;
                        break;
                    }
                }
                //替换距离目标节点最远的节点
                if (flag) {
                    int index = 0 ;
                    for (int j = 0; j < k; j++) {
                        if (kList.get(j).getDistance() > type.getDistance()) {
                            index = j ;
                        }
                    }
                    kList.remove(index);
                    kList.add(type);

                    kList.forEach(list -> System.out.println(list.toString()));
                    System.out.println("=========================================");
                }
            }
        }
        return kList;
    }

    /**
     * 计算所有节点到目标节点的距离
     * @param totalType
     * @param knnNode
     * @return
     */
    private static List<KnnNode> getDistance(List<KnnNode> totalType, KnnNode knnNode){
        for (int i = 0 ; i < totalType.size() ; i++ ) {
            KnnNode type = totalType.get(i);
            float distance = distance(type, knnNode);
            type.setDistance(distance);
            System.out.println( i+"类别为：【"+ type.getType() + "】  距离为：【"+distance +"】" );
        }
        return totalType;
    }

    /**
     * 计算目标节点所属类别
     * @param kList
     * @return
     */
    private static String getNodeType(List<KnnNode> kList) {
        //结算距离目标节点最近的K个节点中的，节点最多的类别是什么
        int redNum = 0;
        int blackNum = 0;
        for (KnnNode result : kList) {
            if (RED.equals(result.getType())){
                redNum++;
            }else if (BLACK.equals(result.getType())){
                blackNum++;
            }
        }
        return blackNum > redNum ? BLACK : RED;
    }


    /**
     * 欧式距离计算公式
     * @param source
     * @param target
     * @return
     */
    private static float distance(KnnNode source, KnnNode target) {
        float x = source.getX() - target.getX();
        float y = source.getY() - target.getY();
        float z = x * x + y * y;
        float distance = (float) Math.sqrt(z);
        return distance;
    }

    /**
     * 初始化节点
     * @return
     */
    private static List<KnnNode> init() {
        List<KnnNode> totalType = new ArrayList<>();
        totalType.add(new KnnNode(1,2,RED));
        totalType.add(new KnnNode(2,2,RED));
        totalType.add(new KnnNode(1,3,RED));
        totalType.add(new KnnNode(2,1,RED));
        totalType.add(new KnnNode(2,3,RED));
        totalType.add(new KnnNode(3,5,BLACK));
        totalType.add(new KnnNode(4,6,BLACK));
        totalType.add(new KnnNode(3,4,BLACK));
        totalType.add(new KnnNode(5,4,BLACK));
        totalType.add(new KnnNode(5,3,BLACK));
        return totalType;
    }

}