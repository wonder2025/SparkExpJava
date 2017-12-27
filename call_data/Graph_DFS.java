import java.util.Arrays;
import java.util.Random;

public class Graph_DFS {
    //建立一个标识数组，0表示未被发现的节点，1表示已被发现的节点，2表示邻接表检索完后的节点
    private static int[] color;
    //记录连通图的个数
    private static int count = 0;

    //遍历方法
    public void DFS_visit(int[][] array, int n) {
        //节点n已查找
        color[n] = 1;
        System.out.println(Arrays.toString(color));
        //从n出发查找与n相连的节点
        for (int i = n; i < array.length; i++) {
            for (int j = 0; j < array.length; j++) {
                if (array[n][j] == 1) {
                    if (color[j] == 0) {
                        DFS_visit(array, j);
                    }
                }
            }
        }
        color[n] = 2;
        System.out.println(Arrays.toString(color));
        //以上两次打印color数组是为了显示遍历过程，当某一次打印的数组只有2或0时，表示这里有一个已经查找完毕的连通图
    }

    public Graph_DFS(int[][] graph) {
        //初始化color数组，表示该无向图的所有节点都没有查找过
        for (int i = 0; i < graph.length; i++) {
            color[i] = 0;
        }
        //图的遍历
        for (int j = 0; j < graph.length; j++) {
            if (color[j] == 0) {
                //每次执行以下2行代码，表示多出一个连通图
                count++;
                DFS_visit(graph, j);
            }
        }
    }

    //创建一个随机的2维数组
    private static int[][] createRandomBArray() {
        Random ra = new Random();
        int n = ra.nextInt(5) + 4;
        int[][] Barray = new int[n][n];
        for (int k = 0; k < n; k++) {
            Barray[k][k] = 0;
        }
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    if (j > i) {
                        if (Math.random() < 0.5)
                            Barray[i][j] = 1;
                        else Barray[i][j] = 0;
                    } else Barray[i][j] = Barray[j][i];
                }
            }
        }
        return Barray;
    }

    //打印二维数组
    public static void print(int[][] c) {
        for (int i = 0; i < c.length; i++) {
            for (int j = 0; j < c.length; j++) {
                if (c[i][j] == 0)
                    System.out.print(c[i][j] + "\t");
                else
                    System.out.print(c[i][j] + "\t");
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        //创建一个1,2,3和4,5,6的环
        int[][] map1 = new int[][]{
                {0, 1, 0},
                {1, 0, 0},
                {0, 0, 0},
        };
        int[][] map = new int[][]{
                {0, 1, 0, 0, 0, 0},
                {1, 0, 0, 0, 0, 0},
                {0, 0, 0, 1, 0, 0},
                {0, 0, 1, 0, 0, 0},
                {0, 0, 0, 0, 0, 1},
                {0, 0, 0, 0, 1, 0}};
        //创建随机数组
        int[][] map2 = createRandomBArray();
        //打印
        print(map2);
        System.out.println("随机创建的2维数组中，行号和列号表示2个节点，它们对应的数据表示它们的连接情况，0表示这两个节点未连通，1表示这两个节点连通");
        //0表示未被发现的节点，1表示已被发现的节点，2表示邻接表检索完后的节点
        color = new int[map2.length];
        System.out.println("无向图的深度优先搜索:");
        new Graph_DFS(map2);
        System.out.println("连通图个数为:" + count);
    }

}

