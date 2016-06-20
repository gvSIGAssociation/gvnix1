/*
 * Copyright 2015 DiSiD Technologies S.L.L. All rights reserved.
 * 
 * Project  : DiSiD proof_toc 
 * SVN Id   : $Id$
 */
package org.gvnix.util.fancytree;

/**
 * Utility methods for tree node objects handling
 * 
 * @author gvNIX Team
 * @version 1.5.0
 */
public class TreeUtils {

    /**
     * Return if a node is the hidden root node of a tree
     * 
     * @param key Identifier of a node
     * 
     * @return true if node is root node
     */
    public boolean isRootNode(String key) {
        return "#".equals(key);
    }
}
