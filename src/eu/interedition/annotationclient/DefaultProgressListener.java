package eu.interedition.annotationclient;

import com.vaadin.ui.ProgressIndicator;

import de.catma.backgroundservice.ProgressListener;

public class DefaultProgressListener implements ProgressListener {

	private ProgressIndicator pi;
	private Object lock;
	
	public DefaultProgressListener(ProgressIndicator pi, Object lock) {
		this.pi = pi;
		this.lock = lock;
	}
	
	public void finish() {
	}

	public void start(int jobSize, String jobName, Object... args) {
	}

	public void update(int alreadyDoneSize) {
		// TODO Auto-generated method stub

	}

	public void setIndeterminate(boolean indeterminate, String jobName,
			Object... args) {
		synchronized (lock) {
			pi.setCaption(jobName);
		}
	}

}
