package eu.interedition.annotationclient;

import java.net.URI;
import java.util.Map;

import com.vaadin.Application;
import com.vaadin.terminal.ParameterHandler;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Window;

import de.catma.ui.tagger.Tagger;

public class AnnotationClientApplication extends Application {
	private static enum ArgumentKey {
		uri,
		;
	}


	@Override
	public void init() {
		final Window mainWindow = new Window("Annotator");

		Panel editorPanel = new Panel("Tagger");
		final Tagger tagger = new Tagger();
		tagger.setSizeFull();
		editorPanel.getContent().setSizeUndefined();
		editorPanel.setWidth("640px");
		editorPanel.addComponent(tagger);
		editorPanel.setScrollable(true);

		final HorizontalLayout mainLayout = new HorizontalLayout();

		mainWindow.setContent(mainLayout);
		
		mainLayout.addComponent(editorPanel);
		mainLayout.setExpandRatio(editorPanel, 2);
		
		setMainWindow(mainWindow);
		setTheme("cleatheme");

		mainWindow.addParameterHandler(new ParameterHandler() {

			public void handleParameters(Map<String, String[]> parameters) {

				String uri = "http://www.gutenberg.org/cache/epub/11/pg11.txt";
				if ((parameters != null) 
						&& (parameters.containsKey(ArgumentKey.uri.name()) 
								&& (parameters.get(ArgumentKey.uri.name()).length > 0))) {	
					uri = parameters.get(ArgumentKey.uri.name())[0];
				}
				
				try {
					AnnotationTargetLoader annotationTargetLoader = 
							new AnnotationTargetLoader(
									new URI(uri));
					tagger.setText(annotationTargetLoader.getTargetText());
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});
	}

}
