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
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import de.catma.backgroundservice.BackgroundService;
import de.catma.backgroundservice.DefaultProgressCallable;
import de.catma.backgroundservice.ExecutionListener;
import de.catma.ui.ApplicationStartedNotifier;
import de.catma.ui.ApplicationStartedNotifier.AppLoadedListener;
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
	private BackgroundService backgroundService;
	private ProgressIndicator pi;
	private Tagger tagger;
	private String annotationServerURL;
	private String constraintServerURL;
	private boolean appLoaded = false;
	
	@Override
	public void init() {
		final Window mainWindow = new Window("Annotator");
		Properties properties = loadProperties();
		annotationServerURL = properties.getProperty("annotationServer");
		constraintServerURL = properties.getProperty("constraintServer");
		
		Panel editorPanel = new Panel("Interedition OAC Annotation Client - Bootcamp, January 2012, Leuven");
		editorPanel.setStyleName("editor-panel");
		editorPanel.getContent().setSizeUndefined();
		editorPanel.setWidth("640px");
		//editorPanel.setScrollable(true);

		Pager pager = new Pager(80, 30);
		
		tagger = new Tagger(pager, new TaggerListener() {
			
			public void tagInstanceAdded(TagInstance tagInstance) {
				try {
					tagInstance.setTargetURI(uri);
					tagInstance.setAuthorURI("http://applications.org/interedition-oac-client");
					AnnotationServerConnection annotationServerConnection = 
							new AnnotationServerConnection(annotationServerURL, constraintServerURL);
					annotationServerConnection.putAnnotation(tagInstance);
				} catch (Exception e) {
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
		
		final HorizontalLayout appLayout = new HorizontalLayout();
		
		final VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.addComponent(editorPanel);
		mainLayout.setComponentAlignment(editorPanel, Alignment.MIDDLE_CENTER);

		mainWindow.setContent(appLayout);
		
		mainLayout.addComponent(pagerComponent);
		mainLayout.setComponentAlignment(pagerComponent, Alignment.MIDDLE_CENTER);
		
		VerticalLayout menuLayout = new VerticalLayout();
		appLayout.addComponent(menuLayout);
		appLayout.setComponentAlignment(menuLayout, Alignment.TOP_CENTER);
		menuLayout.setMargin(true);
		appLayout.addComponent(mainLayout);
		appLayout.setComponentAlignment(mainLayout, Alignment.MIDDLE_CENTER);
		

		Button reloadAnnotations = new Button("Reload annotations");
		reloadAnnotations.addListener(new ClickListener() {
			
			public void buttonClick(ClickEvent event) {
				loadAnnotations();
			}
		});
		menuLayout.addComponent(reloadAnnotations);
		
		Button aboutBT = new Button("About & Usage");
		aboutBT.addListener(new ClickListener() {
			
			public void buttonClick(ClickEvent event) {
				InfoDialog d = new InfoDialog();
				mainWindow.addWindow(d);
				
			}
		});
		menuLayout.addComponent(aboutBT);
		
		setMainWindow(mainWindow);
		setTheme("cleatheme");

		mainWindow.addParameterHandler(new ParameterHandler() {

			public void handleParameters(Map<String, String[]> parameters) {
				System.out.println( "bla");
				uri = "http://www.gutenberg.org/cache/epub/11/pg11.txt";
				if ((parameters != null) 
						&& (parameters.containsKey(ArgumentKey.uri.name()) 
								&& (parameters.get(ArgumentKey.uri.name()).length > 0))) {	
					uri = parameters.get(ArgumentKey.uri.name())[0];
				}
				if (appLoaded) {
					loadURI();
				}
			}
		});
		
		pi = new ProgressIndicator();
		pi.setIndeterminate(true);
		pi.setEnabled(false);
		
		ApplicationStartedNotifier asn = new ApplicationStartedNotifier(new AppLoadedListener() {
			
			public void appLoaded() {
				loadURI();
				appLoaded = true;
			}
		});

		menuLayout.addComponent(asn);

		
		VerticalLayout progressLayout = new VerticalLayout();
		progressLayout.setMargin(true);
		progressLayout.addComponent(pi);
		appLayout.addComponent(progressLayout);
		
		backgroundService = new BackgroundService(this);
		backgroundService.setProgressListener(new DefaultProgressListener(pi, this));
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
	
	private void loadURI() {
		pi.setEnabled(true);
		backgroundService.submit(
				new DefaultProgressCallable<String>() {
					public String call() throws Exception {
						getProgressListener().setIndeterminate(true, "Loading target text...");
						AnnotationTargetLoader annotationTargetLoader = 
								new AnnotationTargetLoader(
										new URI(uri));

						String result =  annotationTargetLoader.getTargetText();
						getProgressListener().setIndeterminate(false, "Loading target text finished!");
						return result;
					}
				},
				new ExecutionListener<String>() {
					public void done(String result) {
						tagger.setText(result);
					}
				});
		loadAnnotations();
	}
	
	private void loadAnnotations() {
		pi.setEnabled(true);
		backgroundService.submit(
				new DefaultProgressCallable<List<TagInstance>>() {
					public List<TagInstance> call() throws Exception {
						AnnotationServerConnection annotationServerConnection = 
								new AnnotationServerConnection(annotationServerURL, constraintServerURL);
						List<TagInstance> availableAnnotations = 
								annotationServerConnection.getAnnotations(uri, getProgressListener());
						return availableAnnotations;
					}
				},
				new ExecutionListener<List<TagInstance>>() {
					public void done(List<TagInstance> availableAnnotations) {
						tagger.setTagInstances(availableAnnotations);
						pi.setCaption("Annotation client is ready!");
						pi.setEnabled(false);
					}
				});
	}
}
