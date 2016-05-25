/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.managedswitch;

import org.openhab.core.binding.BindingProvider;

/**
 * @author robert@savagehomeautomation.com
 * @since 0.1-SNAPSHOT
 */
public interface ManagedSwitchBindingProvider extends BindingProvider {
    boolean providesBindingForManagedItem(String itemName);
    boolean providesBindingForManagedTarget(String targetItemName);
    ManagedSwitchBindingConfig getItemConfig(String itemName);
    String getItemNameFromTarget(String targetItemName);
    String getTarget(String itemName);
    int getTimeout(String itemName);
}
