package node;

public class List <K>{

	/**
	 ** Author : A. Bellaiche
	 ** This list is 0-indexed
	 ** fully functionnal so far;
	 **/

	private ListNode<K> tamp;
	private ListNode<K> last;
	private ListNode<K> first;
	private int size;

	public List() {
		last=null;
		first=null;
		size=0;
	}

	public ListNode<K> getLast() {
		return this.last;
	}

	public int length() {
		return size;
	}

	public void add(K obj) {
		size++;
		if(this.first==null) {
			this.tamp=new ListNode<K>(obj,null);
			this.first=this.tamp;
		}
		else {
			this.tamp=new ListNode<K>(obj,this.last);
			this.last.setNext(this.tamp);
		}
		this.last=this.tamp;
		this.tamp=null;

	}

	public int placeOf(K obj) {
		int res=0;
		boolean found=false;
		for(this.tamp=this.first;tamp!=null||!found;tamp=tamp.getNext()){
			res++;
			if(tamp.getObj().equals(obj)) {
				found=true;
			}
		}
		return res;
	}

	public boolean isThere(Object obj) {
		boolean res=false;
		for(this.tamp=this.first;tamp!=null && !res;tamp=tamp.getNext()){
			if(this.tamp.getObj().equals(obj)) {
				res=true;
			}
		}
		return res;
	}


	public Object seeLast() {
		return last.getObj();
	}

	public void insert(K obj, int place) { //insert in the List.
		ListNode<K> temp=this.first;
		if(place<=1) {
			this.addFirst(obj);
		} else if(!this.isEmpty() && place<=this.size) { //if the list is empty or if we should add it "after the last object of the list", it's only an "add".
			int current;
			for(current=2;current<=place && temp.getNext()!=null;current++) {//get to place
				temp=temp.getNext();
			}
			ListNode<K> in=new ListNode<K>(obj,temp);
			if(temp.getNext()!=null) { //if it is not the last place
				ListNode<K> prev=temp.getPrev();
				prev.setNext(in);
				in.setPrev(prev);
				in.setNext(temp);
				temp.setPrev(in);
				this.size++;
			} else {
				ListNode<K> prev=this.last.getPrev();
				prev.setNext(in);
				in.setPrev(prev);
				in.setNext(this.last);
				this.last.setPrev(in);
				this.size++;
			}
		}
		else { //well, just add it.
			this.add(obj);
		}
	}

	private void addFirst(K obj) {
		if(!this.isEmpty()) {
			ListNode<K> temp= new ListNode<K>(obj, null);
			temp.setNext(this.first);
			this.first.setPrev(temp);
			this.first=temp;
			this.size++;
		} else {
			this.add(obj);
		}
	}

	public ListNode<K> getFirst() {
		return first;
	}

	public int getSize() {
		return size;
	}

	public boolean isEmpty() {
		return this.size==0;
	}

	public boolean isFirst(K obj) {
		return !(this.size == 0) && obj.equals(this.first.getObj());
	}

	public boolean isLast(K obj) {
		return !this.isEmpty() && obj.equals(this.last.getObj());
	}

	public void removeObj(K obj) { //removes from the list the first occurence of obj.
		boolean found=false;
		tamp=this.first;
		while(tamp!=null && !found) {
			if(tamp.getObj().equals(obj)) {
				found=true;
				if(tamp.equals(this.first)) {//Cas premier
					if(tamp.equals(this.last)) {// cas premier et dernier
						this.first=null;
						this.last=null;
						this.size=0;
					}
					else {// seulement premier
						this.first=this.first.getNext();
						this.first.setPrev(null);
						this.size-=1;
					}
				}
				else if(tamp.equals(this.last)) {//Cas dernier seulement
					this.last.getPrev().setNext(null);
					this.last=this.last.getPrev();
					this.size-=1;
				}
				else { //Cas normal
					tamp.getNext().setPrev(tamp.getPrev());
					tamp.getPrev().setNext(tamp.getNext());
					this.size-=1;
				}
			}
			tamp=tamp.getNext();
		}
	}

	public K takeLast(){
		K obj = this.getLast().getObj();
		this.removeLast();
		return obj;
	}

	public K takeFirst(){
		K obj = this.getFirst().getObj();
		this.removeObj(obj);
		return obj;
	}

	public void removeLast() {
		if(this.last!=null) {
			if(!this.last.equals(this.first)) {
				this.last=this.last.getPrev();
				this.last.getNext().setObj(null);
				this.last.getNext().setPrev(null);
				this.last.setNext(null);
				this.size--;
			}
			else {
				this.first=null;
				this.size--;
			}
		}
	}

	public void removeAll() {
		for(ListNode<K> tamp=this.first; tamp!=null;tamp=tamp.getNext())  {
			tamp.setPrev(null);
		}
		for(ListNode<K> tamp=this.last; tamp!=null ; tamp=tamp.getPrev())  {
			tamp.setNext(null);
		}
		this.first=null;
		this.last=null;
		this.size=0;
	}

	public Object[] toTab() {
		Object obj[]=new Object[this.size-1];
		this.tamp=this.first;
		for(int k=0; tamp.getNext()!=null; k++) {
			obj[k]=this.tamp.getObj();
			this.tamp=this.tamp.getNext();
		}
		return obj;
	}

	public void affiche(Object tab[]){
		for (Object aTab : tab) {
			System.out.print(aTab + " ");
		}
		System.out.print("\n");
	}

	public static void main(String[] args) {
		List<String> list=new List<String>();
		System.out.println("List Created");
		String test1= "test1";
		list.add(test1);
		list.add(test1);
		list.add(test1);
		System.out.println("3 Object added, type string");
		System.out.println("Must Be 3 " + list.length());
		String test2="str2";
		list.add(test2);
		System.out.println("Second Object Created : type int");
		System.out.println("Must Be 4 " + list.length());
		System.out.println("Test2 is in the list " + list.isThere(test2));
		System.out.println("Must be 4 " + list.placeOf(test2));
		int p=3;

		list.affiche(list.toTab());
		list.insert(test2,p);
		System.out.println(test2 + " inserted in place" + p);
		list.affiche(list.toTab());
		list.removeObj(test1);
		System.out.println("Remove one obj");
		System.out.println("Must be str2 " + list.seeLast());
		list.affiche(list.toTab());
		list.removeAll();
		System.out.println("Must be 0 " + list.length());
		list.removeLast();
		System.out.println("Must be 0 " + list.length());

	}

}
