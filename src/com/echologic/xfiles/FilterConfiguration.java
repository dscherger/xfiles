/*
 * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
 *
 * license to be determined.
 */
package com.echologic.xfiles;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vcs.FileStatus;

/**
 * wondering about two classes for configuring a new filter
 * - a wide-open prototype filter that selects every file
 * - the current configured filter that selects some files
 *
 * Note that in the changed files case we want certain vcs statuses or non-vcs files
 *
 * examples
 * - changed files
 * - config files (*.properties *.xml)
 * - *Entry
 * - notes (daily log, selected other files, perhaps explicit includes and excludes)
 *
 * globlets
 * - plain word checked with startsWith
 * - word* also checked with startsWith
 * - *word checked with endsWith so that we can represent *Entry and not get *EntryFoo
 * - word1*word2 checked with startsWith(word1) and endsWith(word2)
 * - word1*word2*word3*word4 checked with startsWith(word1) and endsWith(word4) and indexOf(word2) >=0
 *   and indexOf(word3) > indexOf(word2)
 *
 * perhaps generating a regex from the glob would be easier!
 * glob chars: * (any chars)
 *             ? (any single char)
 *
 * what about logical combinations (and/or)?
 * i.e. *Transaction or *Entry and changed status
 *
 * - perhaps while configuring a filter provide a test button that runs the filter and
 *   lists the selected files to save hopping in and out of the config panel
 * - when a filter configuration panel is loaded it should run the test to list files and
 *   counts to select from
 *
 *
 * @author <a href="mailto:derek@echologic.com">Derek Scherger</a>
 */
public class FilterConfiguration {

    private FileType[] type;
    private FileStatus[] status;

    private String[] vcs; // names or null to include non-vcs files
    private String[] module;
    private Object[] classification; // source, test, java, text, ...

    private String[] glob; // or regex or globlet

}
