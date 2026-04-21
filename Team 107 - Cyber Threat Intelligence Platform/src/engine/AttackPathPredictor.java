package engine;

import java.util.*;

public class AttackPathPredictor {

    static class Edge {
        String target;
        int weight; // Represents time or effort to compromise
        Edge(String t, int w) { target = t; weight = w; }
    }

    private Map<String, List<Edge>> graph = new HashMap<>();

    public void addEdge(String source, String target, int weight) {
        graph.putIfAbsent(source, new ArrayList<>());
        graph.get(source).add(new Edge(target, weight));
        graph.putIfAbsent(target, new ArrayList<>()); // ensure target node exists
    }

    // BFS to find the blast radius (how many nodes can be reached depending on severity)
    public int calculateBlastRadiusPercentage(String startNode, int severity) {
        if (!graph.containsKey(startNode)) return 0;

        int maxDepth = Math.max(1, severity / 30);
        
        Set<String> visited = new HashSet<>();
        Queue<Map.Entry<String, Integer>> queue = new LinkedList<>();

        queue.add(new AbstractMap.SimpleEntry<>(startNode, 0));
        visited.add(startNode);

        while (!queue.isEmpty()) {
            Map.Entry<String, Integer> current = queue.poll();
            String currNode = current.getKey();
            int currentDepth = current.getValue();

            // Limit expansion depth based on severity threshold
            if (currentDepth >= maxDepth) continue;

            for (Edge edge : graph.getOrDefault(currNode, new ArrayList<>())) {
                if (!visited.contains(edge.target)) {
                    visited.add(edge.target);
                    queue.add(new AbstractMap.SimpleEntry<>(edge.target, currentDepth + 1));
                }
            }
        }
        
        int totalNodes = graph.size();
        return (int) (((double) visited.size() / totalNodes) * 100);
    }

    // Dijkstra to calculate shortest time to compromise a target database
    public int timeToCompromise(String startNode, String targetNode) {
        if (!graph.containsKey(startNode)) return -1;

        Map<String, Integer> distances = new HashMap<>();
        PriorityQueue<Edge> pq = new PriorityQueue<>(Comparator.comparingInt(e -> e.weight));

        for (String node : graph.keySet()) {
            distances.put(node, Integer.MAX_VALUE);
        }
        distances.put(startNode, 0);
        pq.add(new Edge(startNode, 0));

        while (!pq.isEmpty()) {
            Edge curr = pq.poll();
            if (curr.target.equals(targetNode)) return distances.get(targetNode);

            if (curr.weight > distances.get(curr.target)) continue;

            for (Edge neighbor : graph.getOrDefault(curr.target, new ArrayList<>())) {
                int newDist = distances.get(curr.target) + neighbor.weight;
                if (newDist < distances.get(neighbor.target)) {
                    distances.put(neighbor.target, newDist);
                    pq.add(new Edge(neighbor.target, newDist));
                }
            }
        }
        return -1; // Unreachable
    }

    // Returns the exact step-by-step path (e.g. Web_Server -> App_Server -> Database)
    public List<String> getShortestPath(String startNode, String targetNode) {
        if (!graph.containsKey(startNode)) return new ArrayList<>();

        Map<String, Integer> distances = new HashMap<>();
        Map<String, String> previous = new HashMap<>();
        PriorityQueue<Edge> pq = new PriorityQueue<>(Comparator.comparingInt(e -> e.weight));

        for (String node : graph.keySet()) {
            distances.put(node, Integer.MAX_VALUE);
        }
        distances.put(startNode, 0);
        pq.add(new Edge(startNode, 0));

        while (!pq.isEmpty()) {
            Edge curr = pq.poll();
            if (curr.target.equals(targetNode)) break;

            if (curr.weight > distances.get(curr.target)) continue;

            for (Edge neighbor : graph.getOrDefault(curr.target, new ArrayList<>())) {
                int newDist = distances.get(curr.target) + neighbor.weight;
                if (newDist < distances.get(neighbor.target)) {
                    distances.put(neighbor.target, newDist);
                    previous.put(neighbor.target, curr.target);
                    pq.add(new Edge(neighbor.target, newDist));
                }
            }
        }

        List<String> path = new ArrayList<>();
        String current = targetNode;
        if (previous.containsKey(current) || current.equals(startNode)) {
            while (current != null) {
                path.add(current);
                current = previous.get(current);
            }
            Collections.reverse(path);
        }
        return path;
    }
}
