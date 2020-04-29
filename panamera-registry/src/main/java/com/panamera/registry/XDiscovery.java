package com.panamera.registry;

import java.util.List;

/**
 * discovery every things.<br>
 * 
 * @author lx
 *
 */
public interface XDiscovery extends XService {

	public <T> List<T> discovery();

}
