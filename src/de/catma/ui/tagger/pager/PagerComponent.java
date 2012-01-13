package de.catma.ui.tagger.pager;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;

import de.catma.ui.tagger.pager.Pager.PagerListener;

public class PagerComponent extends HorizontalLayout {
	
	public static interface PageChangeListener {
		public void pageChanged(int number);
	}
	
	private static class NumberField extends TextField {
		private int startValue;
		public NumberField(int number) {
			super();
			startValue = number;
			setNumber(number);
		}

		public void setNumber(int number) {
			setValue(String.valueOf(number));
		}
		
		public int getNumber() {
			String value = getValue().toString();
			try {
				int pageNumber = Integer.valueOf(value);
				return pageNumber;
			}
			catch(NumberFormatException nfe) {
				return startValue;
			}
		}
	}

	private static final ThemeResource firstPageIcon = new ThemeResource("resources/page-first.gif");
	private static final ThemeResource previousPageIcon = new ThemeResource("resources/page-prev.gif");
	private static final ThemeResource nextPageIcon = new ThemeResource("resources/page-next.gif");
	private static final ThemeResource lastPageIcon = new ThemeResource("resources/page-last.gif");
	
	private Button firstPageButton;
	private Button previousPageButton;
	private NumberField pageInput;
	private Button nextPageButton;
	private Button lastPageButton;
	private PageChangeListener pageChangeListener;
	private int currentPageNumber = 1;
	private int lastPageNumber;
	private Label lastPageNumberLabel;
	
	public PagerComponent(final Pager pager, PageChangeListener pageChangeListener) {
		this.pageChangeListener = pageChangeListener;
		initComponents();
		initActions();
		pager.setPagerListener(new PagerListener() {
			
			public void textChanged() {
				setLastPageNumber(pager.getLastPageNumber());
			}
		});
	}
	
	public void setLastPageNumber(int lastPageNumber) {
		this.lastPageNumber = lastPageNumber;
		this.lastPageNumberLabel.setValue("/"+lastPageNumber);
	}

	private void initActions() {
		firstPageButton.addListener(new ClickListener() {
			
			public void buttonClick(ClickEvent event) {
				currentPageNumber = 1;
				pageInput.setValue("1");
			}
		});
		previousPageButton.addListener(new ClickListener() {
			
			public void buttonClick(ClickEvent event) {
				currentPageNumber--;
				pageInput.setNumber(currentPageNumber);
			}
		});
		nextPageButton.addListener(new ClickListener() {
			
			public void buttonClick(ClickEvent event) {
				currentPageNumber++;
				pageInput.setNumber(currentPageNumber);
			}
		});
		lastPageButton.addListener(new ClickListener() {
			
			public void buttonClick(ClickEvent event) {
				currentPageNumber = lastPageNumber;
				pageInput.setNumber(currentPageNumber);
			}
		});
		pageInput.addListener(new ValueChangeListener() {
			
			public void valueChange(ValueChangeEvent event) {
				currentPageNumber = pageInput.getNumber();
				pageChangeListener.pageChanged(currentPageNumber);
			}
		});
		
	}

	private void initComponents() {
		setSpacing(true);
		firstPageButton = new Button();
		firstPageButton.setIcon(firstPageIcon);
		addComponent(firstPageButton);
		previousPageButton = new Button();
		previousPageButton.setIcon(previousPageIcon);
		addComponent(previousPageButton);
		pageInput = new NumberField(1);
		pageInput.setImmediate(true);
		pageInput.setStyleName("pager-pageinput");
		addComponent(pageInput);
		lastPageNumberLabel = new Label("/NA");
		addComponent(lastPageNumberLabel);
		nextPageButton = new Button();
		nextPageButton.setIcon(nextPageIcon);
		addComponent(nextPageButton);
		lastPageButton = new Button();
		lastPageButton.setIcon(lastPageIcon);
		addComponent(lastPageButton);
	}
}
