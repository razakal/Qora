package qora.web;

import java.util.List;

import qora.naming.Name;
import controller.Controller;

public class ProfileHelper {

	private Profile currentProfile = null;

	private static ProfileHelper instance;

	public static synchronized ProfileHelper getInstance() {
		if (instance == null) {
			instance = new ProfileHelper();
		}
		
		return instance;

	}
	
	public ProfileHelper() {
		List<Profile> enabledProfiles = Profile.getEnabledProfiles();
		if(enabledProfiles.size() > 0)
		{
			currentProfile = enabledProfiles.get(0);
		}
	}

	public Profile getActiveProfileOpt(){
		if (currentProfile != null) {
			Name name = currentProfile.getName();
			// RELOADING CURRENT VALUES
			Profile profile = Profile.getProfileOpt(name);
			// PROFILE STILL ENABLED AND DO I OWN IT?
			if (profile != null && profile.isProfileEnabled()
					&& Controller.getInstance().getName(name.getName()) != null) {
				currentProfile = profile;
			} else {
				currentProfile = null;
			}

		}
		return currentProfile;
	}

	public void switchProfileOpt(String profileString) {
		
		if(profileString != null)
		{
			Name name = Controller.getInstance().getName(profileString);
			if(name != null)
			{
				Profile profile = Profile.getProfileOpt(name);
				if(profile != null && profile.isProfileEnabled())
				{
					currentProfile = profile;
				}
				
			}
		}
		

	}

}
