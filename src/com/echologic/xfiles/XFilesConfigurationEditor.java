/*
 * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
 *
 * license to be determined.
 */
package com.echologic.xfiles;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.table.AbstractTableModel;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.diagnostic.Logger;

/**
 * This configuration editor panel is modeled after the JUnit run configuration editor.
 *
 * @author <a href="mailto:derek@echologic.com">Derek Scherger</a>
 */
public class XFilesConfigurationEditor extends JPanel {

    private static final Logger log = Logger.getInstance(XFiles.class.getName());

    private Icon addIcon = new ImageIcon(getClass().getResource("/general/add.png"));
    private Icon removeIcon = new ImageIcon(getClass().getResource("/general/remove.png"));
    private Icon copyIcon = new ImageIcon(getClass().getResource("/general/copy.png"));
    private Icon moveUpIcon = new ImageIcon(getClass().getResource("/actions/moveUp.png"));
    private Icon moveDownIcon = new ImageIcon(getClass().getResource("/actions/moveDown.png"));

    private DefaultListModel filterListModel = new DefaultListModel();
    private JList filterList = new JList(filterListModel);

    public XFilesConfigurationEditor() {
        ActionManager actionManager = ActionManager.getInstance();

        AnAction add = new IconAction("add", "add", addIcon);
        AnAction remove = new IconAction("remove", "remove", removeIcon);
        AnAction copy = new IconAction("copy", "copy", copyIcon);
        AnAction moveUp = new IconAction("move up", "move up", moveUpIcon);
        AnAction moveDown = new IconAction("move down", "move down", moveDownIcon);

        DefaultActionGroup group = new DefaultActionGroup("xfiles configuration group", false);
        group.add(add);
        group.add(remove);
        group.add(copy);
        group.add(moveUp);
        group.add(moveDown);


        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.Y_AXIS));

        ActionToolbar toolbar = actionManager.createActionToolbar("XFilesConfigurationToolbar", group, true);

        filterPanel.add(toolbar.getComponent());
        filterPanel.add(filterList);

        JPanel configPanel = new JPanel();
        configPanel.setLayout(new BoxLayout(configPanel, BoxLayout.Y_AXIS));

        JLabel filterNameLabel = new JLabel("Filter Name:");
        filterNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        configPanel.add(filterNameLabel);
        configPanel.add(getTable("status",4));
        configPanel.add(getTable("type",8));
        configPanel.add(getTable("vcs",2));
        configPanel.add(getTable("module",3));
        configPanel.add(getTable("other",4));
        configPanel.add(getTable("globs",2));


        JPanel testPanel = new JPanel();
        testPanel.setLayout(new BoxLayout(testPanel, BoxLayout.Y_AXIS));

        JButton testButton = new JButton("Test Filter");
        testPanel.add(testButton);

        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.add(filterPanel);
        this.add(configPanel);
        this.add(testPanel);
   }

    private JComponent getTable(String type, int count) {
        ConfigurationTableModel model = new ConfigurationTableModel(type);
        for (int i=0; i<count; i++) {
            model.add(new ConfigurationItem(true, type, i));
        }

        JTable table = new JTable(model);
        table.setPreferredScrollableViewportSize(table.getPreferredSize());
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        return new JScrollPane(table);
    }

    // toolbar with add/delete/copy/up/down buttons (toolbar and actions)
    // list of configured filters (JList, Scrollpane, model
    // selecting a filter shows the associated filter configuration editor (selection listener)

    // filter configuration editor contains
    // status names
    // type names
    // vcs names
    // module names
    // path globs
    // ignored/source/test/files/directories/open check boxes
    // each item contains a count of matching things from running a blank filter over the current project

    // note that we're editing the settings for a single filter not for all filters
    // however the cancel/apply buttons apply to the entire configuration session
    // and either all changes or no changes are saved

    // we need a table component that holds a checkbox, type and count
    // that lists all available settings from the selected filter merged
    // with those from the last scan. several instances of this component
    // will be used, one for each different aspect of the filter

    // also something like match all (logical AND) or match any (logical OR) filter option 

    private class IconAction extends AnAction {
        public IconAction(String text, String description, Icon icon) {
            super(text, description, icon);
        }

        public void actionPerformed(AnActionEvent e) {
        }
    }

    private class ConfigurationItem {
        private Boolean included;
        private String text;
        private Integer count;

        public ConfigurationItem(boolean included, String text, int count) {
            this.included = Boolean.valueOf(included);
            this.text = text;
            this.count = Integer.valueOf(count);
        }
    }

    private class ConfigurationTableModel extends AbstractTableModel {

        private String name;
        private List items = new ArrayList();

        /**
         * specify the name of items in this model
         * i.e. vcs, status, type, etc.
         *
         * @param name
         */
        public ConfigurationTableModel(String name) {
            this.name = name;
        }

        public void add(ConfigurationItem item) {
            items.add(item);
        }

        public int getRowCount() {
            return items.size();
        }

        public int getColumnCount() {
            return 3;
        }

        public String getColumnName(int column) {
            switch (column) {
                case 0: return "include";
                case 1: return name;
                case 2: return "count";
                default: return null; // exception!
            }
        }

        public Class getColumnClass(int column) {
            switch (column) {
                case 0: return Boolean.class;
                case 1: return String.class;
                case 2: return Integer.class;
                default: return null; // exception!
            }
        }

        public boolean isCellEditable(int row, int column) {
            return column == 0;
        }

        public Object getValueAt(int row, int column) {
            ConfigurationItem item = (ConfigurationItem) items.get(row);
            switch (column) {
                case 0: return item.included;
                case 1: return item.text;
                case 2: return item.count;
                default: return null; // exception!
            }
        }

        public void setValueAt(Object value, int row, int column) {
            Boolean included = (Boolean) value;
            ConfigurationItem item = (ConfigurationItem) items.get(row);
            switch (column) {
                case 0: item.included = included;
                default: ; // exception!
            }

            log.debug((included ? "included " : "excluded ") + name + " " + item.text);
        }

        public List getSelectedItems() {
            List selected = new ArrayList();
            for (Iterator iterator = items.iterator(); iterator.hasNext();) {
                ConfigurationItem item = (ConfigurationItem) iterator.next();
                if (item.included.booleanValue())
                    selected.add(item.text);
            }
            return selected;
        }
    }
}
