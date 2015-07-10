package qora.web;

import java.util.Collections;
import java.util.List;

public class BlogProfile implements Comparable<BlogProfile> {

	private final Profile profile;
	private final List<String> follows;

	public BlogProfile(Profile profile, List<String> follower) {
		this.profile = profile;
		follows = follower;
	}

	public int getFollowerCount() {
		return follows.size();
	}

	public Profile getProfile() {
		return profile;
	}

	@Override
	public int compareTo(BlogProfile o) {
		
		if(getFollowerCount() == o.getFollowerCount())
		{
			return 0;
		}else if(getFollowerCount() > o.getFollowerCount())
		{
			return -1;
		}else
		{
			return 1;
		}
	}

	public List<String> getFollower() {
		return Collections.unmodifiableList(follows);
	}


}
