package com.panamera.registry.node;

public abstract class AbstractNode {
	/**
	 * 
	 */
	private String nodeName;
	/**
	 * 
	 */
	private String nodePath;

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public String getNodePath() {
		return nodePath;
	}

	public void setNodePath(String nodePath) {
		this.nodePath = nodePath;
	}

}
