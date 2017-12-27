import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.KosarajuStrongConnectivityInspector;
import org.jgrapht.alg.interfaces.StrongConnectivityAlgorithm;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Component
{
    private DirectedGraph<String, DefaultEdge> directedGraph;

    public Component(List<String> list, Map<String, String> map){
        directedGraph = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
        for (String v:list){
            directedGraph.addVertex(v);

        }
        for (Map.Entry<String, String> vo : map.entrySet()){
            if(list.contains(vo.getValue())){
                directedGraph.addEdge(vo.getKey(), vo.getValue());

            }
        }
    }

    public void testStrongConn(){
        StrongConnectivityAlgorithm<String, DefaultEdge> scAlg =
                new KosarajuStrongConnectivityInspector<String, DefaultEdge>(directedGraph);
        List<Set<String>> stronglyConnetedSet =
                scAlg.stronglyConnectedSets();

        System.out.println("Strongly connected components:");
        for (int i = 0; i < stronglyConnetedSet.size(); i++) {
            System.out.println(stronglyConnetedSet.get(i));
        }
        System.out.println();

    }
    public List<Set<String>> testWeakConn() {
        ConnectivityInspector<String, DefaultEdge> connectivityInspector = new ConnectivityInspector<String, DefaultEdge>(directedGraph);
        List<Set<String>> weaklyConnectedSet = connectivityInspector.connectedSets();
        System.out.println("Weakly connected components:");
        for (int i = 0; i < weaklyConnectedSet.size(); i++) {
            System.out.println(weaklyConnectedSet.get(i));
        }
        return weaklyConnectedSet;
    }

}