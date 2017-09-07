package pers.wusatosi.CRC.CRCApi;

import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

class IdentifyBufferMap extends AbstractMap<String, StudentIdentify> implements Map<String, StudentIdentify> {

	private IdentifyBufferMap() {
		Boolean isBeforeSep = YearMonth.now().isBefore(YearMonth.of(Year.now().getValue(), Month.SEPTEMBER));

		//I think this program won't live for 80 years, or at least this map
		final Year schoolYear = (isBeforeSep ? Year.now().minusYears(1) : Year.now()).minusYears(2000); 
		final Year fromYear = schoolYear.minusYears(offsetYear);

		AcceptFrom = (short) fromYear.getValue();
		AcceptTo = (short) fromYear.plusYears(Defualt_Grade_To - Default_Grade_From - 1).getValue();
		
		Nodes = new Node[(AcceptTo - AcceptFrom + 1 ) + 1];
	}
	
	private IdentifyBufferMap(String AcceptFromHeader,String AcceptToHeader) {
		Objects.requireNonNull(AcceptToHeader);
		Objects.requireNonNull(AcceptFromHeader);
		
		if (AcceptFromHeader.length()!=2||AcceptToHeader.length()!=2)
			throw new IllegalArgumentException();
		
		try {
			AcceptFrom = Short.valueOf(AcceptFromHeader);
			AcceptTo = Short.valueOf(AcceptToHeader);
			if (AcceptFrom > AcceptTo) 
				throw new IllegalArgumentException();
		}catch (NumberFormatException e) {
			throw new IllegalArgumentException();
		}
		
		Nodes = new Node[(AcceptTo - AcceptFrom + 1 ) + 1];
	}
	
	private IdentifyBufferMap(short AcceptGradeFrom, short AcceptGradeTo) {
		Boolean isBeforeSep = YearMonth.now().isBefore(YearMonth.of(Year.now().getValue(), Month.SEPTEMBER));

		final Year schoolYear = (isBeforeSep ? Year.now().minusYears(1) : Year.now()).minusYears(2000); 
		final Year fromYear = schoolYear.minusYears(offsetYear);

		AcceptFrom = (short) fromYear.getValue();
		AcceptTo = (short) fromYear.plusYears(AcceptGradeTo - AcceptGradeFrom - 1).getValue();
		
		Nodes = new Node[(AcceptTo - AcceptFrom + 1 ) + 1];
	}

	private static final short Default_Grade_From = 7;
	private static final short Defualt_Grade_To = 12;

	private static final int offsetYear = (2016 - 2008) + (9 - 7) + 1 ;// 2016 is the caluYear, 08 which my ID starts with, i'm 9th grade

	private final short AcceptFrom;
	private final short AcceptTo;

	private class Node extends AbstractMap.SimpleEntry<String, StudentIdentify> {

		/**
		 * 
		 */
		private static final long serialVersionUID = -7315092903155855354L;

		public Node(String key, StudentIdentify value) {
			super(key, value);
		}

		public Node nextEntry;

		@SuppressWarnings("unused") //wut?
		public Node End;

	}

	private final Node[] Nodes;

	private int SizeCount = 0;

	@Override
	public int size() {
		return SizeCount + otherMap.size();
	}

	public StudentIdentify get(String key) {
		if (!loginedUser.StudentIDChecker(key))
			return otherMap.get(key);
		Node node = Nodes[offsetHelper(key)];
		if (node == null)
			return null;
		if (node.getKey().equals(key)) return node.getValue();
		while (node.nextEntry != null) {
			if (node.getKey().equals(key))
				return node.getValue();
			node = node.nextEntry;
		}
		return null;
	}

	
	public StudentIdentify put(final String key, StudentIdentify value){
		if (!loginedUser.StudentIDChecker(key))
			return otherMap.put(key, value);
		int offset;
		Node tab = Nodes[offset = offsetHelper(key)];
		if (tab == null) {
			tab = (Nodes[offset] = new Node(key, value));
			tab.End = tab;
		} else {
			final Node StTab = tab;
			while (tab.nextEntry != null) {
				if (tab.getKey().equals(key))
					throw new IllegalArgumentException("Key already exists");
				tab = tab.nextEntry;
			}
			tab.nextEntry = new Node(key, value);
			StTab.End = tab.nextEntry;
		}
		SizeCount++;
		return null;
	}

	public StudentIdentify remove(String key) {
		if (!loginedUser.StudentIDChecker(key)) return otherMap.remove(key);
		try {
			int offset;
			Node tab = Nodes[offset = offsetHelper(key)];
			if (tab == null)
				return null;
			Node last;
			if (tab.getKey().equals(key)) {
				StudentIdentify info = tab.getValue();
				if (tab.nextEntry == null) {
					Nodes[offset] = null;
				}else {
					Nodes[offset]=tab.nextEntry;
				}
				return info;
			}
			if (tab.nextEntry == null)
				return null;
			last = tab;
			tab = tab.nextEntry;
			while (tab.nextEntry != null) {
				if (tab.getKey().equals(key)) {
					StudentIdentify info = tab.getValue();
					last.nextEntry = tab.nextEntry;
					return info;
				}
				last = last.nextEntry;
				tab = tab.nextEntry;
			}
		} finally {
			SizeCount++;
		}
		return null;
	}

	private int offsetHelper(String key) {
		short header = Short.valueOf(key.substring(0, 2));
		if (header < AcceptFrom || header > AcceptTo) {
			return 0;
		}
		return header - AcceptFrom + 1;
	}

	@Override
	public void clear() {
		for (int i = 0; i < Nodes.length; i++) {
			Nodes[i] = null;
		}
		SizeCount = 0;
		otherMap.clear();
	}

	@Override
	public Set<Entry<String, StudentIdentify>> entrySet() {
		Set<Entry<String, StudentIdentify>> info = new HashSet<>(SizeCount);
		for (Node tab : Nodes) {
			if (tab == null)
				continue;
			while (tab.nextEntry != null) {
				info.add(tab);
				tab = tab.nextEntry;
			}
		}
		info.addAll(otherMap.entrySet());
		return info;
	}
	
	private Map<String,StudentIdentify> otherMap = new HashMap<>();
	
	private static final IdentifyBufferMap Instance = new IdentifyBufferMap();
	
	public static IdentifyBufferMap getInstance() {
		return Instance;
	}

}
