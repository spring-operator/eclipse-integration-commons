/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.livexp.ui.util;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Text;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.UIValueListener;
import org.springsource.ide.eclipse.commons.livexp.ui.Disposable;
import org.springsource.ide.eclipse.commons.livexp.ui.Stylers;
import org.springsource.ide.eclipse.commons.livexp.util.Filter;
import org.springsource.ide.eclipse.commons.livexp.util.Filters;

/**
 * Convenience methods to attach LiveExp / LiveVar model elements to SWT widgets.
 */
public class WidgetUtil {

	public static void connect(Text text, LiveVariable<String> model) {
		if (!text.isDisposed()) {
			text.addDisposeListener(de -> model.dispose());
			ModifyListener widgetListener = (me) -> {
				if (!text.isDisposed()) {
					model.setValue(text.getText());
				}
			};
			text.addModifyListener(widgetListener);
			Disposable disconnect = model.onChange(UIValueListener.from((e,v) -> {
				String oldText = text.getText();
				String newText = model.getValue();
				if (newText==null) {
					newText = "";
				}
				if (!oldText.equals(newText)) {
					text.setText(newText);
				}
			}));
			text.addDisposeListener(de -> disconnect.dispose());
			model.onDispose(de -> text.removeModifyListener(widgetListener));
		}
	}
	
	/**
	 * Connect a filterbox model to a treeviewer. This assumes that the filter is text-based. The filter is applied to the labels of the elements in the tree.
	 * <p>
	 * For the viewer filter to work correctly the ITreeContentProvider must provide a proper implementation of the 'getParent' method. If getParent only
	 * returns null the viewer filter will not be able to check whether an element should be shown when a parent element is selected by the search filter.
	 * <p>
	 * Note: you can use {@link TreeElementWrappingContentProvider} in order to ensure that ITreeContentProvider keeps track of parent nodes properly.
	 */
	public static void connectTextBasedFilter(TreeViewer viewer, LiveExpression<Filter<String>> searchBoxModel, LabelProvider labels, ITreeContentProvider treeContent) {
 		TreeAwareViewerFilter viewerFilter = new TreeAwareViewerFilter(viewer, Filters.acceptAll(), labels, treeContent);
		Disposable disposable = searchBoxModel.onChange(UIValueListener.from((e, filter) -> {
			viewerFilter.setFilter(searchBoxModel.getValue());
			viewer.refresh();
		}));
		viewer.setFilters(viewerFilter); //TODO: what if there are existing filters?
		viewer.getControl().addDisposeListener(de -> {
			disposable.dispose();
		});
		Stylers stylers = new Stylers(viewer.getTree().getFont());
		ILabelProvider baseLabels = (ILabelProvider) viewer.getLabelProvider();
		Assert.isNotNull(baseLabels); //Can't add bolding support without this! Ensure label provider is set before calling this method
		
		viewer.setLabelProvider(boldMatchedElements(stylers, baseLabels, Filters.delegatingTo(searchBoxModel)));
	}
	
	/**
	 * Decorate a basic LabelProvider so that it bolds matched elements based on a text-based filter applied to its labels.
	 * @return 
	 */
	public static StyledCellLabelProvider boldMatchedElements(Stylers stylers, ILabelProvider baseLabels, Filter<String> filter) {
		return new StyledCellLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
				Object element = cell.getElement();
				
				//image
				cell.setImage(baseLabels.getImage(element));
				
				//styled label
				String label = baseLabels.getText(element);
				StyledString styledLabel = new StyledString(label);
				if (!filter.isTrivial() && filter.accept(label)) {
					styledLabel.setStyle(0, label.length(), stylers.bold());
				}
				cell.setStyleRanges(styledLabel.getStyleRanges());
				cell.setText(styledLabel.getString());
			}
			
		};
	}
	
}
