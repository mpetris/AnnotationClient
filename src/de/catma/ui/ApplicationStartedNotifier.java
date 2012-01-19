package de.catma.ui;

import java.util.Map;

import com.vaadin.ui.AbstractComponent;

import de.catma.ui.client.ui.VApplicationStartedNotifier;

@com.vaadin.ui.ClientWidget(VApplicationStartedNotifier.class)
public class ApplicationStartedNotifier extends AbstractComponent {

	public static interface AppLoadedListener {
		public void appLoaded();
	}

	private AppLoadedListener appLoadedListener;
	
	public ApplicationStartedNotifier(AppLoadedListener appLoadedListener) {
		this.appLoadedListener = appLoadedListener;
	}
	
	@Override
	public void changeVariables(Object source, Map<String, Object> variables) {
		super.changeVariables(source, variables);
		
		if ((variables.containsKey("AppStarted")) 
				&& Boolean.valueOf(variables.get("AppStarted").toString())) {
			if (appLoadedListener != null) {
				appLoadedListener.appLoaded();
				appLoadedListener = null;
			}
		}
	}
}
