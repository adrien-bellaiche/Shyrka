package node;

public class ListNode<K> {

	private K obj;
	private ListNode<K> next;
	private ListNode<K> prev;
	
	public ListNode(K obj, ListNode<K> prev){
		this.prev=prev;
		this.obj=obj;
		this.next=null;
	}
	public K getObj() {
		return obj;
	}
	public void setObj(K obj) {
		this.obj = obj;
	}
	public ListNode<K> getNext() {
		return next;
	}
	public void setNext(ListNode<K> next) {
		this.next = next;
	}
	public ListNode<K> getPrev() {
		return prev;
	}
	public void setPrev(ListNode<K> prev) {
		this.prev = prev;
	}
	
}
