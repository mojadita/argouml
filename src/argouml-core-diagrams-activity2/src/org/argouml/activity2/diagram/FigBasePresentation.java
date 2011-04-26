/* $Id: $
 *****************************************************************************
 * Copyright (c) 2010-2011 Contributors - see below
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Bob Tarling
 *****************************************************************************
 */

package org.argouml.activity2.diagram;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;

import org.argouml.notation2.NotationType;
import org.argouml.uml.diagram.DiagramSettings;
import org.tigris.gef.presentation.Fig;
import org.tigris.gef.presentation.FigGroup;

abstract class FigBasePresentation extends FigComposite
        implements StereotypeDisplayer, NameDisplayer {
    
    private final DiagramElement border;
    private final DiagramElement nameDisplay;
    private Rectangle bounds;
    private static final int PADDING = 2;
    private static final int MIN_WIDTH = 90;
    
    public FigBasePresentation(
            final Rectangle rect,
            final Color lineColor,
            final Color fillColor,
            final Object modelElement,
            final DiagramSettings settings) {
        nameDisplay = new FigNotation(
                modelElement,
                new Rectangle(0, 0, 0, 0),
                settings,
                NotationType.NAME);
        border = createBorder(rect, lineColor, fillColor);
        addFig((Fig) border);
        addFig((Fig) getNameDisplay());
        setBounds(rect);
    }
    
    public DiagramElement getStereotypeDisplay() {
        return null;
    }

    public DiagramElement getNameDisplay() {
        return nameDisplay;
    }

    abstract DiagramElement createBorder(
            final Rectangle rect,
            final Color lineColor,
            final Color fillColor);
    
    DiagramElement getBorder() {
        return border;
    }
    
    // TODO: Move an empty implementation to FigGroup in GEF
    protected void positionChildren() {
        Rectangle myBounds = getBounds();
        
        getBorder().setBounds(myBounds);
        
        final Dimension nameDim = getNameDisplay().getMinimumSize();
        final int nameWidth = nameDim.width;
        final int nameHeight = nameDim.height;
        
        final int nx = bounds.x + getLeftMargin()
            + (bounds.width - (nameWidth + getLeftMargin() + getRightMargin()))
            / 2;
        final int ny = bounds.y + getTopMargin()
            + (bounds.height - nameHeight - getTopMargin() - getBottomMargin())
            / 2;
        getNameDisplay().setLocation(nx, ny);
    }
    
    @Override
    public Dimension getMinimumSize() {
        
        final Dimension nameDim = getNameDisplay().getMinimumSize();
        int width = nameDim.width;
        int height = nameDim.height;
//        if (getStereotypeDisplay() != null) {
//            final Dimension stereoDim = getStereotypeDisplay().getMinimumSize();
//            width += Math.max(stereoDim.width, nameDim.width);
//            height += (stereoDim.height - 2);
//        }
        
        int w = width + getRightMargin() + getLeftMargin();
        final int h = height + getTopMargin() + getBottomMargin();
        w = Math.max(w, MIN_WIDTH);
        return new Dimension(w, h);
    }
    
    protected int getRightMargin() {
        return PADDING;
    }
    
    protected int getLeftMargin() {
        return PADDING;
    }
    
    protected int getTopMargin() {
        return PADDING;
    }
    
    protected int getBottomMargin() {
        return PADDING;
    }
    
    //
    // !! TODO: All code below here is duplicated in FigBaseNode. The reason
    // is the GEF defect - http://gef.tigris.org/issues/show_bug.cgi?id=358
    // Once we have taken a release of GEF with that fix we can remove this
    // code.
    //
    @Override
    protected void setBoundsImpl(
            final int x,
            final int y,
            final int w,
            final int h) {

        final int ww;
        // Refuse to set bounds below the minimum width
        final int minWidth = getMinimumSize().width;
        if (w < minWidth) {
            ww = minWidth;
        } else {
            ww = w;
        }
        final int hh;
        final int minHeight = getMinimumSize().height;
        if (h < minHeight) {
            hh = minHeight;
        } else {
            hh = h;
        }
        
        final Rectangle oldBounds = getBounds();
        bounds = new Rectangle(x, y, ww, hh);
        
        if (oldBounds.equals(bounds)) {
            return;
        }
        
        _x = x;
        _y = y;
        _w = ww;
        _h = hh;
        
        positionChildren();
        
        firePropChange("bounds", oldBounds, bounds);
    }
    
    protected Rectangle getBoundsImpl() {
        return bounds;
    }
    
    
    /**
     * Change the position of the object from where it is to where it is plus dx
     * and dy. Often called when an object is dragged. This could be very useful
     * if local-coordinate systems are used because deltas need less
     * transforming... maybe. Fires property "bounds".
     */
    protected void translateImpl(int dx, int dy) {
        if (dx ==0 || dy == 0) {
            return;
        }
        Rectangle oldBounds = getBounds();
        Rectangle newBounds = new Rectangle(
                oldBounds.x + dx,
                oldBounds.y + dy,
                oldBounds.width,
                oldBounds.height);
        setBounds(newBounds);
    }
    
    
    /**
     * This is called to rearrange the contents of the Fig when a childs
     * minimum size means it will no longer fit. If this group also has
     * a parent and it will no longer fit that parent then control is
     * delegated to that parent.
     */
    public void calcBounds() {
        final Dimension min = getMinimumSize();
        if (getGroup() != null
                && (getBounds().height < min.height
                        || getBounds().width < min.width)) {
            ((FigGroup) getGroup()).calcBounds();
        } else {
            int maxw = Math.max(getWidth(), min.width);
            int maxh = Math.max(getHeight(), min.height);
            setSize(maxw, maxh);
        }
    }
}