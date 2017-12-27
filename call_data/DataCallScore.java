import com.alibaba.fastjson.JSON;

import java.io.*;
import java.util.*;

public class DataCallScore {
    public static Map<String, HashSet> rawMap = new HashMap<String, HashSet>();

    public static List<Set<String>> componentSet=new ArrayList<Set<String>>();

    public static Map<String, Double> score(Map<String, HashSet> map) throws IOException {
        StringBuffer nbr = new StringBuffer();
        StringBuffer score = new StringBuffer();
        StringBuffer match = new StringBuffer();
        int i = 0;
        Map<String, Double> res=new HashMap<String, Double>();
        for (Map.Entry<String, HashSet> vo : map.entrySet()) {

            String key = vo.getKey();
            HashSet<String> value = vo.getValue();
            double x = value.size();
            //剔除一个月只打一次电话的用户（异网用户）
            if(x==1.0){
                i++;
                continue;
            }
            nbr.append(key).append(",");
            double y = 0;
            // hashset
            for (String s : value) {
                HashSet hs = map.get(s);
                if (hs != null) {
                    HashSet hs1 = new HashSet(hs);
                    hs1.retainAll(value);
                    int temp = hs1.size();
                    y = y + temp;

                }
            }
            x = x + 1;
            y = y + 1;

            score.append(x / y).append(",");
            match.append("{" + key + ":" + x / y + "}").append(",");
            res.put(key,x/y);
            i++;

        }
//        System.out.println(nbr);
        String s = nbr.append("||").append(score).append("||").append(match).toString();
        writeFromBuffer("E:\\SparkExp\\call_data\\network_graph\\call_score.txt", s);
        System.out.println("--原始顶点数目（主叫用户数）--" + i);

        return res;
    }

    public static List<String> deleteVertexByScore(Map<String,Double> map,double threshold) throws IOException {
        List<String> deteteList=new ArrayList<String>();
        StringBuffer sb=new StringBuffer();
        for (Map.Entry<String, Double> vo : map.entrySet()) {

            String key = vo.getKey();
            Double value = vo.getValue();
            if (value <=threshold ){
                sb.append(key).append("\n");
                deteteList.add(key);
            }

        }
        String json = JSON.toJSONString(deteteList);

        writeFromBuffer("E:\\SparkExp\\call_data\\network_graph\\call_score_delete.txt", json);
        return deteteList;
    }

    public static void writeFromBuffer(String filePath, String sb) throws IOException {
        File file = new File(filePath);
        FileWriter fw;
        try {
            fw = new FileWriter(file);
            if (sb.toString() != null && !"".equals(sb.toString())) {
                fw.write(sb.toString());
            }
            fw.close();
        } catch (IOException e) {
            throw new IOException("文件写入异常！请检查路径名是否正确!");
        }

    }

    public static Map<String, HashSet> read(String filePath) {
        try {
            File csv = new File(filePath);
            BufferedReader br = new BufferedReader(new FileReader(csv));

            String line = "";
            while ((line = br.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(line, ",");
                while (st.hasMoreTokens()) {
                    String first = st.nextToken();
                    String sencond = st.nextToken();
                    if (rawMap.containsKey(first)) {
                        HashSet hs = rawMap.get(first);
                        hs.add(sencond);
                    } else {
                        HashSet hs = new HashSet();
                        hs.add(sencond);
                        rawMap.put(first, hs);
                    }
                }
            }
            writeFromBuffer("E:\\SparkExp\\call_data\\network_graph\\call_score_raw.txt", rawMap.toString());
            br.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rawMap;
    }
    //查找剩余顶点的边
    public static Map<String, String> generationEdges(List<String> deteteList) throws IOException {
        StringBuffer sb=new StringBuffer();
        Map<String, String> mapSD = new HashMap<String, String>();
        int i=0;
        for(String ll:deteteList){
            System.out.println("-----计算第几个顶点----"+i);
            HashSet<String> hs=rawMap.get(ll);
            for(String s:hs){
                if(deteteList.contains(s)){
                    mapSD.put(ll,s);
                    sb.append("(").append(ll).append(",").append(s).append(")").append("\n");
                }
            }
            i++;
        }
        writeFromBuffer("E:\\SparkExp\\call_data\\network_graph\\call_score_st.txt", sb.toString());
        return mapSD;
    }


    private static List<Set<String>> calculateComponet(List<String> deteteList, Map<String, String> deleteMap) throws IOException {
        Component test = new Component(deteteList,deleteMap);
//        test.testStrongConn();
        List<Set<String>> componentSet=test.testWeakConn();
        int sum =0;
        int ss =0;
        //统计作用
        for(Set set:componentSet){
            sum=sum+set.size();
            if (set.size()>1){
                ss=ss+1;
            }
        }
        writeFromBuffer("E:\\SparkExp\\call_data\\network_graph\\call_score_componet.txt", componentSet.toString());
//        ----顶点数----56114
//        ----componet数目----12601
//        ----component元素大于1的componet数目----4408
        System.out.println("----顶点数----"+sum);
        System.out.println("----componet数目----"+componentSet.size());
        System.out.println("----component元素大于1的componet数目----"+ss);
        //生成连通子图的json 每个分为一个文件 （文件数目多）
        generationComponentJson(componentSet);
        return componentSet;
    }

    private static void generationComponentJson(List<Set<String>> componentSet) throws IOException {
        int index=0;
        for(Set set:componentSet){
            if(set.size()>1){
                Map<String, String> subEdges=generationComponentEdges(set);
                List<String> subNodes=new ArrayList<String>(set);
                generationJson(subNodes,subEdges,"componentSet"+index+".json");

            }
            index++;
        }
    }
    public static Map<String, String> generationComponentEdges(Set<String> set) throws IOException {
        Map<String, String> sd = new HashMap<String, String>();
        StringBuffer sb=new StringBuffer();
        for(String ll:set){
            HashSet<String> hs=rawMap.get(ll);
            for(String s:hs){
                if(set.contains(s)){
                    sd.put(ll,s);

                }
            }

        }
        return sd;
    }


    private static void generationJson(List<String> nodes,Map<String, String> edges,String name) throws IOException{
        Map<String,List<Map<String,Object>>> data=new HashMap<String,List<Map<String,Object>>>();
        List<Map<String,Object>> nodeList=new ArrayList<Map<String,Object>>();
        String color=getRandColorCode();
        int i=0;
        int j=0;
        for(String node:nodes){
            Map<String,Object> map=new HashMap<String,Object>();
            map.put("color","#"+color);
            map.put("id",node);
            map.put("label",node);
            map.put("size",10);
            map.put("x",getRandom(-1000,1000));
            map.put("y",getRandom(-400,400));
            nodeList.add(map);
        }
        data.put("nodes",nodeList);
        List<Map<String,Object>> edgeList=new ArrayList<Map<String,Object>>();
        for (Map.Entry<String, String> vo : edges.entrySet()) {

            String s = vo.getKey();
            String d = vo.getValue();
            Map<String,Object> map=new HashMap<String,Object>();
            map.put("sourceID",s);
            map.put("targetID",d);
            map.put("size",3);
            edgeList.add(map);

        }
        data.put("edges",edgeList);
        String json = JSON.toJSONString(data);

        writeFromBuffer("E:\\SparkExp\\call_data\\network_graph\\"+name, json);
    }
    //生成json 考虑连通子图的位置
    private static void generationJsonWithComponent(List<String> nodes,Map<String, String> edges,List<Set<String>> comp,String name) throws IOException{
        Map<String,String> match=new HashMap<String,String>();
        for(Set<String> set:comp){
            String color=getRandColorCode();
            int offx=getRandom(-1100,1100);
            int offy=getRandom(-500,500);
            for(String s:set){
                match.put(s,"#"+color+","+offx+","+offy);
            }

        }

        Map<String,List<Map<String,Object>>> data=new HashMap<String,List<Map<String,Object>>>();
        List<Map<String,Object>> nodeList=new ArrayList<Map<String,Object>>();
        int i=0;
        int j=0;
        for(String node:nodes){
            String conf=match.get(node);
            String[] confs=conf.split(",");
            Map<String,Object> map=new HashMap<String,Object>();
            map.put("color",confs[0]);
            map.put("id",node);
            map.put("label",node);
            map.put("size",5);
            map.put("x",getRandom(-20,20)+Integer.parseInt(confs[1]));
            map.put("y",getRandom(-20,20)+Integer.parseInt(confs[2]));

            nodeList.add(map);
        }



        data.put("nodes",nodeList);
        List<Map<String,Object>> edgeList=new ArrayList<Map<String,Object>>();
        for (Map.Entry<String, String> vo : edges.entrySet()) {

            String s = vo.getKey();
            String d = vo.getValue();
            Map<String,Object> map=new HashMap<String,Object>();
            map.put("sourceID",s);
            map.put("targetID",d);
            map.put("size",3);
            edgeList.add(map);

        }
        data.put("edges",edgeList);




        String json = JSON.toJSONString(data);
        writeFromBuffer("E:\\SparkExp\\call_data\\network_graph\\"+name, json);

    }

    private static int getRandom(int min,int max){
        Random random = new Random();

        int s = random.nextInt(max)%(max-min+1) + min;
        return s;
    }
    /**
     * 获取十六进制的颜色代码.例如  "#6E36B4"
     * @return String
     */
    public static String getRandColorCode(){
        String r,g,b;
        Random random = new Random();
        r = Integer.toHexString(random.nextInt(256)).toUpperCase();
        g = Integer.toHexString(random.nextInt(256)).toUpperCase();
        b = Integer.toHexString(random.nextInt(256)).toUpperCase();

        r = r.length()==1 ? "0" + r : r ;
        g = g.length()==1 ? "0" + g : g ;
        b = b.length()==1 ? "0" + b : b ;

        return r+g+b;
    }
    public static void main(String[] args) {
        Map<String, HashSet> rowData = read("D:\\深圳培训\\spark\\call_data\\call_data_noheader_all.csv");
        try {
            Map<String,Double> map=score(rowData);
            double threshold=300;
            //删除score高的点，返回剩余顶点数目
            List<String> deteteList=deleteVertexByScore(map,threshold);
            System.out.println("--剩余顶点数目--"+deteteList.size());
            //查找剩余顶点的edges
            Map<String,String> mapSD=generationEdges(deteteList);
            System.out.println("--edge数目--"+mapSD.size());
            //计算剩余顶点的连通子图
            List<Set<String>> comps=calculateComponet(deteteList, mapSD);
            //保存json文件
//            generationJson(deteteList,mapSD,"callScoreJson.json");
            //算了componet 所有在一张图
//            generationJsonWithComponent(deteteList,mapSD,comps,"callScoreJsonComp.json");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
