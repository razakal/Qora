package api;

import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import network.Peer;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import controller.Controller;

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
}
