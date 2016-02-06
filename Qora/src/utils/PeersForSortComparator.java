package utils;

import java.util.Comparator;

import database.PeerMap.PeersForSort;

public class PeersForSortComparator implements Comparator<PeersForSort> {

	@Override
	public int compare(PeersForSort o1, PeersForSort o2) {
		
		if(o1.getWhiteConnectTime() < o2.getWhiteConnectTime())
			return -1;
		else if(o1.getWhiteConnectTime() > o2.getWhiteConnectTime())
			return 1;
		else
		{
			if(o1.getPingCouner() < o2.getPingCouner())
				return -1;
			else if(o1.getPingCouner() > o2.getPingCouner())
				return 1;
			else
				return 0;
		}
	}

}
