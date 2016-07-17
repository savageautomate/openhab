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

import java.util.Collection;

/**
 * @author robert@savagehomeautomation.com
 * @since 0.1-SNAPSHOT
 */
public interface ManagedSwitchBindingProvider extends BindingProvider {
    boolean providesBindingForManagedItem(String item);
    boolean providesBindingForManagedTarget(String target);
    boolean providesBindingForManagedSensor(String sensor);
    boolean providesBindingForManagedOnSensor(String sensor);
    boolean providesBindingForManagedOffSensor(String sensor);
    ManagedSwitchBindingConfig getItemConfig(String item);
    String getItemNameFromTarget(String target);
    String getTarget(String item);
    int getTimeout(String item);
    String[] getItemNamesFromOnSensor(String sensor);
    String[] getItemNamesFromOffSensor(String sensor);
}
