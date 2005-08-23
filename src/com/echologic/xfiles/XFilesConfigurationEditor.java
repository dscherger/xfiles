/*
 * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
 *
 * license to be determined.
 */
package com.echologic.xfiles;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;

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

    private Project project;

    private DefaultListModel filterListModel = new DefaultListModel();
    private JList filterList = new JList(filterListModel);

    private DefaultListModel testListModel = new DefaultListModel();
    private JList testList = new JList(testListModel);

    private ConfigurationTable statusTable = new ConfigurationTable("status");
    private ConfigurationTable typeTable = new ConfigurationTable("type");
    private ConfigurationTable vcsTable = new ConfigurationTable("vcs");
    private ConfigurationTable moduleTable = new ConfigurationTable("module");
    private ConfigurationTable globTable = new ConfigurationTable("glob");
    private ConfigurationTable otherTable = new ConfigurationTable("other");

    public XFilesConfigurationEditor(Project project) {
        this.project = project;

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

        // filter panel //

        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.Y_AXIS));

        ActionToolbar toolbar = actionManager.createActionToolbar("XFilesConfigurationToolbar", group, true);
        JComponent toolbarComponent = toolbar.getComponent();
        toolbarComponent.setMaximumSize(toolbarComponent.getPreferredSize());
        filterPanel.add(border(align(toolbar.getComponent())));
        filterPanel.add(border(align(filterList)));

        // config panel //

        JPanel configPanel = new JPanel();
        configPanel.setLayout(new BoxLayout(configPanel, BoxLayout.Y_AXIS));

        JLabel filterNameLabel = new JLabel("Filter Name:");
        configPanel.add(border(align(filterNameLabel)));

        /*
        configPanel.add(getTable("status",4));
        configPanel.add(getTable("type",8));
        configPanel.add(getTable("vcs",2));
        configPanel.add(getTable("module",3));
        configPanel.add(getTable("other",4));
        configPanel.add(getTable("globs",2));
        */

        statusTable.initialize(4);
        typeTable.initialize(8);
        vcsTable.initialize(2);
        moduleTable.initialize(3);
        otherTable.initialize(4);
        globTable.initialize(2);

        configPanel.add(statusTable.getScrollPane());
        configPanel.add(typeTable.getScrollPane());
        configPanel.add(vcsTable.getScrollPane());
        configPanel.add(moduleTable.getScrollPane());
        configPanel.add(otherTable.getScrollPane());
        configPanel.add(globTable.getScrollPane());

        // test panel //

        JPanel testPanel = new JPanel();
        testPanel.setLayout(new BoxLayout(testPanel, BoxLayout.Y_AXIS));

        JButton testButton = new JButton("Test Filter");
        testPanel.add(border(align(testButton)));
        testPanel.add(border(align(testList)));


        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.add(border(align(filterPanel)));
        this.add(border(align(configPanel)));
        this.add(border(align(testPanel)));
   }

    /*
    private JComponent getTable(String type, int count) {
        ConfigurationTableModel model = new ConfigurationTableModel(type);
        for (int i=0; i<count; i++) {
            model.add(new ConfigurationItem(true, type, i));
        }

        JTable table = new JTable(model);
        table.setPreferredScrollableViewportSize(table.getPreferredSize());
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        return border(align(new JScrollPane(table)));
    }
    */

    /**
     * For box layout to work well we need to ensure things are aligned consistently.
     *
     * @param c component to align
     * @return component aligned top left
     */
    private JComponent align(JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.setAlignmentY(Component.TOP_ALIGNMENT);
        return c;
    }

    private JComponent border(JComponent c) {
        c.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.red),
                                                       c.getBorder()));
        return c;
    }

    public boolean isModified(XFilesConfiguration configuration) {
        return false;
    }

    /**
     * Apply the current values in this editor to the specified configuration.
     *
     * @param configuration
     */
    public void apply(XFilesConfiguration configuration) {

    }

    /**
     * Reset the current values in this editor from the specified configuration.
     *
     * @param configuration
     */
    public void reset(XFilesConfiguration configuration) {

        filterListModel.clear();
        for (Iterator iterator = configuration.CONFIGURED_FILTERS.iterator(); iterator.hasNext();) {
            XFilesFilterConfiguration filter = (XFilesFilterConfiguration) iterator.next();
            filterListModel.addElement(filter.NAME);
        }

        // we run a counting filter at this point to get available values
        // after filter has completed use the associated logger to get initial values for
        // the confugration panel then apply current configurations on top of that

        final ProjectRootManager projectRootManager = ProjectRootManager.getInstance(project);
        final ProjectFileIndex index = projectRootManager.getFileIndex();

        VirtualFile[] roots = projectRootManager.getContentRoots();

        log.debug("iterating content under roots");

        FilterLogger logger = new FilterLogger();
        XFilesFilterConfiguration empty = new XFilesFilterConfiguration();
        XFilesVirtualFileFilter filter = new XFilesVirtualFileFilter(project);
        filter.setConfiguration(empty);
        filter.setLogger(logger);

        XFilesContentIterator content = new XFilesContentIterator(filter);

        for (int i = 0; i < roots.length; i++) {
            VirtualFile root = roots[i];
            log.debug("root " + root.getPath());
            index.iterateContentUnderDirectory(root, content);
        }

        // run filter for current counts and values
        // set flags based on configurations

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

        public void update(AnActionEvent e) {
            Presentation presentation = e.getPresentation();
            presentation.setEnabled(false);
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

        public String getName() {
            return name;
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

    private class ConfigurationTable extends JTable {

        public ConfigurationTable(String name) {
            setModel(new ConfigurationTableModel(name));
            setPreferredScrollableViewportSize(getPreferredSize());
            setAutoResizeMode(AUTO_RESIZE_ALL_COLUMNS);
        }

        public JComponent getScrollPane() {
            return border(align(new JScrollPane(this)));
        }

        public void initialize(int count) {
            ConfigurationTableModel model = (ConfigurationTableModel) getModel();
            for (int i=0; i<count; i++) {
                model.add(new ConfigurationItem(true, model.getName(), i));
            }

        }
    }

}
