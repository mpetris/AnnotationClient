package eu.interedition.annotationclient;

import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class InfoDialog extends Window {
	
	public InfoDialog() {
		super("About & Usage");
		VerticalLayout layout = new VerticalLayout();
		Label info = new Label(
				"<p>To add an annotation just mark some text and activate " +
				"the mouse menu by hovering over the marked text.</p>" +
				"<p>To show the body of an annotation just hover over the annotated text.</p>" +
				"<p>You can drag this bookmarklet " +
				"<a href=javascript:location.href=\"http://demo.interedition.eu/AnnotationClient/?uri=\"+location.href>Annotator</a>" +
				" to your browsers toolbar, navigate to a text to be annotated and click the Annotator toolbar button.</p>" +
				"<p>Please send bug reports and feedback to marco DOT petris AT web DOT de!</p>" +
				"<p>Leuven/Hamburg January 2012</p>");
		info.setContentMode(Label.CONTENT_XHTML);
		layout.addComponent(info);
		setModal(true);
		setContent(layout);
		setWidth("300px");
		setHeight("350px");
	}

}
