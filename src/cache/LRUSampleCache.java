package cache;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author www.itgupshup.com
 *
 * @param <K> - key
 * @param <V> - value
 */
public class LRUSampleCache<K,V> {
	private Node<K, V> head;
	private Node<K, V> tail;
	private final int maxSize;
	private Map<K, Node<K,V>> map = null;
	private DAOInterface<K, V> daoInterface = new DAOInterfaceImpl();

	public LRUSampleCache(int maxSize) {
		this.maxSize = maxSize;
		//dummy Nodes 
		head = new Node<K, V>(null, null, 0);
		tail = new Node<K, V>(null, null, 0);
		head.next = tail;
		tail.prev = head;
		map = new HashMap<K, Node<K, V>>(maxSize);
	}

	public Object get(Object key) {
		Node<K, V> node = map.get(key);
		if (node == null){
			node = daoInterface.getData(key);
			if(node==null)
				return null;
			
			if(node.expireTime < System.currentTimeMillis())
			{
				map.remove(node.key);
				this.removeNodeFromList(node);
				return null;
			}
			
			addNodeToListAtHead(node);
			return node.data;
		}
		if (map.size() == 1)
			return node.data;
		
		if(node.expireTime < System.currentTimeMillis())
		{
			map.remove(node.key);
			this.removeNodeFromList(node);
			return null;
		}

		removeNodeFromList(node);
		addNodeToListAtHead(node);
		return node.data;
	}
	
	public void put(K key, V data, long expireDate) {
		if (maxSize <= 0)
			return;
		Node<K, V> node = map.get(key);
		if (node != null) {
			removeNodeFromList(node);
			addNodeToListAtHead(node);
			node.data = data;
		} else {
			node = new Node<K, V>(key, data, expireDate);
			map.put(key, node);
			addNodeToListAtHead(node);
			if (map.size() > maxSize) {
				map.remove(tail.prev.key);
				removeNodeFromList(tail.prev);
			}
		}
	}
	
	/** 
	 * Adding element Node next to head 
	 * @param node 
	 */
	private synchronized void addNodeToListAtHead(Node<K, V> node) {
		node.next = head.next;
		node.prev = head;
		head.next.prev = node;
		head.next = node;
	}
	
	/**
	 * remove the node from doubly linked list.  
	 * @param node
	 */
	private synchronized void removeNodeFromList(Node<K, V> node) {
		node.prev.next = node.next;
		node.next.prev = node.prev;
	}
}