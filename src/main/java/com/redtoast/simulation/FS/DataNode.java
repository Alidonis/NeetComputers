package com.redtoast.simulation.FS;

import com.redtoast.neet.NeetComputersServer;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public record DataNode(boolean isDirectory, String name, String path, DataNode[] nodes) {
    private static final Logger logger = LoggerFactory.getLogger("Neetcomputers-datascanning");
    public static Map<Integer, DataNode[]> nodeTree = null;

    public String[] getChildren() {
        String[] children = new String[nodes.length];
        for (int i = 0; i < nodes.length; i++) children[i] = nodes[i].name();
        return children;
    }

    public String[] getChildren(List<String> blacklist) {
        if (blacklist.isEmpty()) return getChildren();
        List<String> children = new LinkedList<>();
        for (DataNode node : nodes) if (!blacklist.contains(node.name())) children.add(node.name());
        return children.toArray(new String[] {});
    }

    public static @Nullable DataNode getNode(int address, String path) {
        assert nodeTree != null;
        if (!nodeTree.containsKey(address)) return null;
        if (path.isEmpty()) {
            return new DataNode(true, "", "/", nodeTree.get(address));
        }
        DataNode[] space = nodeTree.get(address);
        String[] parts = path.replaceFirst("[/\\\\]$","").split("[/\\\\]");
        boolean startingSlash = path.charAt(0) == '/' || path.charAt(0) == '\\';
        for (int i = startingSlash ? 1 : 0; i < parts.length; i++) {
            DataNode found = null;
            for (DataNode node : space) {
                if (node.name().equals(parts[i])) {
                    found = node;
                    break;
                }
            }
            if (found == null) return null;
            if (i == parts.length-1) return found;
            space = found.nodes();
        }
        return null;
    }

    private record DataConstructorNode(String name, List<DataConstructorNode> nodes){
        DataNode process(String prefix){
            String path = prefix + name;
            DataNode[] subNodes = new DataNode[nodes.size()];
            for (int i = 0; i < subNodes.length; i++) {
                subNodes[i] = nodes.get(i).process(path+'/');
            }
            return new DataNode(!nodes.isEmpty(), name, path, subNodes);
        }
    }

    private static DataConstructorNode spaceGet(List<DataConstructorNode> space, String index) {
        for (DataConstructorNode node : space) if (node.name().equals(index)) return node;
        space.add(new DataConstructorNode(index, new LinkedList<>()));
        return space.getLast();
    }

    public static void loadData(){
        Hashtable<String, List<DataConstructorNode>> table = new Hashtable<>();
        NeetComputersServer.datahandling.findResources("neet/hard_addresses", arg -> {
            String[] parts = arg.toString().split("/");
            if (!table.containsKey(parts[2])) table.put(parts[2], new ArrayList<>());
            List<DataConstructorNode> space = table.get(parts[2]);
            for (int i = 3; i < parts.length; i++){
                space = spaceGet(space, parts[i]).nodes();
            }
            return true;
        });
        Hashtable<Integer, DataNode[]> product = new Hashtable<>();
        table.forEach((key, nodes) -> {
            int id;
            try {
                id = -Integer.parseInt(key);
                if (id>=0) {
                    if (NeetComputersServer.DO_LOGGING) logger.warn("Invalid id number '{}'", key);
                    return;
                }
            }catch (NumberFormatException ignored) {
                if (NeetComputersServer.DO_LOGGING) logger.warn("Non-address '{}' found in address folder", key);
                return;
            }
            DataNode[] subNodes = new DataNode[nodes.size()];
            for (int i = 0; i < subNodes.length; i++) {
                subNodes[i] = nodes.get(i).process("");
            }
            product.put(id, subNodes);
        });
        nodeTree = Map.copyOf(product);
    }
}
