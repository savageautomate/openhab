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

import java.util.*;

/**
 * This is a helper class holding binding specific configuration details
 *
 * @author robert@savagehomeautomation.com
 * @since 0.1-SNAPSHOT
 */
public class ManagedSwitchBindingConfig implements BindingConfig {
    private String target;
    private int timeout;
    private int remaining;
    private boolean offEnabled = true;
    private boolean onEnabled = true;
    private boolean instantUpdateEnabled = false;
    private Set<String> on_sensors = new HashSet<>();
    private Set<String> off_sensors = new HashSet<>();

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

    public void clearRemaining(){
        this.remaining = 0;
    }
    public void setRemaining(int remaining){
        this.remaining = remaining;
    }
    public int getRemaining(){
        return this.remaining;
    }
    public boolean hasRemaining(){
        return this.remaining > 0;
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

    public void addSensor(String sensor) {
        addOnSensor(sensor);
        addOffSensor(sensor);
    }

    public Collection<String> getOnSensors() { return this.on_sensors; };
    public void addOnSensor(String sensor) {
        if(!this.on_sensors.contains(sensor)) {
            this.on_sensors.add(sensor);
        }
    }
    public boolean hasOnSensor(String sensor){
        return this.on_sensors.contains(sensor);
    }

    public Collection<String> getOffSensors() { return this.off_sensors; };
    public void addOffSensor(String sensor) {
        if(!this.off_sensors.contains(sensor)){
            this.off_sensors.add(sensor);
        }
    }
    public boolean hasOffSensor(String sensor){
        return this.off_sensors.contains(sensor);
    }
}
