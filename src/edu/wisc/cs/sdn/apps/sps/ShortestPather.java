package edu.wisc.cs.sdn.apps.sps;

import java.util.*;

import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.routing.Link;

/**
 * this class uses bellman-ford for shortest path in directed graph,
 * which is implemented under guide of
 *      - https://algs4.cs.princeton.edu/44sp/
 */
public class ShortestPather {

    private static final double WEIGHT = 1.0;
    private static final double EPSILON = 1E-14;

    private Map<Long, Double> distTo;
    private Map<Long, Link> edgeTo;
    private Set<Long> onQueue;
    private Queue<Long> queue;
    private Map<Long, ArrayList<Link>> network;   // topological structure of sdn

    public ShortestPather() {
        distTo = new HashMap<>();
        edgeTo = new HashMap<>();
        onQueue = new HashSet<>();
        queue = new ArrayDeque<>();
        network = new HashMap<>();
    }

    public List<Link> getShortestPathToDstHost(long dstHostSwitchId) {
        LinkedList<Link> path = new LinkedList<>();
        while (edgeTo.containsKey(dstHostSwitchId)) {
            Link link = edgeTo.get(dstHostSwitchId);
            path.addFirst(link);
            dstHostSwitchId = link.getSrc();
        }

        return path;
    }

    public void runBellmanFord(IOFSwitch sourceSwitch, Collection<Link> links, Map<Long, IOFSwitch> switches) {
        buildNetworkStructure(links);

        for (long sId: switches.keySet()) {
            distTo.put(sId, Double.POSITIVE_INFINITY);
        }
        distTo.put(sourceSwitch.getId(), 0.0);

        while (!queue.isEmpty()) {
            long currSID = queue.poll();
            onQueue.remove(currSID);
            relax(network, currSID);
        }

    }

    private void relax(Map<Long, ArrayList<Link>> network, long fromSID) {
        for (Link link: network.get(fromSID)) {
            long toSID = link.getDst();
            if (distTo.get(toSID) > distTo.get(fromSID) + WEIGHT + EPSILON) {
                distTo.put(toSID, distTo.get(fromSID) + WEIGHT);
                edgeTo.put(toSID, link);

                if (!onQueue.contains(toSID)) {
                    onQueue.add(toSID);
                    queue.offer(toSID);
                }
            }
        }
    }

    private void buildNetworkStructure(Collection<Link> links) {
        for (Link link: links) {
            network.computeIfAbsent(link.getSrc(), (k -> new ArrayList<>())).add(link);
        }
    }
}
