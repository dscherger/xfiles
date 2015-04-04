/*
 * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
 *
 * license to be determined.
 */
package com.echologic.xfiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.AbstractListModel;

import com.intellij.openapi.vfs.VirtualFile;

/**
 * @author <a href="mailto:derek@echologic.com">Derek Scherger</a>
 */
public class XFilesListModel extends AbstractListModel<VirtualFile> {

    /**
     * TODO: consider different sort orders
     * - alphabetic
     * - little endian class order (camel case order)
     * - hotspot order on number of change events recorded
     * - file size
     * - modification stamp
     */
    private VirtualFileComparator comparator = new VirtualFileComparator();
    private List<VirtualFile> files = new ArrayList<>();
    private int selected = -1;

    public int getSize() {
        return files.size();
    }

    public VirtualFile getElementAt(int index) {
        return files.get(index);
    }

    /**
     * Add the specified element to the model while maintaining the models sort
     * order as defined by Comparable on the elements. Currenly this sorts by file
     * type and then by name.
     *
     * @param file
     */
    public void addElement(VirtualFile file ) {
        // add element in sorted position
        int index = Collections.binarySearch(files, file, comparator);

        if (index < 0) {
            index = -index - 1;
            files.add(index, file);
            fireIntervalAdded(this, index, index);
            setSelected(index);
        }
    }

    public void removeElementAt(int index) {
        if (index >= 0) {
            files.remove(index);
            fireIntervalRemoved(this, index, index);
        }
    }

    /**
     * Removes all of the elements from this list.  The list will
     * be empty after this call returns (unless it throws an exception).
     */
    public void clear() {
        int index = files.size() - 1;
        files.clear();
        if (index >= 0) {
            fireIntervalRemoved(this, 0, index);
        }
    }

    public boolean contains(VirtualFile element) {
        return files.contains(element);
    }

    public int indexOf(VirtualFile element) {
        return files.indexOf(element);
    }

    public void setSelectedItem(VirtualFile element) {
        setSelected(files.indexOf(element));
    }

    /**
     * Set the selected item to the specified index. This method is used internally to
     * change selection from one index to another.
     *
     * @param index
     */
    private void setSelected(int index) {
        if (selected != index) {
            int unselected = selected;
            selected = index;
            fireContentsChanged(this, unselected, selected);
        }
    }
}
