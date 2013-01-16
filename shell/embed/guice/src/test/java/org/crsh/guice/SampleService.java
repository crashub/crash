package org.crsh.guice;

import java.util.HashMap;
import java.util.Map;

import com.google.inject.Singleton;

@Singleton
public class SampleService {

	private Map<String, Integer> counter;
	
	public SampleService() {
		counter = new HashMap<String, Integer>();
	}
	
	public void jump(String howHigh) {
		Integer previous = counter.get(howHigh);
		if (previous == null) {
			counter.put(howHigh, 1);
		} else {
			counter.put(howHigh, previous + 1);
		}
	}
	
	public void jump5() {
		jump("5");
	}
	
	@Override
	public String toString() {
		return "SampleService";
	}
	
}
