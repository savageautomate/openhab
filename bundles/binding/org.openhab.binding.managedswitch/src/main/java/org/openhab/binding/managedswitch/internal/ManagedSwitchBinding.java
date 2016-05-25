/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.managedswitch.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.openhab.binding.managedswitch.ManagedSwitchBindingConfig;
import org.openhab.binding.managedswitch.ManagedSwitchBindingProvider;

import org.apache.commons.lang.StringUtils;
import org.openhab.core.binding.AbstractActiveBinding;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.library.types.OnOffType;
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

    Map<String, Timer> timeoutTimers = new HashMap<>();

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
	private long refreshInterval = 60000;


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
	}

	/**
	 * @{inheritDoc}
	 */
	@Override
	protected void internalReceiveCommand(String itemName, Command command) {
//      logger.trace("internalReceiveCommand(itemname = {}, Command = {})", itemName, command.toString());
        logger.warn(">>>>>>>>>>> [MANAGED SWITCH] >>>>>>>>>>>>>>> internalReceiveCommand({},{}) is called!", itemName, command);

        // forward the "ON" | "OFF" command from the managed (virtual) switch to the physical target switch
        if(OnOffType.OFF.equals(command) || OnOffType.ON.equals(command)){
            for (ManagedSwitchBindingProvider provider : providers) {
                if (provider.providesBindingForManagedItem(itemName)) {

                    // get item configuration by managed/virtual item name
                    final ManagedSwitchBindingConfig itemConfig = provider.getItemConfig(itemName);

                    // validate command enabled
                    if(OnOffType.ON.equals(command) && !itemConfig.isOnEnabled()){
                        logger.warn("The 'ON' command is not enabled on this managed item.");
                        continue;
                    }
                    else if(OnOffType.OFF.equals(command) && !itemConfig.isOffEnabled()){
                        logger.warn("The 'OFF' command is not enabled on this managed item.");
                        continue;
                    }

                    // send update command to physical/target item
                    eventPublisher.sendCommand(itemConfig.getTarget(), command);

                    // instant update feedback for managed/virtual item
                    if(itemConfig.isInstantUpdateEnabled()) {
                        eventPublisher.postUpdate(itemName, OnOffType.ON.equals(command) ? OnOffType.ON : OnOffType.OFF);
                    }

                    // update item timeout
                    updateItemTimeout(provider, itemName, OnOffType.ON.equals(command));

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

        // handle the "ON" and "OFF" updated states
        if(OnOffType.OFF.equals(newState) || OnOffType.ON.equals(newState)){
            for (ManagedSwitchBindingProvider provider : providers) {

                // determine if this update is for a managed target item
                if (provider.providesBindingForManagedTarget(itemName)) {

                    // the item name received in this method represents the target item
                    final String targetItemName = itemName;

                    // get managed/virtual item name from target item
                    final String managedItemName = provider.getItemNameFromTarget(targetItemName);
                    if(managedItemName != null) {

                        // get managed/virtual item by name from the item registry
                        Item managedItem = null;
                        try {
                            managedItem = itemRegistry.getItem(managedItemName);

                            // only proceed with update if the state has changed (ignore duplicate update notifications)
                            if(!managedItem.getState().equals(newState)){

                                // publish updated state to the managed/virtual item
                                eventPublisher.postUpdate(managedItemName, newState);

                                // update item timeout
                                updateItemTimeout(provider, managedItemName, OnOffType.ON.equals(newState));
                            }
                        } catch (ItemNotFoundException e) {
                            logger.error(e.getMessage());
                            continue;
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

            // get timer from mapped items
            Timer timer = null;
            if(!timeoutTimers.containsKey(itemName)){
                timer = new Timer();
                timeoutTimers.put(itemName, timer);
            } else{
                timer = timeoutTimers.get(itemName);
            }

            // cancel any existing timer
            if (timer != null) {
                timer.cancel();
                logger.warn(">>>>>>>>>>> [CANCEL MANAGED TIMEOUT] >>>>>>>>>>>>>>> ({})", itemName);
            }

            // set timeout if this item is in the ON state
            if (onState && itemConfig.hasTimeout()) {

                logger.warn(">>>>>>>>>>> [STARTED MANAGED TIMEOUT] >>>>>>>>>>>>>>> ({})", itemName);

                // create new timer
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        logger.warn(">>>>>>>>>>> [EXECUTING MANAGED TIMEOUT] {}", itemName);
                        // remove this timer from the mapped items
                        //timeoutTimers.remove(itemName);
                        Timer tmr = timeoutTimers.get(itemName);
                        tmr = null;

                        // send OFF command instruction to target item
                        eventPublisher.sendCommand(itemConfig.getTarget(), OnOffType.OFF);

                        // instant update feedback for managed/virtual item
                        if(itemConfig.isInstantUpdateEnabled()) {
                            eventPublisher.postUpdate(itemName, OnOffType.OFF);
                        }
                    }
                }, itemConfig.getTimeout() * 1000);
            }
        }
    }
}
