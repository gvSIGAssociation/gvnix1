/*
 * Copyright 2015 DiSiD Technologies S.L.L. All rights reserved.
 * 
 * Project  : DiSiD proof_toc 
 * SVN Id   : $Id$
 */
package org.gvnix.util.fancytree;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Defines the properties for an object that can be used as a tree node in a
 * tree plugin
 * 
 * @author gvNIX Team
 * @version 1.5.0
 * 
 * @see http://wwwendt.de/tech/fancytree/doc/jsdoc/global.html#NodeData
 */
@JsonInclude(Include.NON_NULL)
public class TreeNode {

    private Boolean active;
    private ArrayList<TreeNode> children = new ArrayList<TreeNode>();
    private String data;
    private Boolean expanded;
    private String extraClasses;
    private Boolean focus;
    private Boolean folder;
    private Boolean hideCheckbox;
    private String iconclass;
    private String key;
    private Boolean lazy;
    private Boolean selected;
    private String title;
    private String tooltip;
    private Boolean unselectable;

    public TreeNode(String key) {
        this.key = key;
        this.title = key;
    }

    public TreeNode(String key, Boolean folder) {
        this(key);
        this.folder = folder;
    }

    /**
     * Returns the identifier of a node
     * 
     * @return the identifier of a node
     */
    public String getKey() {
        return this.key;
    }

    /**
     * Sets a custom title for a node
     * 
     * @param txt
     */
    public void setTitle(String txt) {
        this.title = txt;
    }

    /**
     * Returns a String with the node title
     * 
     * @return a String with the node title
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Returns true if the node has any child nodes
     * 
     * @return true if the node has any child nodes
     */
    public boolean hasChildren() {
        return !children.isEmpty();
    }

    /**
     * Appends a child node into a node.
     * 
     * @param node
     */
    public boolean addChild(TreeNode node) {
        return children.add(node);
    }

    /**
     * Removes a child node from a node.
     * 
     * @param node
     */
    public boolean removeChild(TreeNode node) {
        return children.remove(node);
    }

    /**
     * Returns all child nodes from a node
     * 
     * @return all child nodes from a node
     */
    public ArrayList<TreeNode> getChildren() {
        return this.children;
    }

    /**
     * Returns a child node from a specific position in the children list
     * 
     * @param index
     * @return a child node from a specific position in the children list
     */
    public TreeNode getChildAt(int index) {
        return this.children.get(index);
    }

    /**
     * Returns the number of children in a node
     * 
     * @return the number of children in a node
     */
    public int getChildCount() {
        return this.children.size();
    }

    /**
     * Returns the position of a child element from specified node
     * 
     * @param node
     * @return the position of a child element from specified node (0 as first)
     *         <p>
     *         -1 if child element cannot be found
     */
    public int getIndex(TreeNode node) {
        for (TreeNode childNode : this.getChildren()) {
            if (childNode.equals(node)) {
                return children.indexOf(childNode);
            }
        }
        return -1;
    }

    /**
     * Returns if node is a folder node
     * 
     * @return true if node is folder type
     */
    public Boolean getFolder() {
        return this.folder;
    }

    /**
     * Returns if node has lazy loading enabled
     * 
     * @return true if a node has lazy loading enabled
     */
    public Boolean getLazy() {
        return lazy;
    }

    /**
     * Returns if node starts active when tree is created
     * 
     * @return true if node starts active when tree is created
     */
    public Boolean getActive() {
        return active;
    }

    /**
     * Sets if node will be active when create tree
     * 
     * @param active Active flag
     */
    public void setActive(Boolean active) {
        this.active = active;
    }

    /**
     * Returns set extra data from node
     * 
     * @return set extra data from node
     */
    public String getData() {
        return data;
    }

    /**
     * Add extra data in node for custom usages
     * 
     * @param data Extra data
     */
    public void setData(String data) {
        this.data = data;
    }

    /**
     * Returns if node will start expanded when tree is created
     * 
     * @return true if node will start expanded tree is created
     */
    public Boolean getExpanded() {
        return expanded;
    }

    /**
     * Auto expand this node when tree is created
     * 
     * @param expanded True to auto expand node
     */
    public void setExpanded(Boolean expanded) {
        this.expanded = expanded;
    }

    /**
     * Returns additional CSS classes added to node
     * 
     * @return additional CSS classes added to node
     */
    public String getExtraClasses() {
        return extraClasses;
    }

    /**
     * Add additional CSS classes to node
     * 
     * @param extraClasses
     */
    public void setExtraClasses(String extraClasses) {
        this.extraClasses = extraClasses;
    }

    /**
     * Returns if node has keyboard focus
     * 
     * @return true if node has keyboard focus
     */
    public Boolean getFocus() {
        return focus;
    }

    /**
     * Set keyboard focus to this node
     * 
     * @param focus
     */
    public void setFocus(Boolean focus) {
        this.focus = focus;
    }

    /**
     * Sets if a node is folder type
     * 
     * @param folder Folder type
     */
    public void setFolder(Boolean folder) {
        this.folder = folder;
    }

    /**
     * Returns if checkbox will be hidden on this node
     * 
     * @return true if hidden checkbox
     */
    public Boolean getHideCheckbox() {
        return hideCheckbox;
    }

    /**
     * Hides checkbox on this node
     * 
     * @param hideCheckbox
     */
    public void setHideCheckbox(Boolean hideCheckbox) {
        this.hideCheckbox = hideCheckbox;
    }

    /**
     * Gets custom icon class name of this node
     * 
     * @return custom icon name of this node
     */
    public String getIconclass() {
        return iconclass;
    }

    /**
     * Adds a custom icon class to this node
     * 
     * @param iconclass Icon class name
     */
    public void setIconclass(String iconclass) {
        this.iconclass = iconclass;
    }

    /**
     * Sets if node content is only loaded by demand, i.e. on first expansion
     * 
     * @param lazy True if only load by demand
     */
    public void setLazy(Boolean lazy) {
        this.lazy = lazy;
    }

    /**
     * Returns if this node is selected when tree is created
     * 
     * @return true if this node is selected when tree is created
     */
    public Boolean getSelected() {
        return selected;
    }

    /**
     * Sets if a node is selected when tree is created
     * 
     * @param selected
     */
    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    /**
     * Returns custom tooltip text of this node
     * 
     * @return custom tooltip text of this node
     */
    public String getTooltip() {
        return tooltip;
    }

    /**
     * Adds a custom tooltip text to this node
     * 
     * @param tooltip Tooltip text
     */
    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    /**
     * Returns if this node can't be selected by user
     * 
     * @return True if node can't be selected
     */
    public Boolean getUnselectable() {
        return unselectable;
    }

    /**
     * Makes this node unselectable by user
     * 
     * @param unselectable True to make unselectable
     */
    public void setUnselectable(Boolean unselectable) {
        this.unselectable = unselectable;
    }

}
