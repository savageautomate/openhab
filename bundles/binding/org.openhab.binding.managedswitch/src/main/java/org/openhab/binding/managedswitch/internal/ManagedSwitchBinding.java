/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.managedswitch.internal;

import java.util.*;

import org.openhab.binding.managedswitch.ManagedSwitchBindingConfig;
import org.openhab.binding.managedswitch.ManagedSwitchBindingProvider;

import org.apache.commons.lang.StringUtils;
import org.openhab.core.binding.AbstractActiveBinding;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implement this class if you are going create an actively polling service
 * like querying a Website/Device.
 *
 * @author robert@savagehomeautomation.com
 * @since 0.1-SNAPSHOT
 */
public class ManagedSwitchBinding extends AbstractActiveBinding<ManagedSwitchBindingProvider> {

	private static final Logger logger =
		LoggerFactory.getLogger(ManagedSwitchBinding.class);

    /**
	 * The BundleContext. This is only valid when the bundle is ACTIVE. It is set in the activate()
	 * method and must not be accessed anymore once the deactivate() method was called or before activate()
	 * was called.
	 */
	private BundleContext bundleContext;

    // Injected by the OSGi Container through the setItemRegistry and
    // unsetItemRegistry methods.
    private ItemRegistry itemRegistry;

	/**
	 * the refresh interval which is used to poll values from the ManagedSwitch
	 * server (optional, defaults to 60000ms)
	 */
	private long refreshInterval = 1000;


	public ManagedSwitchBinding() {
	}

    /**
     * Invoked by the OSGi Framework.
     *
     * This method is invoked by OSGi during the initialization of the MiOSBinding, so we have subsequent access to the
     * ItemRegistry (needed to get values from Items in openHAB)
     */
    public void setItemRegistry(ItemRegistry itemRegistry) {
        logger.debug("setItemRegistry: called");
        this.itemRegistry = itemRegistry;
    }

    /**
     * Invoked by the OSGi Framework.
     *
     * This method is invoked by OSGi during the initialization of the MiOSBinding, so we have subsequent access to the
     * ItemRegistry (needed to get values from Items in openHAB)
     */
    public void unsetItemRegistry(ItemRegistry itemRegistry) {
        logger.debug("unsetItemRegistry: called");
        this.itemRegistry = null;
    }

	/**
	 * Called by the SCR to activate the component with its configuration read from CAS
	 *
	 * @param bundleContext BundleContext of the Bundle that defines this component
	 * @param configuration Configuration properties for this component obtained from the ConfigAdmin service
	 */
	public void activate(final BundleContext bundleContext, final Map<String, Object> configuration) {
		this.bundleContext = bundleContext;

		// the configuration is guaranteed not to be null, because the component definition has the
		// configuration-policy set to require. If set to 'optional' then the configuration may be null


		// to override the default refresh interval one has to add a
		// parameter to openhab.cfg like <bindingName>:refresh=<intervalInMs>
		String refreshIntervalString = (String) configuration.get("refresh");
		if (StringUtils.isNotBlank(refreshIntervalString)) {
			refreshInterval = Long.parseLong(refreshIntervalString);
		}

		// read further config parameters here ...

		setProperlyConfigured(true);
	}

	/**
	 * Called by the SCR when the configuration of a binding has been changed through the ConfigAdmin service.
	 * @param configuration Updated configuration properties
	 */
	public void modified(final Map<String, Object> configuration) {
		// update the internal configuration accordingly
	}

	/**
	 * Called by the SCR to deactivate the component when either the configuration is removed or
	 * mandatory references are no longer satisfied or the component has simply been stopped.
	 * @param reason Reason code for the deactivation:<br>
	 * <ul>
	 * <li> 0 – Unspecified
     * <li> 1 – The component was disabled
     * <li> 2 – A reference became unsatisfied
     * <li> 3 – A configuration was changed
     * <li> 4 – A configuration was deleted
     * <li> 5 – The component was disposed
     * <li> 6 – The bundle was stopped
     * </ul>
	 */
	public void deactivate(final int reason) {
		this.bundleContext = null;
		// deallocate resources here that are no longer needed and
		// should be reset when activating this binding again
	}


	/**
	 * @{inheritDoc}
	 */
	@Override
	protected long getRefreshInterval() {
		return refreshInterval;
	}

	/**
	 * @{inheritDoc}
	 */
	@Override
	protected String getName() {
		return "ManagedSwitch Refresh Service";
	}

	/**
	 * @{inheritDoc}
	 */
	@Override
	protected void execute() {
		// the frequently executed code (polling) goes here ...
		//logger.warn(">>>>>>>>>>> [MANAGED SWITCH] >>>>>>>>>>>>>>> execute() method is called!");

        // iterate over all provider instances and then iterate over each named item in the provider
        for (ManagedSwitchBindingProvider provider : providers) {
            Collection<String> names = provider.getItemNames();
            for (String name : names) {
                // process the timeout timer tick for each provider named item
                processItemTimeout(provider, name);
            }
        }
	}

	/**
	 * @{inheritDoc}
	 */
	@Override
	protected void internalReceiveCommand(String itemName, Command command) {
//      logger.trace("internalReceiveCommand(itemname = {}, Command = {})", itemName, command.toString());
        logger.warn(">>>>>>>>>>> [MANAGED SWITCH] >>>>>>>>>>>>>>> internalReceiveCommand({},{}) is called!", itemName, command);

        // forward the "ON" | "OFF" command from the managed (virtual) switch to the physical target switch
        if(command instanceof DecimalType || command instanceof OnOffType){
            for (ManagedSwitchBindingProvider provider : providers) {
                if (provider.providesBindingForManagedItem(itemName)) {

                    // get item configuration by managed/virtual item name
                    final ManagedSwitchBindingConfig itemConfig = provider.getItemConfig(itemName);

                    boolean isCommandOn = false;
                    boolean isCommandOff = false;

                    if(command instanceof OnOffType) {
                        isCommandOn = OnOffType.ON.equals(command);
                        isCommandOff = OnOffType.OFF.equals(command);
                    }
                    if(command instanceof DecimalType) {
                        isCommandOn = ((DecimalType)command).intValue() > 0;
                        isCommandOff = ((DecimalType)command).intValue() <= 0;
                    }

                    // validate command enabled
                    if(isCommandOn && !itemConfig.isOnEnabled()){
                        logger.warn("The 'ON' command is not enabled on this managed item.");
                        continue;
                    }
                    else if(isCommandOff && !itemConfig.isOffEnabled()){
                        logger.warn("The 'OFF' command is not enabled on this managed item.");
                        continue;
                    }

                    // send update command to physical/target item
                    eventPublisher.sendCommand(itemConfig.getTarget(), command);

                    // instant update feedback for managed/virtual item
                    if(itemConfig.isInstantUpdateEnabled()) {
                        eventPublisher.postUpdate(itemName, (State)command);
                    }

                    // update item timeout
                    updateItemTimeout(provider, itemName, isCommandOn);

                    continue;
                }
            }
        }
	}

	/**
	 * @{inheritDoc}
	 */
	@Override
	protected void internalReceiveUpdate(String itemName, State newState) {
        logger.warn(">>>>>>>>>>> [MANAGED SWITCH] >>>>>>>>>>>>>>> internalReceiveUpdate({},{}) is called!", itemName, newState);

        // iterate over all the managed binding providers
        for (ManagedSwitchBindingProvider provider : providers) {

            // handle the "ON" and "OFF" updated states for "target" items
            if((newState instanceof DecimalType || newState instanceof OnOffType) && provider.providesBindingForManagedTarget(itemName)) {
                // get managed/virtual item name from target item
                String managedItemName = provider.getItemNameFromTarget(itemName);
                if (managedItemName != null) {

                    // get managed/virtual item by name from the item registry
                    Item managedItem = null;
                    try {
                        managedItem = itemRegistry.getItem(managedItemName);

                        // only proceed with update if the state has changed (ignore duplicate update notifications)
                        // NOTE: we only update on an actual change in state because polled "refresh" events
                        //       can send duplicate/same state info messages
                        if (!managedItem.getState().equals(newState)) {

                            boolean isStateOn = false;
                            if(newState instanceof DecimalType){
                                isStateOn = ((DecimalType)newState).intValue() > 0;

                                // publish updated state to the managed/virtual item
                                if(managedItem.getAcceptedDataTypes().contains(DecimalType.class)){
                                    eventPublisher.postUpdate(managedItemName, newState);
                                }
                                else if(managedItem.getAcceptedDataTypes().contains(OnOffType.class)){
                                    //logger.warn(">>>>>>>>>>> [MANAGED SWITCH] >>>>>>>>>>>>>>> internalReceiveUpdate({},{}) is called <CONVERT DECIMAL TO ON|OFF>!", itemName, newState);
                                    eventPublisher.postUpdate(managedItemName, (isStateOn) ? OnOffType.ON : OnOffType.OFF);
                                }
                            }
                            else if(newState instanceof OnOffType){
                                isStateOn = OnOffType.ON.equals(newState);

                                // publish updated state to the managed/virtual item
                                eventPublisher.postUpdate(managedItemName, newState);
                            }

                            // update item timeout
                            updateItemTimeout(provider, managedItemName, isStateOn);
                        }
                    } catch (ItemNotFoundException e) {
                        logger.error(e.getMessage());
                        continue;
                    }
                }
            }

            // handle the "OPEN" states for "sensor" items
            if(OpenClosedType.OPEN.equals(newState) && provider.providesBindingForManagedOnSensor(itemName)){
                // get managed/virtual item name from target item
                String[] managedItemNames = provider.getItemNamesFromOnSensor(itemName);
                for(String managedItemName : managedItemNames) {
                    // get item configuration by managed/virtual item name
                    ManagedSwitchBindingConfig itemConfig = provider.getItemConfig(managedItemName);
                    if(itemConfig != null) {
                        // only proceed with update if the state has changed (ignore duplicate update notifications)
                        if (itemConfig.isOnEnabled()) {
                            // publish updated state to the managed/virtual item
                            eventPublisher.sendCommand(managedItemName, OnOffType.ON);
                        } else {
                            // update item timeout if needed
                            updateItemTimeout(provider, managedItemName, true);
                        }
                    }
                }
            }

            // handle the "CLOSED" state for "sensor" items
            if(OpenClosedType.CLOSED.equals(newState) && provider.providesBindingForManagedOffSensor(itemName)){
                // get managed/virtual item name from target item
                String[] managedItemNames = provider.getItemNamesFromOffSensor(itemName);
                for(String managedItemName : managedItemNames) {
                    // get item configuration by managed/virtual item name
                    ManagedSwitchBindingConfig itemConfig = provider.getItemConfig(managedItemName);
                    if(itemConfig != null) {
                        if (itemConfig.isOffEnabled()) {
                            // publish updated state to the managed/virtual item
                            eventPublisher.sendCommand(managedItemName, OnOffType.OFF);
                        } else {
                            // update item timeout if needed
                            updateItemTimeout(provider, managedItemName, false);
                        }
                    }
                }
            }
        }
	}

    private void updateItemTimeout(ManagedSwitchBindingProvider provider, final String itemName, final boolean onState){

        // get item configuration by managed/virtual item name
        final ManagedSwitchBindingConfig itemConfig = provider.getItemConfig(itemName);
        if(itemConfig != null) {

            // cancel any existing timer
            if (!onState && itemConfig.hasRemaining()){
                logger.warn(">>>>>>>>>>> [CANCEL MANAGED TIMEOUT] >>>>>>>>>>>>>>> ({})", itemName);
                itemConfig.clearRemaining();
            }

            // set timeout if this item is in the ON state
            else if (onState && itemConfig.hasTimeout()) {

                logger.warn(">>>>>>>>>>> [STARTED MANAGED TIMEOUT] >>>>>>>>>>>>>>> ({})", itemName);

                // create new timer
                itemConfig.setRemaining(itemConfig.getTimeout());
            }
        }
    }

    private void processItemTimeout(ManagedSwitchBindingProvider provider, final String itemName) {

        // get item configuration by managed/virtual item name
        final ManagedSwitchBindingConfig itemConfig = provider.getItemConfig(itemName);
        if(itemConfig != null && itemConfig.hasTimeout() && itemConfig.hasRemaining()) {

            // get remaining time
            int remaining = itemConfig.getRemaining();

            // decrement remaining time
            remaining--;

            // process "OFF" execution if there is no more remaining time
            if(remaining <= 0){
                logger.warn(">>>>>>>>>>> [EXECUTING MANAGED TIMEOUT] {}", itemName);

                // send OFF command instruction to target item
                eventPublisher.sendCommand(itemConfig.getTarget(), OnOffType.OFF);

                // instant update feedback for managed/virtual item
                if (itemConfig.isInstantUpdateEnabled()) {
                    eventPublisher.postUpdate(itemName, OnOffType.OFF);
                }

                // clear remaining time
                itemConfig.clearRemaining();
            }
            else{
                // update remaining time
                itemConfig.setRemaining(remaining);
                logger.warn(">>>>>>>>>>> [MANAGED SWITCH] {} will timeout in {} seconds", itemName, remaining);
            }
        }
    }
}
