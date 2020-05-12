package main;
import java.util.HashMap;

public class DependencyGraphManager {

	protected String itemName;
	protected Item item;
	protected HashMap<String, Integer> itemDependencyCountMap;

	public DependencyGraphManager(Item item) {
		this.item = item;
		this.itemName = null;
		this.itemDependencyCountMap = new HashMap<String, Integer>();

	}

	public boolean isAnyReference() {
		return itemDependencyCountMap.entrySet()
		    .stream()
		    .anyMatch(entry -> (entry.getValue()>0));
	}

	public int totalReferenceCount() {
		int referenceCcount = 0;
		for (String src : itemDependencyCountMap.keySet()) {
			referenceCcount += itemDependencyCountMap.get(src);
		}
		return referenceCcount;
	}
	
	public int totalNonReferenceCount(String str) {
		int nonReferenceCcount = 0;
		for (String src : itemDependencyCountMap.keySet()) {
			if (!str.equals(src)) {
				nonReferenceCcount += itemDependencyCountMap.get(src);
			}
		}
		return nonReferenceCcount;
	}

	public boolean hasReferenceFromSource(String source) {
		if (itemDependencyCountMap.containsKey(source) && itemDependencyCountMap.get(source) > 0) {
			return true;
		}
		return false;
	}

	public int addReferenceFromSource(String source) {
		Integer refCount = new Integer(0);
		if (itemDependencyCountMap.containsKey(source)) {
			refCount = itemDependencyCountMap.get(source);
		} 
		refCount++;
		if (refCount >= 0) {
			itemDependencyCountMap.put(source, refCount);
		}

		return refCount;
	}

	public int removeReferenceFromSource(String Source) {
		Integer refCount;
		if (hasReferenceFromSource(Source) == false) {
			return 0;
		}
		refCount = itemDependencyCountMap.get(Source);
		refCount--;

		itemDependencyCountMap.put(Source, refCount);
		return refCount;
	}
};