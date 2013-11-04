package cache;

/**
 * 
 * @author www.itguphup.com
 * This is a wrapper class ove cacher data and internally a doubly link list.
 * @param <K> - key
 * @param <V> - value
 */
public class Node<K,V> {

	public Node<K,V> prev;
	public Node<K,V> next;
	public K key;
	public V data;
	public long expireTime; 

	public Node() {}

	public Node(K key, V data, long expireTime) {
		this.key = key;
		this.data = data;
		this.expireTime = expireTime;
	}
}