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
public class XFilesListModel extends AbstractListModel {

    /**
     * TODO: consider different sort orders
     * - alphabetic
     * - little endian class order
     * - hotspot order on number of change events recorded
     */
    private VirtualFileComparator comparator = new VirtualFileComparator();
    private List files = new ArrayList();
    private int selected = -1;

    public int getSize() {
        return files.size();
    }

    public Object getElementAt(int index) {
        return files.get(index);
    }

    /**
     * Add the specified element to the model while maintaining the models sort
     * order as defined by Comparable on the elements. Currenly this sorts by file
     * type and then by name.
     *
     * @param element
     */
    public void addElement(Object element) {
        VirtualFile file = (VirtualFile) element;

        // add element in sorted position
        int index = Collections.binarySearch(files, file, comparator);

        if (index < 0) {
            index = -index - 1;
            files.add(index, element);
            fireIntervalAdded(this, index, index);
            setSelected(index);
        }
    }

    /**
     * Remove the specified element from the model. Note that the change in selected
     * element is not explicitly handled here. This method is called on fileClosed events
     * which are followed closely by selectionChanged events. The latter appears to
     * handle selection updates sufficiently.
     *
     * @param element
     */
    public void removeElement(Object element) {
        int index = files.indexOf(element);
        removeElementAt(index);
    }

    /**
     * This method is unsupported in this implementation because it may violate the sort
     * order imposed by the addElement method and it is not required by the associated action.
     *
     * @param element
     * @param index
     */
    public void insertElementAt(Object element, int index) {
        throw new UnsupportedOperationException("insertElementAt");
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
	int index = files.size()-1;
	files.clear();
	if (index >= 0) {
	    fireIntervalRemoved(this, 0, index);
	}
    }

    public boolean contains(Object element) {
        return files.contains(element);
    }

    public int indexOf(Object element) {
        return files.indexOf(element);
    }

    public int size() {
        return files.size();
    }
    
    public void setSelectedItem(Object element) {
        setSelected(files.indexOf(element));
    }

    public Object getSelectedItem() {
        if (selected >= 0) {
            return files.get(selected);
        } else {
            return null;
        }
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
