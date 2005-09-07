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
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;

/**
 * This configuration editor panel is modeled after the JUnit run configuration editor.
 *
 * @author <a href="mailto:derek@echologic.com">Derek Scherger</a>
 */
public class XFilesConfigurationEditor extends JPanel {

    private Logger log = Logger.getInstance(getClass().getName());

    private Project project;

    private DefaultListModel filterListModel = new DefaultListModel();
    private JList filterList = new JList(filterListModel);

    private DefaultListModel testListModel = new DefaultListModel();
    private JList testList = new JList(testListModel);

    private ConfigurationTable statusTable = new ConfigurationTable();
    private ConfigurationTable typeTable = new ConfigurationTable();
    private ConfigurationTable vcsTable = new ConfigurationTable();
    private ConfigurationTable moduleTable = new ConfigurationTable();
    //private ConfigurationTable globTable = new ConfigurationTable();
    private ConfigurationTable otherTable = new ConfigurationTable();

    private EnableableAction add = new AddFilterAction();
    private EnableableAction remove = new RemoveFilterAction();
    private EnableableAction copy = new CopyFilterAction();
    private EnableableAction moveUp = new MoveFilterUpAction();
    private EnableableAction moveDown = new MoveFilterDownAction();

    public XFilesConfigurationEditor(Project project) {
        this.project = project;

        ActionManager actionManager = ActionManager.getInstance();

        add.setEnabled(true);

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

        ListSelectionListener listener = new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {

                    int selected = filterList.getSelectedIndex();
                    int first = 0;
                    int last = filterListModel.getSize() - 1;

                    remove.setEnabled(selected >= first);
                    copy.setEnabled(selected >= first);
                    moveUp.setEnabled(selected > first);
                    moveDown.setEnabled(selected < last);
                }
            }
        };

        ListSelectionModel selection = filterList.getSelectionModel();
        selection.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        selection.addListSelectionListener(listener);

        // config panel //

        JPanel configPanel = new JPanel();
        configPanel.setLayout(new BoxLayout(configPanel, BoxLayout.Y_AXIS));

        JLabel filterNameLabel = new JLabel("Filter Name:");
        configPanel.add(border(align(filterNameLabel)));

        configPanel.add(statusTable.getScrollPane());
        configPanel.add(typeTable.getScrollPane());
        configPanel.add(vcsTable.getScrollPane());
        configPanel.add(moduleTable.getScrollPane());
        configPanel.add(otherTable.getScrollPane());
        //configPanel.add(globTable.getScrollPane());

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

        XFilesVirtualFileFilter filter = new XFilesVirtualFileFilter(project);
        CountingFilterListener logger = new CountingFilterListener();
        filter.setListener(logger);

        XFilesContentIterator content = new XFilesContentIterator(project);
        content.setFilter(filter);
        content.iterate();

        log.debug("reset: " + content + logger);

        logger.log();

        filterListModel.clear();
        ConfigurableFilterModel selected = new ConfigurableFilterModel(logger);

        for (Iterator iterator = configuration.CONFIGURED_FILTERS.iterator(); iterator.hasNext();) {
            XFilesFilterConfiguration filterConfig = (XFilesFilterConfiguration) iterator.next();

            ConfigurableFilterModel filterModel = new ConfigurableFilterModel(logger);
            filterModel.reset(filterConfig);

            filterListModel.addElement(filterModel);
            // TODO: this model should contain configurable filter models
            // selection of one element must set the table models to those in the selected filter
            // and also enable/disable buttons on the configuration toolbar
            // delete simply removes one element and all of it's models from the list
            // add must create a new element with associated modesl and add it to the list
            // these models must be initialized with the results from running the reset filter
            /*
            if (filterModel.name.equals(configuration.SELECTED_FILTER)) {
                selected = filterModel;
                log.debug("selected filter " + selected.name);
            }
            */
        }

        if (selected != null) {
            statusTable.setModel(selected.statusModel);
            typeTable.setModel(selected.typeModel);
            vcsTable.setModel(selected.vcsModel);
            moduleTable.setModel(selected.moduleModel);
            //globTable.setModel(selected.globModel);
            otherTable.setModel(selected.otherModel);
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
         * @param map
         */
        public ConfigurationTableModel(VirtualFileCounterMap map) {
            this.name = map.getName();

            for (Iterator iterator = map.getCounters().iterator(); iterator.hasNext();) {
                VirtualFileCounter counter = (VirtualFileCounter) iterator.next();
                items.add(new ConfigurationItem(false, counter.getName(), counter.getCount()));
            }
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
        }

        /**
         * Get a list of the selected item names
         */
        public List getSelectedItems() {
            List selected = new ArrayList();
            for (Iterator iterator = items.iterator(); iterator.hasNext();) {
                ConfigurationItem item = (ConfigurationItem) iterator.next();
                if (item.included.booleanValue())
                    selected.add(item.text);
            }
            return selected;
        }

        /**
         * Set the list of selected item names
         *
         * @param selected
         */
        public void setSelectedItems(List selected) {
            for (Iterator iterator = items.iterator(); iterator.hasNext();) {
                ConfigurationItem item = (ConfigurationItem) iterator.next();

                if (selected.remove(item.text))
                    item.included = Boolean.TRUE;
                else
                    item.included = Boolean.FALSE;
            }

            for (Iterator iterator = selected.iterator(); iterator.hasNext();) {
                String text = (String) iterator.next();
                ConfigurationItem item = new ConfigurationItem(true, text, 0);
                items.add(item);
            }
        }

    }

    private class ConfigurationTable extends JTable {

        private JScrollPane scroller = new JScrollPane(this,
                                                       ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                                                       ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        public ConfigurationTable() {
            setAutoResizeMode(AUTO_RESIZE_ALL_COLUMNS);
        }

        public JComponent getScrollPane() {
            return border(align(scroller));
        }

        public void setModel(TableModel model) {
            super.setModel(model);
            setPreferredScrollableViewportSize(getPreferredSize());
        }
    }

    /**
     * This class holds entries in the list of configurable filters that's being edited
     */
    private class ConfigurableFilterModel {

        private String name;
        private ConfigurationTableModel statusModel;
        private ConfigurationTableModel typeModel;
        private ConfigurationTableModel vcsModel;
        private ConfigurationTableModel moduleModel;
        //private ConfigurationTableModel globModel;
        private ConfigurationTableModel otherModel;

        public ConfigurableFilterModel(CountingFilterListener logger) {
            // add unselected items and counts for each table in the logger

            // TODO: the idea here is that we initialize the models based on the logger
            // and then later set the list of selected things

            statusModel = new ConfigurationTableModel(logger.getStatusMap());
            typeModel = new ConfigurationTableModel(logger.getTypeMap());
            vcsModel = new ConfigurationTableModel(logger.getVcsMap());
            moduleModel = new ConfigurationTableModel(logger.getModuleMap());
            //globModel = new ConfigurationTableModel(logger.getGlobMap());
            otherModel = new ConfigurationTableModel(logger.getOtherMap());
        }

        public void reset(XFilesFilterConfiguration configuration) {
            name = configuration.NAME;
            // select items
            statusModel.setSelectedItems(configuration.ACCEPTED_STATUS_NAMES);
            typeModel.setSelectedItems(configuration.ACCEPTED_TYPE_NAMES);
            vcsModel.setSelectedItems(configuration.ACCEPTED_VCS_NAMES);
            moduleModel.setSelectedItems(configuration.ACCEPTED_MODULE_NAMES);
            //globModel.setSelectedItems(configuration.ACCEPTED_NAME_GLOBS);
            otherModel.setSelectedItems(configuration.ACCEPTED_OTHERS);
        }

        public void apply(XFilesFilterConfiguration configuration) {
            configuration.NAME = name;

            configuration.ACCEPTED_STATUS_NAMES.clear();
            configuration.ACCEPTED_TYPE_NAMES.clear();
            configuration.ACCEPTED_VCS_NAMES.clear();
            configuration.ACCEPTED_MODULE_NAMES.clear();
            configuration.ACCEPTED_NAME_GLOBS.clear();
            configuration.ACCEPTED_OTHERS.clear();

            configuration.ACCEPTED_STATUS_NAMES.addAll(statusModel.getSelectedItems());
            configuration.ACCEPTED_TYPE_NAMES.addAll(typeModel.getSelectedItems());
            configuration.ACCEPTED_VCS_NAMES.addAll(vcsModel.getSelectedItems());
            configuration.ACCEPTED_MODULE_NAMES.addAll(moduleModel.getSelectedItems());
            //configuration.ACCEPTED_NAME_GLOBS.addAll(globModel.getSelectedItems());
            configuration.ACCEPTED_OTHERS.addAll(otherModel.getSelectedItems());
        }

        public String toString() {
            return name;
        }
    }
}
