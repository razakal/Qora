package api;

import java.net.UnknownHostException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import controller.Controller;
import database.DBSet;
import database.PeerMap.PeerInfo;
import network.Peer;
import network.PeerManager;
import ntp.NTP;
import utils.DateTimeFormat;

@Path("peers")
@Produces(MediaType.APPLICATION_JSON)
public class PeersResource 
{
	@SuppressWarnings("unchecked")
	@GET
	public String getPeers()
	{
		List<Peer> peers = Controller.getInstance().getActivePeers();
		JSONArray array = new JSONArray();
		
		for(Peer peer: peers)
		{
			array.add(peer.getAddress().getHostAddress());
		}
		
		return array.toJSONString();
	}
	
	@SuppressWarnings("unchecked")
	@GET
	@Path("height")
	public String getTest()
	{
		Map<Peer,Integer> peers = Controller.getInstance().getPeerHeights();
		JSONArray array = new JSONArray();
		
		for(Map.Entry<Peer, Integer> peer: peers.entrySet())
		{
			JSONObject o = new JSONObject();
			o.put("peer", peer.getKey().getAddress().getHostAddress());
			o.put("height", peer.getValue());
			array.add(o);
		}
		
		return array.toJSONString();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@GET
	@Path("detail")
	public String getInfo()
	{
		Map<Peer,Integer> peers = Controller.getInstance().getPeerHeights();
		
		Map output = new LinkedHashMap();
	
		for(Map.Entry<Peer, Integer> peer: peers.entrySet())
		{
			JSONObject o = new JSONObject();
			o.put("height", peer.getValue());
			o.put("version", Controller.getInstance().getVersionOfPeer(peer.getKey()).getA());
			o.put("ping", peer.getKey().getPing());
			o.put("onlineTime", (NTP.getTime() - peer.getKey().getConnectionTime())/1000);

			PeerInfo peerInfo = DBSet.getInstance().getPeerMap().getInfo(peer.getKey().getAddress());
			
			o.put("findingTime", DateTimeFormat.timestamptoString(peerInfo.getFindingTime()));
			o.put("findingTimeStamp", peerInfo.getFindingTime());

			if(peerInfo.getWhiteConnectTime()>0) {
				o.put("lastWhite", DateTimeFormat.timestamptoString(peerInfo.getWhiteConnectTime()));
				o.put("lastWhiteTimeStamp", peerInfo.getWhiteConnectTime());

			}
			else{
				o.put("lastWhite", "never");
			}
			if(peerInfo.getGrayConnectTime()>0) {
				o.put("lastGray", DateTimeFormat.timestamptoString(peerInfo.getGrayConnectTime()));
				o.put("lastGrayTimeStamp", peerInfo.getGrayConnectTime());
			}
			else{
				o.put("lastGray", "never");
			}
			o.put("pingCounter", peerInfo.getWhitePingCouner());

			output.put(peer.getKey().getAddress().getHostAddress(), o);
		}
		
		return JSONValue.toJSONString(output);
	}
	
	@SuppressWarnings("unchecked")
	@GET
	@Path("best")
	public String getTopPeers()
	{
		List<Peer> peers = PeerManager.getInstance().getBestPeers();
		JSONArray array = new JSONArray();
		
		for(Peer peer: peers)
		{
			array.add(peer.getAddress().getHostAddress());
		}
		
		return array.toJSONString();
	}
	
	@SuppressWarnings({ "unchecked" })
	@GET
	@Path("known")
	public String getFull() throws UnknownHostException
	{
		List<String> addresses = DBSet.getInstance().getPeerMap().getAllPeersAddresses(-1);
		
		JSONArray array = new JSONArray();

		array.addAll(addresses);
		
		return array.toJSONString();
	}
	
	@DELETE
	@Path("/known")
	public String clearPeers()
	{
		DBSet.getInstance().getPeerMap().reset();
		
		return "OK";
	}
}
