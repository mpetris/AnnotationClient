package de.catma.ui.client.ui.tagger.menu;

import java.util.ArrayList;
import java.util.List;

import net.auroris.ColorPicker.client.ColorPicker;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.HandlerRegistration;

import de.catma.ui.client.ui.tagger.VTagger;
import de.catma.ui.client.ui.tagger.shared.TagInstance;

class TagMenuPopup extends DialogBox {
		
	private TreeItem root;
	private List<HandlerRegistration> handlerRegistrations = new ArrayList<HandlerRegistration>();
	private VTagger vTagger;
	private String lastSelectedColor = null;
	
	public TagMenuPopup(VTagger vTagger, String lastSelectedColor) {
		super(true);
		this.setText("Annotations");
		this.vTagger = vTagger;
		this.lastSelectedColor = lastSelectedColor;
		root = new TreeItem("Available annotations");
		final Tree tree = new Tree();
		tree.addItem(root);
		root.setState(true);
		root.setStyleName("tagger_menu_root");
		
		final VerticalPanel vPanel = new VerticalPanel();
		
		if (vTagger.hasSelection()) {
			
			final VerticalPanel annotationCreationPanel = new VerticalPanel();
			annotationCreationPanel.setSpacing(5);
			annotationCreationPanel.setWidth("100%");
			final TextArea annotationBodyInput = new TextArea();
			annotationBodyInput.setWidth("90%");
			annotationCreationPanel.add(annotationBodyInput);
			final HorizontalPanel annotationCreationButtonPanel = new HorizontalPanel();
			annotationCreationButtonPanel.setSpacing(5);
			final Label colorLabel = new HTML("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
			final ColorPicker colorPicker = new ColorPicker() {
				@Override
				public void onChange(Widget sender) {
					super.onChange(sender);
					colorLabel.getElement().setAttribute("style", "background:#"+this.getHexColor()+";");
				}
			};
			try {
				if (lastSelectedColor != null) {
					colorPicker.setHex(lastSelectedColor);
				}
				else {
					int [] randomColor = getRandomColor();
					colorPicker.setRGB( randomColor[0], randomColor[1], randomColor[2]);
				}
			} catch (Exception e) {
				// well...
			}
			colorLabel.getElement().setAttribute("style", "background:#"+colorPicker.getHexColor()+";");

			HorizontalPanel colorPanel = new HorizontalPanel();
			colorPanel.setSpacing(5);
			PushButton colorButton = new PushButton("Change color...");
			colorPanel.add(colorButton); 
			
			colorPanel.add(colorLabel);
			
			HandlerRegistration colorButtonReg = colorButton.addClickHandler(new ClickHandler() {
				
				public void onClick(ClickEvent event) {
					
					annotationCreationPanel.insert(
						colorPicker, annotationCreationPanel.getWidgetIndex(annotationCreationButtonPanel));
					TagMenuPopup.this.center();
				}
			});
			handlerRegistrations.add(colorButtonReg);
			annotationCreationPanel.add(colorPanel);
			
			PushButton saveButton = new PushButton("Save");
			//saveButton.setStylePrimaryName("tagger-pushButton");
			HandlerRegistration saveButtonReg = saveButton.addClickHandler(new ClickHandler() {
				
				public void onClick(ClickEvent event) {
					TagMenuPopup.this.vTagger.addTag(annotationBodyInput.getText(), colorPicker.getHexColor());
					TagMenuPopup.this.lastSelectedColor = colorPicker.getHexColor();
					hide();
				}
			});
			handlerRegistrations.add(saveButtonReg);
			
			PushButton cancelButton = new PushButton("Cancel");
			//cancelButton.setStylePrimaryName("tagger-pushButton");
			
			annotationCreationButtonPanel.add(saveButton);
			annotationCreationButtonPanel.add(cancelButton);
			annotationCreationPanel.add(annotationCreationButtonPanel);
		
			PushButton addAnnotationButton = new PushButton("Add annotation...");
			//addAnnotationButton.setStylePrimaryName("tagger-pushButton");
			
			HandlerRegistration addAnnotationBtReg = addAnnotationButton.addClickHandler(new ClickHandler() {
				
				public void onClick(ClickEvent event) {
					vPanel.insert(annotationCreationPanel, vPanel.getWidgetIndex(tree));
				}
			});
			handlerRegistrations.add(addAnnotationBtReg);
			vPanel.add(addAnnotationButton);
			
			HandlerRegistration cancelButtonReg = cancelButton.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					annotationBodyInput.setText("");
					vPanel.remove(annotationCreationPanel);
				}
			});
			
			handlerRegistrations.add(cancelButtonReg);
		}
		
		vPanel.add(tree);
		vPanel.setStylePrimaryName("tagger_menu");
		setWidget(vPanel);
	}
	
	@Override
	public void show() {
		Window.enableScrolling(true);
		super.show();
	}
	
	public void addTag(final String tagInstanceID) {
		
		TagInstance tagInstance = vTagger.getTagInstance(tagInstanceID);

		Grid grid = new Grid(1,3);
		Label l = new HTML(tagInstance.getBody() + " #" + tagInstanceID);
		grid.setWidget(0, 0, l);
		
		PushButton tagRemoveButton = new PushButton("remove");
		//tagRemoveButton.addStyleName("tagger-pushButton");
		grid.setWidget(0, 1, tagRemoveButton);
		HandlerRegistration tagRemoveBtReg = tagRemoveButton.addClickHandler(new ClickHandler() {
			
			public void onClick(ClickEvent event) {
				vTagger.removeTag(tagInstanceID);
				hide();
			}
		});
		handlerRegistrations.add(tagRemoveBtReg);
		
		PushButton tagEditButton = new PushButton("edit");
		//tagEditButton.addStyleName("tagger-pushButton");
		tagEditButton.setEnabled(false);
		grid.setWidget(0, 2, tagEditButton);
		HandlerRegistration tagEditBtReg = tagEditButton.addClickHandler(new ClickHandler() {
			
			public void onClick(ClickEvent event) {
				
			}
		});
		handlerRegistrations.add(tagEditBtReg);			
		root.addItem(grid);
		root.setState(true);
	}
	
	@Override
	public void hide() {
		super.hide();
		for (HandlerRegistration hr : handlerRegistrations) {
			hr.removeHandler();
		}
		Window.enableScrolling(false);
	}
	
	
	private int[] getRandomColor() {
		int i = Random.nextInt(3);
		switch(i) {
			case 0 : {
				return new int[] { 255, 0, 0};
			}
			case 1 : {
				return new int[] { 0, 255, 0};
			}
			case 2 : {
				return new int[] { 0, 0, 255};
			}
			default : {
				return new int[] { 0, 0, 255};
			}
		}
	}
	
	public String getLastSelectedColor() {
		return lastSelectedColor;
	}
	
}