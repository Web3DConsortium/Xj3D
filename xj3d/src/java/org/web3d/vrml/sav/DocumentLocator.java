/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.sav;

/**
 * Callback into the parser to ask for information about where we are in the
 * parsing process.
 * <p>
 * If a parser supports Locators, it must at least support line numbers. Column
 * numbers are optional.
 */
public interface DocumentLocator {

    /**
     * Get the current column number at the end of the last processing event.
     * If column number support is not provided, this should always return -1.
     *
     * @return The column number of the last processing event
     */
    int getColumnNumber();

    /**
     * Get the current line number of the last event processing step. If the
     * last processing step takes more than one line, this is the first line
     * of the processing that called the callback event.
     *
     * @return The line number of the last processing step.
     */
    int getLineNumber();
}
