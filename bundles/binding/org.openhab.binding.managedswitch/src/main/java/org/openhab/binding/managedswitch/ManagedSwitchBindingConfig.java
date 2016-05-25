/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.managedswitch;

import org.openhab.core.binding.BindingConfig;

/**
 * This is a helper class holding binding specific configuration details
 *
 * @author robert@savagehomeautomation.com
 * @since 0.1-SNAPSHOT
 */
public class ManagedSwitchBindingConfig implements BindingConfig {
    private String target;
    private int timeout;
    private boolean offEnabled = true;
    private boolean onEnabled = true;
    private boolean instantUpdateEnabled = false;

    public ManagedSwitchBindingConfig(String target){
        this.target = target;
    }

    public boolean isTarget(String target){
        return this.target.equals(target);
    }

    public String getTarget(){
        return this.target;
    }

    public void setTimeout(int timeout){
        this.timeout = timeout;
    }
    public int getTimeout(){
        return this.timeout;
    }
    public boolean hasTimeout(){
        return this.timeout > 0;
    }

    public void setOffEnabled(boolean enabled){
        this.offEnabled = enabled;
    }
    public boolean isOffEnabled(){
        return this.offEnabled;
    }

    public void setOnEnabled(boolean enabled){
        this.onEnabled = enabled;
    }
    public boolean isOnEnabled(){
        return this.onEnabled;
    }

    public void setInstantUpdateEnabled(boolean enabled){
        this.instantUpdateEnabled = enabled;
    }
    public boolean isInstantUpdateEnabled(){
        return this.instantUpdateEnabled;
    }

}
