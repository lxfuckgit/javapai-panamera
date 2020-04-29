package com.panamera.registry;

import com.panamera.registry.node.INode;

/**
 * registry every things.<br>
 * 
 * @author lx
 *
 */
public interface XRegistry extends XService {
	/**
	 * 
	 * @param node
	 */
	public void register(INode node);
}
