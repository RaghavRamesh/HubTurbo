package util;

import java.util.HashMap;
import java.util.List;

import org.eclipse.egit.github.core.IRepositoryIdProvider;

public class SessionConfigurations {
	private HashMap<String, List<String>> projectFilters;
	
	SessionConfigurations() {
		this.projectFilters = new HashMap<String, List<String>>();
	}
	
	SessionConfigurations(HashMap<String, List<String>> projectFilters) {
		this.projectFilters = projectFilters;
	}
	
	public void setFiltersForNextSession(IRepositoryIdProvider project, List<String> filter) {
		projectFilters.put(project.generateId(), filter);
	}
	
	public List<String> getFiltersFromPreviousSession(IRepositoryIdProvider project) {
		System.out.println(project);
		return projectFilters.get(project.generateId());
	}

}