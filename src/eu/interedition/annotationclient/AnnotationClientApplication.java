package eu.interedition.annotationclient;

import java.net.URI;
import java.util.Map;

import com.vaadin.Application;
import com.vaadin.terminal.ParameterHandler;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import de.catma.ui.tagger.Tagger;
import de.catma.ui.tagger.pager.Pager;
import de.catma.ui.tagger.pager.PagerComponent;
import de.catma.ui.tagger.pager.PagerComponent.PageChangeListener;

public class AnnotationClientApplication extends Application {
	private static enum ArgumentKey {
		uri,
		;
	}

	@Override
	public void init() {
		final Window mainWindow = new Window("Annotator");

		Panel editorPanel = new Panel("Interedition OAC Annotation Client - Bootcamp, January 2012 Leuven");
		editorPanel.setStyleName("editor-panel");
		editorPanel.getContent().setSizeUndefined();
		editorPanel.setWidth("640px");
		editorPanel.setScrollable(true);
		Pager pager = new Pager(80, 30);
		System.out.println("nase");
		final Tagger tagger = new Tagger(pager);
		tagger.setSizeFull();
		editorPanel.addComponent(tagger);

		PagerComponent pagerComponent = new PagerComponent(pager, new PageChangeListener() {
			public void pageChanged(int number) {
				tagger.setPage(number);
			}
		});
		
		final VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.addComponent(editorPanel);
		mainLayout.setComponentAlignment(editorPanel, Alignment.MIDDLE_CENTER);

		mainWindow.setContent(mainLayout);
		
		mainLayout.addComponent(pagerComponent);
		mainLayout.setComponentAlignment(pagerComponent, Alignment.MIDDLE_CENTER);
		
		setMainWindow(mainWindow);
		setTheme("cleatheme");

		mainWindow.addParameterHandler(new ParameterHandler() {

			public void handleParameters(Map<String, String[]> parameters) {

//				String uri = "http://www.gutenberg.org/cache/epub/11/pg11.txt";
				String uri = "file:///C:/data/projects/interedition/pg11.txt";
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
