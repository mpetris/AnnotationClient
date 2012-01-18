package eu.interedition.annotationclient;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.vaadin.Application;
import com.vaadin.terminal.ParameterHandler;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import de.catma.ui.client.ui.tagger.shared.TagInstance;
import de.catma.ui.tagger.Tagger;
import de.catma.ui.tagger.Tagger.TaggerListener;
import de.catma.ui.tagger.pager.Pager;
import de.catma.ui.tagger.pager.PagerComponent;
import de.catma.ui.tagger.pager.PagerComponent.PageChangeListener;
import eu.interedition.annotationcomm.AnnotationServerConnection;

/**
 * @author marco.petris@web.de
 *
 */
public class AnnotationClientApplication extends Application {
	
	private static enum ArgumentKey {
		uri,
		;
	}

	private static final String WEB_INF_DIR = "WEB-INF";
	private static final String PROPERTY_FILE = "annotationclient.properties";
	
	private String uri;
	
	@Override
	public void init() {
		final Window mainWindow = new Window("Annotator");
		Properties properties = loadProperties();
		final String annotationServerURL = properties.getProperty("annotationServer");
		final String constraintServerURL = properties.getProperty("constraintServer");
		
		Panel editorPanel = new Panel("Interedition OAC Annotation Client - Bootcamp, January 2012, Leuven");
		editorPanel.setStyleName("editor-panel");
		editorPanel.getContent().setSizeUndefined();
		editorPanel.setWidth("640px");
		//editorPanel.setScrollable(true);

		Pager pager = new Pager(80, 30);
		
		final Tagger tagger = new Tagger(pager, new TaggerListener() {
			
			public void tagInstanceAdded(TagInstance tagInstance) {
				try {
					tagInstance.setTargetURI(uri);
					tagInstance.setAuthorURI("http://applicatons.org/interedition-oac-client");
					AnnotationServerConnection annotationServerConnection = 
							new AnnotationServerConnection(annotationServerURL, constraintServerURL);
					annotationServerConnection.putAnnotation(tagInstance);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
		});
		
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
		Button reloadAnnotations = new Button("Reload annotations");
		reloadAnnotations.addListener(new ClickListener() {
			
			public void buttonClick(ClickEvent event) {
				try {
					AnnotationServerConnection annotationServerConnection = 
							new AnnotationServerConnection(annotationServerURL, constraintServerURL);
					List<TagInstance> availableAnnotations = annotationServerConnection.getAnnotations(uri);
					tagger.setTagInstances(availableAnnotations);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		mainLayout.addComponent(reloadAnnotations);
		
		setMainWindow(mainWindow);
		setTheme("cleatheme");

		mainWindow.addParameterHandler(new ParameterHandler() {

			public void handleParameters(Map<String, String[]> parameters) {

//			uri = "http://www.gutenberg.org/cache/epub/11/pg11.txt";
				uri = "file:///C:/data/projects/interedition/pg14.txt";
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
					AnnotationServerConnection annotationServerConnection = 
							new AnnotationServerConnection(annotationServerURL, constraintServerURL);
					List<TagInstance> availableAnnotations = annotationServerConnection.getAnnotations(uri);
					tagger.setTagInstances(availableAnnotations);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});
	}

	private Properties loadProperties() {
		String path = 
				this.getContext().getBaseDirectory() 
				+ System.getProperty("file.separator") 
				+ WEB_INF_DIR
				+ System.getProperty("file.separator") 
				+ PROPERTY_FILE;
		
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(path));
		}
		catch( IOException e) {
			e.printStackTrace();
		}
		return properties;
	}
}
