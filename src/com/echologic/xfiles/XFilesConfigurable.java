/*
 * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
 *
 * license to be determined.
 */
package com.echologic.xfiles;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;

/**
 * This configuration editor panel is modeled after the JUnit run configuration editor.
 *
 * @author <a href="mailto:derek@echologic.com">Derek Scherger</a>
 */
public class XFilesConfigurable implements Configurable, ProjectComponent {

    private Logger log = Logger.getInstance(getClass().getName());

    private static final String MATCH_ALL = "all";
    private static final String MATCH_ANY = "any";

    private Project project;

    private XFilesConfiguration configuration;

    private JPanel panel;

    private DefaultListModel filterListModel;
    private JList filterList;

    private DefaultListModel testListModel;
    private JList testList;

    private JTextField nameField;
    private PathTable pathTable;
    private ConfigurationTable attributeTable;
    private ConfigurationTable statusTable;
    private ConfigurationTable typeTable;
    private ConfigurationTable vcsTable;
    private ConfigurationTable moduleTable;

    private JRadioButton matchAll = new JRadioButton("selections on all tabs");
    private JRadioButton matchAny = new JRadioButton("selections on any tab");

    private CountingFilterListener counts;

    private ActionToolbar actions;
    private CommandAction add = new CommandAction("Add", "Add Filter", XFilesIcons.ADD_ICON);
    private CommandAction remove = new CommandAction("Remove", "Remove Filter", XFilesIcons.REMOVE_ICON);
    private CommandAction copy = new CommandAction("Copy", "Copy Filter", XFilesIcons.COPY_ICON);
    private CommandAction moveUp = new CommandAction("Move Up", "Move Filter Up", XFilesIcons.UP_ICON);
    private CommandAction moveDown = new CommandAction("Move Down", "Move Filter Down", XFilesIcons.DOWN_ICON);

    private ActionToolbar pathActions;
    private CommandAction addPattern = new CommandAction("Add", "Add Pattern", XFilesIcons.ADD_ICON);
    private CommandAction removePattern = new CommandAction("Remove", "Remove Pattern", XFilesIcons.REMOVE_ICON);


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

    private Command addPatternCommand = new Command() {
        public void execute() {
            addPattern();
        }
    };

    private Command removePatternCommand = new Command() {
        public void execute() {
            removePattern();
        }
    };

    private ListSelectionListener selectionListener = new ListSelectionListener() {

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

                    nameField.setText(model.name);
                    matchAll.setSelected(model.matchAll);
                    matchAny.setSelected(!model.matchAll);

                    pathTable.setModel(model.pathModel);
                    attributeTable.setModel(model.attributeModel);
                    statusTable.setModel(model.statusModel);
                    typeTable.setModel(model.typeModel);
                    vcsTable.setModel(model.vcsModel);
                    moduleTable.setModel(model.moduleModel);
                }
            }
        }

    };

    private FocusListener nameListener = new FocusListener() {
        public void focusGained(FocusEvent e) {}

        public void focusLost(FocusEvent e) {
            int selected = filterList.getSelectedIndex();

            if (selected >= 0) {
                ConfigurableFilterModel model = (ConfigurableFilterModel) filterListModel.getElementAt(selected);
                model.name = nameField.getText();
            }
        }

    };

    private ActionListener matchButtonListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            ConfigurableFilterModel model = null;

            int selected = filterList.getSelectedIndex();

            if (selected >= 0) {
                model = (ConfigurableFilterModel) filterListModel.getElementAt(selected);
                if (e.getActionCommand().equals(MATCH_ALL)) {
                    model.matchAll = true;
                } else {
                    model.matchAll = false;
                }
            }

        }
    };

    public XFilesConfigurable(Project project) {
        this.project = project;

        matchAll.setActionCommand(MATCH_ALL);
        matchAny.setActionCommand(MATCH_ANY);

        ActionManager actionManager = ActionManager.getInstance();

        add.setEnabled(true);
        add.setCommand(addCommand);
        remove.setCommand(removeCommand);
        copy.setCommand(copyCommand);
        moveUp.setCommand(moveUpCommand);
        moveDown.setCommand(moveDownCommand);

        addPattern.setEnabled(true);
        removePattern.setEnabled(true);
        
        addPattern.setCommand(addPatternCommand);
        removePattern.setCommand(removePatternCommand);

        DefaultActionGroup group = new DefaultActionGroup("xfiles configuration group", false);
        group.add(add);
        group.add(remove);
        group.add(copy);
        group.add(moveUp);
        group.add(moveDown);

        actions = actionManager.createActionToolbar("XFilesConfigurationToolbar", group, true);

        DefaultActionGroup patternGroup = new DefaultActionGroup();
        patternGroup.add(addPattern);
        patternGroup.add(removePattern);

        pathActions = actionManager.createActionToolbar("XFilesPatternToolbar", patternGroup, false);
    }

    // Configurable methods

    public String getDisplayName() {
        return "XFiles";
    }

    public Icon getIcon() {
        return XFilesIcons.XFILES_ICON;
    }

    public String getHelpTopic() {
        return null;
    }

    /**
     * Create the configuration component.
     */
    public JComponent createComponent() {
        panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();

        JComponent toolbar = actions.getComponent();

        filterListModel = new DefaultListModel();
        filterList = new JList(filterListModel);

        testListModel = new DefaultListModel();
        testList = new JList(testListModel);

        pathTable = new PathTable();
        attributeTable = new ConfigurationTable();
        nameField = new JTextField();
        statusTable = new ConfigurationTable();
        typeTable = new ConfigurationTable();
        vcsTable = new ConfigurationTable();
        moduleTable = new ConfigurationTable();

        ListSelectionModel selection = filterList.getSelectionModel();
        selection.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        selection.addListSelectionListener(selectionListener);

        nameField.addFocusListener(nameListener);

        matchAll.addActionListener(matchButtonListener);
        matchAny.addActionListener(matchButtonListener);

        ///////////////////////////

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.fill = GridBagConstraints.BOTH;

        panel.add(toolbar, constraints);

        constraints.gridy = 1;
        constraints.weighty = 10.0;
        constraints.gridheight = 2;

        panel.add(new ListScrollPane(filterList), constraints);

        ///////////////////////////

        JLabel nameLabel = new JLabel("Name:");

        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.gridheight = 1;

        panel.add(nameLabel, constraints);

        constraints.gridx = 2;
        constraints.weightx = 10.0;

        panel.add(nameField, constraints);

        JLabel matchLabel = new JLabel("Match:");

        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;

        panel.add(matchLabel, constraints);

        constraints.gridx = 2;
        constraints.weightx = 10.0;

        ButtonGroup matchButtons = new ButtonGroup();
        matchButtons.add(matchAll);
        matchButtons.add(matchAny);

        FlowLayout layout = new FlowLayout(FlowLayout.LEADING);
        JPanel buttonPanel = new JPanel(layout);
        buttonPanel.add(matchAll);
        buttonPanel.add(matchAny);

        panel.add(buttonPanel, constraints);

        JPanel pathPanel = new JPanel();
        pathPanel.setLayout(new BorderLayout());
        pathPanel.add(pathActions.getComponent(), BorderLayout.WEST);
        pathPanel.add(new TableScrollPane(pathTable), BorderLayout.CENTER);

        final JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        tabs.add("path", pathPanel);
        tabs.add("attribute", new TableScrollPane(attributeTable));
        tabs.add("status", new TableScrollPane(statusTable));
        tabs.add("type", new TableScrollPane(typeTable));
        tabs.add("vcs", new TableScrollPane(vcsTable));
        tabs.add("module", new TableScrollPane(moduleTable));

        constraints.gridy = 2;
        constraints.weightx = 10.0;
        constraints.weighty = 10.0;

        panel.add(tabs, constraints);

        ///////////////////////////

        /*
        JButton testButton = new JButton("Test Filter");

        constraints.gridx = 3;
        constraints.gridy = 0;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;

        panel.add(testButton, constraints);

        constraints.gridy = 1;
        constraints.weighty = 10.0;
        constraints.gridheight = 2;

        panel.add(new ListScrollPane(testList), constraints);
        */

        return panel;
    }


    /**
     * Compare the configuration values against those in the configuration editor.
     *
     * This method must return true when the configuration has changed to enable the apply button
     * on the configuration panel.
     *
     * @return true if the configuration has changed
     */
    public boolean isModified() {
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
     * Apply (store) values to the saved configuration.
     */
    public void apply() {
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
     * Reset (load) values from those in the saved configuration.
     */
    public void reset() {
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

        // TODO: initially select the currently active filter?

        if (!filterListModel.isEmpty())
            filterList.setSelectedIndex(0);
    }

    public void disposeUIResources() {
        // TODO: release all resources
        panel = null;
    }

    ////////////////////////////////


    // ProjectComponent methods

    public String getComponentName() {
        return "XFilesConfigurable";
    }

    public void initComponent() {
    }

    public void disposeComponent() {
    }

    public void projectOpened() {
        configuration = (XFilesConfiguration) project.getComponent(XFilesConfiguration.class);
    }

    public void projectClosed() {
        configuration = null;
    }

    ////////////////////////////////

    private void addFilter() {
        ConfigurableFilterModel filter = new ConfigurableFilterModel(counts);
        filter.name = "<unnamed>";
        int index = filterList.getSelectedIndex()+1;
        filterListModel.add(index, filter);
        filterList.setSelectedIndex(index);
    }

    private void removeFilter() {
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

    private void copyFilter() {
        int index = filterList.getSelectedIndex();
        if (index >= 0) {
            ConfigurableFilterModel filter = (ConfigurableFilterModel) filterListModel.getElementAt(index);
            filter = new ConfigurableFilterModel(filter);
            filter.name = "<unnamed>";
            filterListModel.add(index, filter);
            filterList.setSelectedIndex(index);

            // TODO: there seemed to be a bug here with copied patterns being shared
        }
    }

    private void swap(int index1, int index2) {
        Object element1 = filterListModel.getElementAt(index1);
        Object element2 = filterListModel.getElementAt(index2);

        filterListModel.setElementAt(element1, index2);
        filterListModel.setElementAt(element2, index1);

        filterList.setSelectedIndex(index2);
    }

    private void moveFilterUp() {
        int index = filterList.getSelectedIndex();
        swap(index, index - 1);
    }

    private void moveFilterDown() {
        int index = filterList.getSelectedIndex();
        swap(index, index + 1);
    }

    ////////////////////////////////

    private void addPattern() {
        DefaultTableModel model = (DefaultTableModel) pathTable.getModel();
        int row = pathTable.getSelectedRow()+1;
        model.insertRow(row, (String[]) null);
        pathTable.getSelectionModel().setSelectionInterval(row, row);
    }

    private void removePattern() {
        DefaultTableModel model = (DefaultTableModel) pathTable.getModel();
        int row = pathTable.getSelectedRow();
        if (row >= 0) model.removeRow(row);
        if (row >= model.getRowCount()) row = model.getRowCount() - 1;
        if (row >= 0) pathTable.getSelectionModel().setSelectionInterval(row, row);
    }

    ////////////////////////////////

    private boolean equals(String name, Object a, Object b) {
        if (a.equals(b)) return true;
        log.debug(name + " differs");
        log.debug(a.toString());
        log.debug(b.toString());
        return false;
    }

    private boolean equals(XFilesFilterConfiguration externalizable, ConfigurableFilterModel configurable) {
        if (!equals("name", externalizable.NAME, configurable.name)) return false;
        if (!equals("match", externalizable.MATCH_ALL, configurable.matchAll)) return false;

        if (!equals("path", externalizable.ACCEPTED_PATH_NAMES, configurable.pathModel.getSelectedItems())) return false;
        if (!equals("attribute", externalizable.ACCEPTED_ATTRIBUTE_NAMES, configurable.attributeModel.getSelectedItems())) return false;
        if (!equals("status", externalizable.ACCEPTED_STATUS_NAMES,  configurable.statusModel.getSelectedItems())) return false;
        if (!equals("type", externalizable.ACCEPTED_TYPE_NAMES, configurable.typeModel.getSelectedItems())) return false;
        if (!equals("vcs", externalizable.ACCEPTED_VCS_NAMES, configurable.vcsModel.getSelectedItems())) return false;
        if (!equals("module", externalizable.ACCEPTED_MODULE_NAMES, configurable.moduleModel.getSelectedItems())) return false;

        // TODO: it would be helpful to have a list of the various lists

        return true;
    }

    // RANDOM THOUGHT
    // - ExternalizableFilterConfiguration
    // - ConfigurableFilterConfiguration
    // both extending FilterConfiguration which implements .equals()

    private class ConfigurationItem implements Comparable {
        private Boolean included;
        private String text;
        private Integer count;

        public ConfigurationItem(boolean included, String text, int count) {
            this.included = Boolean.valueOf(included);
            this.text = text;
            this.count = new Integer(count);
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

        // TODO: set column widths more appropriately

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

    private class PathTableModel extends DefaultTableModel {

        private String name;

        public PathTableModel() {
            super(0, 1);
        }

        public PathTableModel(PathTableModel that) {
            this();
            this.name = that.name;
            this.dataVector = that.dataVector;
            this.columnIdentifiers = that.columnIdentifiers;
        }

        /**
         * specify the name of items in this model
         * i.e. vcs, status, type, etc.
         *
         * @param map
         */
        public PathTableModel(VirtualFileCounterMap map) {
            this.name = map.getName();
        }

        public String getName() {
            return name;
        }

        public int getColumnCount() {
            return 1;
        }

        public String getColumnName(int column) {
            return "path glob";
        }

        public Class getColumnClass(int column) {
            return String.class;
        }

        /**
         * Get the non-empty patterns from the path table.
         * TODO: the patterns should be compiled and validated as they are entered
         * and invalid patterns should be disallowed somehow
         */
        public List getSelectedItems() {
            int size = getRowCount();
            List selected = new ArrayList(size);
            for (int i=0; i<size; i++) {
                String item = (String) getValueAt(i, 0);
                if (item != null) {
                    item = item.trim();
                    if (item.length() > 0) selected.add(item);
                }
            }
            return selected;
        }

        public void setSelectedItems(List selected) {
            int size = selected.size();
            setRowCount(size);
            for (int i=0; i<size; i++) {
                setValueAt(selected.get(i), i, 0);
            }
        }
    }

    private class ConfigurationTable extends JTable {
        public ConfigurationTable() {
            super(new ConfigurationTableModel());
            setAutoResizeMode(AUTO_RESIZE_ALL_COLUMNS);
            getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        }
    }

    private class PathTable extends JTable {
        public PathTable() {
            super(new PathTableModel());
            setAutoResizeMode(AUTO_RESIZE_ALL_COLUMNS);
            getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        }
    }

    private class TableScrollPane extends JScrollPane {
        public TableScrollPane(JTable table) {
            super(table, VERTICAL_SCROLLBAR_ALWAYS, HORIZONTAL_SCROLLBAR_NEVER);
        }
    }

    private class ListScrollPane extends JScrollPane {
        public ListScrollPane(JList list) {
            super(list, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);
        }
    }

    /**
     * This class holds entries in the list of configurable filters that's being edited
     */
    private class ConfigurableFilterModel {

        private String name;
        private boolean matchAll;

        // TODO: we may want to hold these in an array
        private PathTableModel pathModel;
        private ConfigurationTableModel attributeModel;
        private ConfigurationTableModel statusModel;
        private ConfigurationTableModel typeModel;
        private ConfigurationTableModel vcsModel;
        private ConfigurationTableModel moduleModel;

        /**
         * Copy constructor
         *
         * @param that
         */
        public ConfigurableFilterModel(ConfigurableFilterModel that) {
            this.name = that.name;
            this.matchAll = that.matchAll;

            this.pathModel = new PathTableModel(that.pathModel);
            this.attributeModel = new ConfigurationTableModel(that.attributeModel);
            this.statusModel = new ConfigurationTableModel(that.statusModel);
            this.typeModel = new ConfigurationTableModel(that.typeModel);
            this.vcsModel = new ConfigurationTableModel(that.vcsModel);
            this.moduleModel = new ConfigurationTableModel(that.moduleModel);
        }

        public ConfigurableFilterModel(CountingFilterListener listener) {
            pathModel = new PathTableModel(listener.getPathMap());
            matchAll = false;

            attributeModel = new ConfigurationTableModel(listener.getAttributeMap());
            statusModel = new ConfigurationTableModel(listener.getStatusMap());
            typeModel = new ConfigurationTableModel(listener.getTypeMap());
            vcsModel = new ConfigurationTableModel(listener.getVcsMap());
            moduleModel = new ConfigurationTableModel(listener.getModuleMap());
        }

        public void reset(XFilesFilterConfiguration configuration) {
            name = configuration.NAME;
            matchAll = configuration.MATCH_ALL;

            pathModel.setSelectedItems(configuration.ACCEPTED_PATH_NAMES);
            attributeModel.setSelectedItems(configuration.ACCEPTED_ATTRIBUTE_NAMES);
            statusModel.setSelectedItems(configuration.ACCEPTED_STATUS_NAMES);
            typeModel.setSelectedItems(configuration.ACCEPTED_TYPE_NAMES);
            vcsModel.setSelectedItems(configuration.ACCEPTED_VCS_NAMES);
            moduleModel.setSelectedItems(configuration.ACCEPTED_MODULE_NAMES);
        }

        public void apply(XFilesFilterConfiguration configuration) {
            log.debug("saving filter " + name);
            configuration.log();
            configuration.NAME = name;
            configuration.MATCH_ALL = matchAll;

            configuration.ACCEPTED_PATH_NAMES.clear();
            configuration.ACCEPTED_ATTRIBUTE_NAMES.clear();
            configuration.ACCEPTED_STATUS_NAMES.clear();
            configuration.ACCEPTED_TYPE_NAMES.clear();
            configuration.ACCEPTED_VCS_NAMES.clear();
            configuration.ACCEPTED_MODULE_NAMES.clear();

            configuration.ACCEPTED_PATH_NAMES.addAll(pathModel.getSelectedItems());
            configuration.ACCEPTED_ATTRIBUTE_NAMES.addAll(attributeModel.getSelectedItems());
            configuration.ACCEPTED_STATUS_NAMES.addAll(statusModel.getSelectedItems());
            configuration.ACCEPTED_TYPE_NAMES.addAll(typeModel.getSelectedItems());
            configuration.ACCEPTED_VCS_NAMES.addAll(vcsModel.getSelectedItems());
            configuration.ACCEPTED_MODULE_NAMES.addAll(moduleModel.getSelectedItems());
            configuration.log();
        }

        public String toString() {
            return name;
        }
    }

}
