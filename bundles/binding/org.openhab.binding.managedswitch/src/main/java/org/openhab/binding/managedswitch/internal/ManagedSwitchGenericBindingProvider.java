/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.managedswitch.internal;

import org.openhab.binding.managedswitch.ManagedSwitchBindingConfig;
import org.openhab.binding.managedswitch.ManagedSwitchBindingProvider;
import org.openhab.core.binding.BindingConfig;
import org.openhab.core.items.Item;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.model.item.binding.AbstractGenericBindingProvider;
import org.openhab.model.item.binding.BindingConfigParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.List;


/**
 * This class is responsible for parsing the binding configuration.
 *
 * @author robert@savagehomeautomation.com
 * @since 0.1-SNAPSHOT
 */
public class ManagedSwitchGenericBindingProvider extends AbstractGenericBindingProvider implements ManagedSwitchBindingProvider {

	/**
	 * {@inheritDoc}
	 */
	public String getBindingType() {
		return "managedswitch";
	}

    private static final Logger logger =
            LoggerFactory.getLogger(ManagedSwitchGenericBindingProvider.class);

    private Map<String, String> targetBindingItems = new HashMap<>();
    private Set<String> onSensorBindingItems = new HashSet<>();
    private Set<String> offSensorBindingItems = new HashSet<>();

    @Override
    public void removeConfigurations(String context) {
        logger.warn(">>>>>>>>>>> [MANAGED SWITCH] >>>>>>>>>>>>>>> removeConfigurations() is called!");
        super.removeConfigurations(context);
        targetBindingItems.clear();
        onSensorBindingItems.clear();
        offSensorBindingItems.clear();
    }

    @Override
    public boolean providesBindingFor(String itemName) {
        if(providesBindingForManagedItem(itemName)){
            return true;
        }
        if(providesBindingForManagedTarget(itemName)){
            return true;
        }
        if(providesBindingForManagedSensor(itemName)){
            return true;
        }
        return false;
    }

    @Override
    public boolean providesBindingForManagedItem(String item) {
        return (this.getItemConfig(item) != null);
    }

    @Override
    public boolean providesBindingForManagedTarget(String target) {
        return targetBindingItems.containsKey(target);
    }

    @Override
    public boolean providesBindingForManagedSensor(String sensor) {
        if(providesBindingForManagedOnSensor(sensor)){
            return true;
        }
        return providesBindingForManagedOffSensor(sensor);
    }

    @Override
    public boolean providesBindingForManagedOnSensor(String sensor) {
        return onSensorBindingItems.contains(sensor);
    }

    @Override
    public boolean providesBindingForManagedOffSensor(String sensor) {
        return offSensorBindingItems.contains(sensor);
    }

    /**
	 * @{inheritDoc}
	 */
	@Override
	public void validateItemType(Item item, String bindingConfig) throws BindingConfigParseException {
        //if (!(item instanceof SwitchItem || item instanceof DimmerItem)) {
        if (!(item instanceof SwitchItem)) {
			throw new BindingConfigParseException("item '" + item.getName()
					+ "' is of type '" + item.getClass().getSimpleName()
					+ "', only SwitchItems are allowed - please check your *.items configuration");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void processBindingConfiguration(String context, Item item, String bindingConfig) throws BindingConfigParseException {
		super.processBindingConfiguration(context, item, bindingConfig);
        logger.warn(">>>>>>>>>>> [MANAGED SWITCH] >>>>>>>>>>>>>>> processBindingConfiguration({},{},{}) is called!", context, item.getName(), bindingConfig);

        // validate segments, there should only be 1 or 2 segments
        String[] segments = bindingConfig.split(":");  // "<target-item>(:<optional-arguments>,..)"
        if (segments.length < 1 || segments.length > 2) {
            throw new BindingConfigParseException("Invalid number of segments in binding: " + bindingConfig);
        }

        // craete an item configuration object for the targeted item
        String target = segments[0];
        ManagedSwitchBindingConfig config = new ManagedSwitchBindingConfig(target);

        // continue parsing optional target item configuration
        if(segments.length > 1) {
            String configData = segments[1];
            String[] optionals = configData.split(",");
            for(String optional : optionals){
                if(optional.contains("=")){
                    String[] optional_parts = optional.split("=", 2);
                    String key = optional_parts[0];
                    String value = optional_parts[1];
                    String[] multivalue = value.split("\\+");

                    // handle "timeout" argument
                    if(key.equals("timeout")){
                        config.setTimeout(Integer.parseInt(value));
                    }

                    // handle "on" argument
                    else if(key.equals("on_enabled")){
                        config.setOnEnabled(Boolean.parseBoolean(value));
                    }

                    // handle "off" argument
                    else if(key.equals("off_enabled")){
                        config.setOffEnabled(Boolean.parseBoolean(value));
                    }

                    // handle "instant_update" argument
                    else if(key.equals("instant_update")){
                        config.setInstantUpdateEnabled(Boolean.parseBoolean(value));
                    }

                    // handle "sensor" argument (supports multiple values)
                    else if(key.equals("sensor")){
                        for (String val : multivalue) {
                            config.addSensor(val);
                        }
                    }

                    // handle "on_sensor" argument (supports multiple values)
                    else if(key.equals("on_sensor")){
                        for (String val : multivalue) {
                            config.addOnSensor(val);
                        }
                    }

                    // handle "off_sensor" argument (supports multiple values)
                    else if(key.equals("off_sensor")){
                        for (String val : multivalue) {
                            config.addOffSensor(val);
                        }
                    }
                }
            }
        }

        // add binding config now
        addBindingConfig(item, config);
	}

    @Override
    protected void addBindingConfig(Item item, BindingConfig config) {
        // add  target
        String target = ((ManagedSwitchBindingConfig)config).getTarget();
        if(target != null) {
            targetBindingItems.put(target, item.getName());
        }

        // add "ON" sensors
        Collection<String> onSensors = ((ManagedSwitchBindingConfig)config).getOnSensors();
        for(String sensor : onSensors) {
            if (sensor != null && !onSensorBindingItems.contains(sensor)) {
                logger.warn(">>>>>>>>>>> [MANAGED SWITCH] >>>>>>>>>>>>>>> ON SENSOR: {}", sensor);
                onSensorBindingItems.add(sensor);
            }
        }

        // add "OFF" sensors
        Collection<String> offSensors = ((ManagedSwitchBindingConfig)config).getOffSensors();
        for(String sensor : offSensors) {
            if (sensor != null && !offSensorBindingItems.contains(sensor)) {
                logger.warn(">>>>>>>>>>> [MANAGED SWITCH] >>>>>>>>>>>>>>> OFF SENSOR: {}", sensor);
                offSensorBindingItems.add(sensor);
            }
        }

        super.addBindingConfig(item, config);
    }

    @Override
    public ManagedSwitchBindingConfig getItemConfig(String itemName){
        return ((ManagedSwitchBindingConfig)this.bindingConfigs.get(itemName));
    }

    @Override
    public String getTarget(String itemName) {
        ManagedSwitchBindingConfig config = getItemConfig(itemName);
        if(config != null) {
            return config.getTarget();
        }
        return null;
    }

    @Override
    public int getTimeout(String itemName) {
        ManagedSwitchBindingConfig config = getItemConfig(itemName);
        if(config != null) {
            return config.getTimeout();
        }
        return 0;
    }

    @Override
    public String getItemNameFromTarget(String target) {
        if(this.targetBindingItems.containsKey(target)) {
            return this.targetBindingItems.get(target);
        }
        return null;
    }

    @Override
    public String[] getItemNamesFromOnSensor(String sensor){
        if(this.onSensorBindingItems.contains(sensor)) {
            Set<String> itemsNames = new HashSet<>();
            for(String itemName : getItemNames()){
                if(getItemConfig(itemName).hasOnSensor(sensor)){
                    itemsNames.add(itemName);
                }
            }
            return itemsNames.toArray(new String[0]);
        }
        return null;
    }

    @Override
    public String[] getItemNamesFromOffSensor(String sensor){
        if(this.offSensorBindingItems.contains(sensor)) {
            Set<String> itemsNames = new HashSet<>();
            for(String itemName : getItemNames()){
                if(getItemConfig(itemName).hasOffSensor(sensor)){
                    itemsNames.add(itemName);
                }
            }
            return itemsNames.toArray(new String[0]);
        }
        return null;
    }
}
