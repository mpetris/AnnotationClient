package de.catma.ui.tagger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;

import de.catma.ui.client.ui.tagger.VTagger;
import de.catma.ui.client.ui.tagger.shared.EventAttribute;
import de.catma.ui.client.ui.tagger.shared.TagInstance;
import de.catma.ui.tagger.pager.Page;
import de.catma.ui.tagger.pager.Pager;

/**
 * Server side component for the VMyComponent widget.
 */
@com.vaadin.ui.ClientWidget(VTagger.class)
public class Tagger extends AbstractComponent {
	
	public static interface TaggerListener {
		public void tagInstanceAdded(TagInstance tagInstance);
	}
	
	private static final long serialVersionUID = 1L;

	private Map<String,String> attributes = new HashMap<String, String>();
	private Pager pager;

	private TaggerListener taggerListener;
	
	public Tagger(Pager pager, TaggerListener taggerListener) {
		this.pager = pager;
		this.taggerListener = taggerListener;
	}

	@Override
	public void paintContent(PaintTarget target) throws PaintException {
		super.paintContent(target);

		if (target.isFullRepaint() && !pager.isEmpty()) {
			attributes.put(EventAttribute.HTML.name(), pager.getCurrentPage().toHTML());
		}
		
		if (!pager.isEmpty() && attributes.containsKey(EventAttribute.HTML.name())) {
			int i = 0;
			for (TagInstance t : pager.getCurrentPage().getTagInstances()) {
				target.addAttribute(EventAttribute.TAGINSTANCE.name()+i, t.toMap());
				i++;
			}
		}
		
		for (Map.Entry<String, String> entry : attributes.entrySet()) {
			target.addAttribute(entry.getKey(), entry.getValue());
		}
		
		attributes.clear();
		
		// We could also set variables in which values can be returned
		// but declaring variables here is not required
	}

	/**
	 * Receive and handle events and other variable changes from the client.
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public void changeVariables(Object source, Map<String, Object> variables) {
		super.changeVariables(source, variables);

		// Variables set by the widget are returned in the "variables" map.

		if (variables.containsKey(EventAttribute.TAGINSTANCE.name())) {
			@SuppressWarnings("unchecked")
			TagInstance tagInstance = 
				new TagInstance(
						(Map<String,Object>)variables.get(EventAttribute.TAGINSTANCE.name()));
			pager.getCurrentPage().addTagInstance(tagInstance);
			taggerListener.tagInstanceAdded(
					pager.getCurrentPage().getAbsoluteTagInstance(tagInstance));
		}
		
		if (variables.containsKey(EventAttribute.TAGINSTANCE_REMOVE.name())) {
			pager.getCurrentPage().removeTagInstance(
					(String)variables.get(EventAttribute.TAGINSTANCE_REMOVE.name()));
		}
		if (variables.containsKey(EventAttribute.LOGMESSAGE.name())) {
			System.out.println(variables.get(EventAttribute.LOGMESSAGE.name()));
		}
	}
	
	private void setHTML(String html) {
		attributes.put(EventAttribute.HTML.name(), html);
		requestRepaint();
	}
	
//	public void addTag(String tag) {
//		attributes.put(EventAttribute.TAGINSTANCE.name(), tag);
//		requestRepaint();				
//	}

	public void setText(String text) {
		pager.setText(text);
		setHTML(pager.getCurrentPage().toHTML());
	}
	
	public void setPage(int pageNumber) {
		Page page = pager.getPage(pageNumber);
		setHTML(page.toHTML());
	}

	public void addTagInstances(List<TagInstance> availableAnnotations) {
		
		for (TagInstance ti : availableAnnotations) {
			System.out.println(ti);
			Page page = pager.getPageFor(ti);
			if (page != null) {
				page.addAbsoluteTagInstance(ti);
			}
		}
		requestRepaint();
	}
}
