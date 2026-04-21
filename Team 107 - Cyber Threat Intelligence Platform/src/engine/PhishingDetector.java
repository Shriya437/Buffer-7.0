package engine;

import java.util.HashMap;
import java.util.Map;

class TrieNode {
    Map<Character, TrieNode> children;
    boolean isEndOfDomain;

    public TrieNode() {
        children = new HashMap<>();
        isEndOfDomain = false;
    }
}

public class PhishingDetector {
    private TrieNode root;

    public PhishingDetector() {
        root = new TrieNode();
    }

    // Insert a known bad domain into the Trie
    public void insert(String domain) {
        TrieNode current = root;
        for (char ch : domain.toCharArray()) {
            current.children.putIfAbsent(ch, new TrieNode());
            current = current.children.get(ch);
        }
        current.isEndOfDomain = true;
    }

    // Check if the domain exists or matches a prefix in the Trie
    public boolean isPhishing(String url) {
        // Simple normalization
        String domain = url.replace("http://", "").replace("https://", "").split("/")[0];
        
        TrieNode current = root;
        for (char ch : domain.toCharArray()) {
            if (!current.children.containsKey(ch)) {
                return false;
            }
            current = current.children.get(ch);
        }
        return current.isEndOfDomain;
    }
}
