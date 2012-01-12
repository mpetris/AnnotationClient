package de.catma.ui.client.ui.common.event;

import java.util.ArrayList;
import java.util.List;

public class EventListenerSupport {

	private List<EventListener> eventListeners;
	
	public EventListenerSupport() {
		eventListeners = new ArrayList<EventListener>();
	}
	
	public void addEventListener(EventListener eventListener) {
		eventListeners.add(eventListener);
	}
	
	public void removeEventListener(EventListener eventListener) {
		eventListeners.remove(eventListener);
	}
	
	public void fireEvent(Object event) {
		for (EventListener listener : eventListeners) {
			listener.eventFired(event);
		}
	}
	
	public void clear() {
		eventListeners.clear();
	}
}
