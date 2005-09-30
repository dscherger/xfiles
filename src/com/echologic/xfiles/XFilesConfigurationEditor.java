/*
 * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
 *
 * license to be determined.
 */
package com.echologic.xfiles;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Collections;
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
import javax.swing.JTabbedPane;
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

    CountingFilterListener counts;

    // TODO: these could all be the same class constructed around their associated commands

    private EnableableAction add = new AddFilterAction();
    private EnableableAction remove = new RemoveFilterAction();
    private EnableableAction copy = new CopyFilterAction();
    private EnableableAction moveUp = new MoveFilterUpAction();
    private EnableableAction moveDown = new MoveFilterDownAction();

    public interface Command {
        public void execute();
    }

    private Command addCommand = new Command() {
        public void execute() {
            addFilter();
        }
    };

    private Command removeCommand = new Command() {
        public void execute() {
            removeFilter();
        }
    };

    private Command copyCommand = new Command() {
        public void execute() {
            copyFilter();
        }
    };

    private Command moveUpCommand = new Command() {
        public void execute() {
            moveFilterUp();
        }
    };

    private Command moveDownCommand = new Command() {
        public void execute() {
            moveFilterDown();        
        }
    };


    public XFilesConfigurationEditor(Project project) {
        this.project = project;

        ActionManager actionManager = ActionManager.getInstance();

        add.setEnabled(true);
        add.setCommand(addCommand);
        remove.setCommand(removeCommand);
        copy.setCommand(copyCommand);
        moveUp.setCommand(moveUpCommand);
        moveDown.setCommand(moveDownCommand);

        DefaultActionGroup group = new DefaultActionGroup("xfiles configuration group", false);
        group.add(add);
        group.add(remove);
        group.add(copy);
        group.add(moveUp);
        group.add(moveDown);

        // filter panel //

        // TODO: probably factor out this panel and have it implement ListSelectionListener directly

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

                    if (selected >= first) {
                        ConfigurableFilterModel model = (ConfigurableFilterModel) filterListModel.getElementAt(selected);

                        // TODO: set the filter name from model.name

                        statusTable.setModel(model.statusModel);
                        typeTable.setModel(model.typeModel);
                        vcsTable.setModel(model.vcsModel);
                        moduleTable.setModel(model.moduleModel);
                        //globTable.setModel(model.globModel);
                        otherTable.setModel(model.otherModel);
                    }
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

        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        tabs.add("status", statusTable.getScrollPane());
        tabs.add("type", typeTable.getScrollPane());
        tabs.add("vcs", vcsTable.getScrollPane());
        tabs.add("module", moduleTable.getScrollPane());
        //tabs.add("glob", globTable.getScrollPane());
        tabs.add("other", otherTable.getScrollPane());

        configPanel.add(tabs);
        
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
     * QUESTION: should these be here or in the actions?
     *
     * perhaps it would be better to have a single ListAction class
     * with instances for each operation and either have the instances
     * hold this class or an adapter to this class to perform the operations?
     * the actions could be created with operation id's to be passed
     * back to a ListOperation interface (implemented here) to keep
     * the actions decoupled from this class. think command pattern,
     * and have the actions constructed with various list commands
     * that know about the list and other things they're operating on.
     *
     * so in actionPerformed() we simply do command.execute();
     *
     * the problem with putting these things in the actions is that the
     * actions then need access to the list, list mode, and configuration
     * tables
     */
    public void addFilter() {
        ConfigurableFilterModel filter = new ConfigurableFilterModel(counts);
        filter.name = "<unnamed>";
        int index = filterList.getSelectedIndex()+1;
        filterListModel.add(index, filter);
        filterList.setSelectedIndex(index);
    }

    public void removeFilter() {
        int index = filterList.getSelectedIndex();
        if (index >= 0) {
            filterListModel.removeElementAt(index);
        }

        if (index >= filterListModel.size()) {
            index = filterListModel.size()-1;
        }

        if (index >= 0) {
            filterList.setSelectedIndex(index);
        } else {
            // TODO: may need to clear configuration tables if there are no remaining filters
        }
    }

    public void copyFilter() {
        int index = filterList.getSelectedIndex();
        if (index >= 0) {
            ConfigurableFilterModel filter = (ConfigurableFilterModel) filterListModel.getElementAt(index);
            filter = new ConfigurableFilterModel(filter);
            filter.name = "<unnamed>";
            filterListModel.add(index, filter);
            filterList.setSelectedIndex(index);
        }
    }

    private void swap(int index1, int index2) {
        Object element1 = filterListModel.getElementAt(index1);
        Object element2 = filterListModel.getElementAt(index2);

        filterListModel.setElementAt(element1, index2);
        filterListModel.setElementAt(element2, index1);

        filterList.setSelectedIndex(index2);
    }

    public void moveFilterUp() {
        int index = filterList.getSelectedIndex();
        swap(index, index - 1);
    }

    public void moveFilterDown() {
        int index = filterList.getSelectedIndex();
        swap(index, index + 1);
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

    private boolean equals(String name, Object a, Object b) {
        if (a.equals(b)) return true;
        log.debug(name + " differs");
        log.debug(a.toString());
        log.debug(b.toString());
        return false;
    }

    private boolean equals(XFilesFilterConfiguration externalizable, ConfigurableFilterModel configurable) {
        if (!equals("name", externalizable.NAME, configurable.name)) return false;

        if (!equals("status", externalizable.ACCEPTED_STATUS_NAMES,  configurable.statusModel.getSelectedItems())) return false;
        if (!equals("type", externalizable.ACCEPTED_TYPE_NAMES, configurable.typeModel.getSelectedItems())) return false;
        if (!equals("vcs", externalizable.ACCEPTED_VCS_NAMES, configurable.vcsModel.getSelectedItems())) return false;
        if (!equals("module", externalizable.ACCEPTED_MODULE_NAMES, configurable.moduleModel.getSelectedItems())) return false;
        // (!equals("glob", externalizable.ACCEPTED_GLOB_NAMES, configurable.globModel.getSelectedItems())) return false;
        if (!equals("other", externalizable.ACCEPTED_OTHERS, configurable.otherModel.getSelectedItems())) return false;

        // TODO: it would be helpful to have a list of the various lists

        return true;
    }

    // RANDOM THOUGHT
    // - ExternalizableFilterConfiguration
    // - ConfigurableFilterConfiguration
    // both extending FilterConfiguration which implements .equals()

    public boolean isModified(XFilesConfiguration configuration) {
        if (configuration.CONFIGURED_FILTERS.size() != filterListModel.size()) {
            log.debug("size modified");
            return true;
        }

        for (int i=0; i<filterListModel.size(); i++) {
            XFilesFilterConfiguration externalizable = (XFilesFilterConfiguration) configuration.CONFIGURED_FILTERS.get(i);
            ConfigurableFilterModel configurable = (ConfigurableFilterModel) filterListModel.get(i);
            if (!equals(externalizable, configurable)) {
                log.debug("configuration " + externalizable.NAME + " modified");
                return true;
            }
        }

        return false;
    }

    /**
     * Apply the current values in this editor to the specified configuration.
     *
     * @param configuration
     */
    public void apply(XFilesConfiguration configuration) {
        configuration.CONFIGURED_FILTERS = new ArrayList();

        for (int i=0; i<filterListModel.size(); i++) {
            ConfigurableFilterModel configurable = (ConfigurableFilterModel) filterListModel.get(i);
            XFilesFilterConfiguration externalizable = new XFilesFilterConfiguration();
            configurable.apply(externalizable);
            configuration.CONFIGURED_FILTERS.add(externalizable);
        }

        // TODO: sort out this (and other) bidirectional dependency
        configuration.getListener().configurePopupActionGroup();
    }

    /**
     * Reset the current values in this editor from the specified configuration.
     *     basically,
     * @param configuration
     */
    public void reset(XFilesConfiguration configuration) {

        XFilesVirtualFileFilter filter = new XFilesVirtualFileFilter(project);

        counts = new CountingFilterListener();
        filter.setListener(counts);

        XFilesContentIterator content = new XFilesContentIterator(project);
        content.setFilter(filter);
        content.iterate();

        log.debug("reset: " + content + counts);

        counts.log();

        filterListModel.clear();

        for (Iterator iterator = configuration.CONFIGURED_FILTERS.iterator(); iterator.hasNext();) {
            XFilesFilterConfiguration filterConfig = (XFilesFilterConfiguration) iterator.next();

            ConfigurableFilterModel filterModel = new ConfigurableFilterModel(counts);
            filterModel.reset(filterConfig);

            filterListModel.addElement(filterModel);
        }

        // select the first filter...
        // TODO: make sure that there is at least one filter
        // TODO: initially select the currently active filter?
        // could use the selected name from the configuration

        filterList.setSelectedIndex(0);
    }

    private class ConfigurationItem implements Comparable {
        private Boolean included;
        private String text;
        private Integer count;

        public ConfigurationItem(boolean included, String text, int count) {
            this.included = Boolean.valueOf(included);
            this.text = text;
            this.count = Integer.valueOf(count);
        }

        public int compareTo(Object o) {
            ConfigurationItem that = (ConfigurationItem) o;
            return this.text.compareTo(that.text);
        }
    }

    private class ConfigurationTableModel extends AbstractTableModel {

        private String name;
        private List items = new ArrayList();

        public ConfigurationTableModel() {
        }

        public ConfigurationTableModel(ConfigurationTableModel that) {
            this.name = that.name;
            this.items.addAll(that.items);
        }

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

            // sorted so that comparisons work properly
            Collections.sort(items);
        }

        public String getName() {
            return name;
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
         * Set the list of selected item names. This sets the selection on existing items included
         * in the selected list and then adds any items in the selected list.
         *
         * Alternatively we could hold items in a map keyed by name with selected/count values.
         * This would allow for iterating over the selected list and simply checking the map
         * for existing values. 
         *
         * @param selected
         */
        public void setSelectedItems(List selected) {
            List temp = new ArrayList();
            temp.addAll(selected);

            for (Iterator iterator = items.iterator(); iterator.hasNext();) {
                ConfigurationItem item = (ConfigurationItem) iterator.next();

                if (temp.remove(item.text))
                    item.included = Boolean.TRUE;
                else
                    item.included = Boolean.FALSE;
            }

            for (Iterator iterator = temp.iterator(); iterator.hasNext();) {
                String text = (String) iterator.next();
                ConfigurationItem item = new ConfigurationItem(true, text, 0);
                items.add(item);
            }

            Collections.sort(items);
        }

    }

    private class ConfigurationTable extends JTable {

        private JScrollPane scroller = new JScrollPane(this,
                                                       ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                                                       ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        public ConfigurationTable() {
            super(new ConfigurationTableModel());
            setAutoResizeMode(AUTO_RESIZE_ALL_COLUMNS);
        }

        public JComponent getScrollPane() {
            return border(align(scroller));
        }

        public void setModel(TableModel model) {
            super.setModel(model);

            ConfigurationTableModel config = (ConfigurationTableModel) model;
            log.debug("setModel " + config.name);

            Dimension d1 = getPreferredSize();
            log.debug("preferred size " + d1);
            Dimension d2 = getPreferredScrollableViewportSize();
            log.debug("viewport size " + d2);

            setPreferredScrollableViewportSize(getPreferredSize());
            revalidate();

            Dimension d3 = getPreferredScrollableViewportSize();
            log.debug("viewport size " + d3);
        }
    }

    /**
     * This class holds entries in the list of configurable filters that's being edited
     */
    private class ConfigurableFilterModel {

        private String name;

        // TODO: we may want to hold these in an array
        private ConfigurationTableModel statusModel;
        private ConfigurationTableModel typeModel;
        private ConfigurationTableModel vcsModel;
        private ConfigurationTableModel moduleModel;
        //private ConfigurationTableModel globModel;
        private ConfigurationTableModel otherModel;

        /**
         * Copy constructor
         *
         * @param that
         */
        public ConfigurableFilterModel(ConfigurableFilterModel that) {
            this.name = that.name;
            this.statusModel = new ConfigurationTableModel(that.statusModel);
            this.typeModel = new ConfigurationTableModel(that.typeModel);
            this.vcsModel = new ConfigurationTableModel(that.vcsModel);
            this.moduleModel = new ConfigurationTableModel(that.moduleModel);
            //this.globModel = new ConfigurationTableModel(that.globModel);
            this.otherModel = new ConfigurationTableModel(that.otherModel);
        }

        public ConfigurableFilterModel(CountingFilterListener listener) {
            statusModel = new ConfigurationTableModel(listener.getStatusMap());
            typeModel = new ConfigurationTableModel(listener.getTypeMap());
            vcsModel = new ConfigurationTableModel(listener.getVcsMap());
            moduleModel = new ConfigurationTableModel(listener.getModuleMap());
            //globModel = new ConfigurationTableModel(listener.getGlobMap());
            otherModel = new ConfigurationTableModel(listener.getOtherMap());
        }

        public void reset(XFilesFilterConfiguration configuration) {
            name = configuration.NAME;
            statusModel.setSelectedItems(configuration.ACCEPTED_STATUS_NAMES);
            typeModel.setSelectedItems(configuration.ACCEPTED_TYPE_NAMES);
            vcsModel.setSelectedItems(configuration.ACCEPTED_VCS_NAMES);
            moduleModel.setSelectedItems(configuration.ACCEPTED_MODULE_NAMES);
            //globModel.setSelectedItems(configuration.ACCEPTED_NAME_GLOBS);
            otherModel.setSelectedItems(configuration.ACCEPTED_OTHERS);
        }

        public void apply(XFilesFilterConfiguration configuration) {
            log.debug("saving filter " + name);
            configuration.log();
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
            configuration.log();
        }

        public String toString() {
            return name;
        }
    }

}
