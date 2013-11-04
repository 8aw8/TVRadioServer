package cache;

public interface DAOInterface<K,V> {

	Node<K, V> getData(Object key);
	void insertData(Node<K,V> node);
}
