package org.springsource.ide.eclipse.commons.gotosymbol.ui;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.search.internal.ui.text.EditorOpener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;
import org.springsource.ide.eclipse.commons.quicksearch.core.LineItem;
import org.springsource.ide.eclipse.commons.quicksearch.core.QuickTextQuery;
import org.springsource.ide.eclipse.commons.quicksearch.core.QuickTextQuery.TextRange;
import org.springsource.ide.eclipse.commons.quicksearch.ui.QuickSearchActivator;
import org.springsource.ide.eclipse.commons.quicksearch.ui.QuickSearchDialog;

public class GotoSymbolHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
//		IWorkbenchWindow w = HandlerUtil.getActiveWorkbenchWindow(event);
		System.out.println("Howdy");
		return null;
	}
	
	public static void doQuickSearch(IWorkbenchWindow window) {
		try {
			QuickSearchDialog dialog = new QuickSearchDialog(window);
			initializeFromSelection(window, dialog);
			int code = dialog.open();
			if (code == QuickSearchDialog.OK) {
				LineItem item = (LineItem) dialog.getFirstResult();
				if (item!=null) {
					QuickTextQuery q = dialog.getQuery();
					TextRange range = q.findFirst(item.getText());
					EditorOpener opener = new EditorOpener();
					IWorkbenchPage page = window.getActivePage();
					if (page!=null) {
						opener.openAndSelect(page, item.getFile(), range.getOffset()+item.getOffset(), 
							range.getLength(), true);
					}
				}
			}
		} catch (PartInitException e) {
			QuickSearchActivator.log(e);
		}
	}
	

	 static private void initializeFromSelection(IWorkbenchWindow workbench, QuickSearchDialog dialog) {
		if (workbench!=null) {
			ISelectionService selectionService = workbench.getSelectionService();
			ISelection selection = selectionService.getSelection();
			if (selection!=null && selection instanceof ITextSelection) {
				//Use text selection to set initial search pattern.
				String text = ((ITextSelection) selection).getText();
				if (text!=null && !"".equals(text)) {
					dialog.setInitialPattern(text, QuickSearchDialog.FULL_SELECTION);
				}
			} 
		}
//		IEditorPart editor = HandlerUtil.getActiveEditor(event);
//		if (editor!=null && editor instanceof ITextEditor) {
//			ITextEditor textEditor = (ITextEditor)editor;
//			ISelection selection = textEditor.getSelectionProvider().getSelection();
//			if (selection!=null && selection instanceof ITextSelection) {
//				String text = ((ITextSelection) selection).getText();
//				if (text!=null && !"".equals(text)) {
//					dialog.setInitialPattern(text, QuickSearchDialog.FULL_SELECTION);
//				}
//			}
//		}
	}
}
