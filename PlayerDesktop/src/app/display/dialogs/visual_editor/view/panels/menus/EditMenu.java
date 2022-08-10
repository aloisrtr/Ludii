package app.display.dialogs.visual_editor.view.panels.menus;

import app.display.dialogs.visual_editor.handler.Handler;

import javax.swing.*;
import java.awt.event.ActionListener;

public class EditMenu extends JMenu
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 3590839696737937583L;

	public EditMenu(EditorMenuBar menuBar)
    {
        super("Edit");
        menuBar.addJMenuItem(this, "Undo", undo, KeyStroke.getKeyStroke("control Z"));
        menuBar.addJMenuItem(this, "Redo", redo, KeyStroke.getKeyStroke("control Y"));
        add(new JSeparator());
        menuBar.addJMenuItem(this, "Copy", copy, KeyStroke.getKeyStroke("control C"));
        menuBar.addJMenuItem(this, "Paste", paste, KeyStroke.getKeyStroke("control V"));
        menuBar.addJMenuItem(this, "Duplicate", duplicate, KeyStroke.getKeyStroke("control shift D"));
        menuBar.addJMenuItem(this, "Delete", delete, KeyStroke.getKeyStroke("control D"));
        add(new JSeparator());
        menuBar.addJMenuItem(this, "Select All", selectAll, KeyStroke.getKeyStroke("control A"));
        menuBar.addJMenuItem(this, "Unselect All", unselectAll);
        menuBar.addJMenuItem(this, "Collapse", collapse, KeyStroke.getKeyStroke("control W"));
        menuBar.addJMenuItem(this, "Expand", expand, KeyStroke.getKeyStroke("control E"));
        menuBar.addJMenuItem(this, "Expand All", expandAll);
        add(new JSeparator());
        menuBar.addJCheckBoxMenuItem(this, "Confirm Sensitive Actions", Handler.sensitivityToChanges, e -> Handler.sensitivityToChanges = !Handler.sensitivityToChanges);
    }

    final ActionListener undo = e -> Handler.undo();

    final ActionListener redo = e -> Handler.redo();

    final ActionListener copy = e -> Handler.copy();

    final ActionListener paste = e -> Handler.paste(-1, -1);

    final ActionListener duplicate = e -> Handler.duplicate();

    final ActionListener delete = e -> Handler.remove();

    final ActionListener selectAll = e -> Handler.selectAll();

    final ActionListener unselectAll = e -> Handler.unselectAll();

    final ActionListener collapse = e -> Handler.collapse();

    final ActionListener expand = e -> Handler.expand();

    final ActionListener expandAll = e -> {
        Handler.selectAll();
        Handler.expand();
    };
}