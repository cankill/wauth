/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2013, Red Hat Inc, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package net.ffff.wauth;

import net.ffff.wauth.inflow.RadiusActivation;
import net.ffff.wauth.inflow.RadiusActivationSpec;

import javax.resource.ResourceException;
import javax.resource.spi.*;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkManager;
import javax.transaction.xa.XAResource;
import java.util.logging.Logger;

/**
 * RadiusResourceAdapter
 *
 * @version $Revision: $
 */
@Connector(
        displayName = "RadiusRA",
        vendorName = "0xffff.net",
        version = "1.0",
        transactionSupport = TransactionSupport.TransactionSupportLevel.NoTransaction,
        reauthenticationSupport = false
)
public class RadiusResourceAdapter implements ResourceAdapter, java.io.Serializable {

    /**
     * The serial version UID
     */
    private static final long serialVersionUID = 1L;

    /**
     * The logger
     */
    private static Logger log = Logger.getLogger(RadiusResourceAdapter.class.getName());

    private RadiusActivation activation;
    private WorkManager workManager;
    private Work worker;


    /**
     * Default constructor
     */
    public RadiusResourceAdapter() {

    }

    /**
     * This is called during the activation of a message endpoint.
     *
     * @param endpointFactory A message endpoint factory instance.
     * @param spec            An activation spec JavaBean instance.
     * @throws ResourceException generic exception
     */
    public void endpointActivation(final MessageEndpointFactory endpointFactory,
                                   final ActivationSpec spec) throws ResourceException {
        activation = new RadiusActivation(this, endpointFactory, (RadiusActivationSpec) spec);
        activation.start();

        workManager.scheduleWork(new Work() {
            @Override
            public void release() {

            }

            @Override
            public void run() {
                try {
                    MessageEndpoint endpoint = endpointFactory.createEndpoint(null);
                    worker = new Worker((RadiusActivationSpec) spec, endpoint, workManager);
                    workManager.scheduleWork(worker);
                } catch (UnavailableException e) {
                    e.printStackTrace();
                } catch (WorkException e) {
                    e.printStackTrace();
                }
            }
        });


        log.finest("endpointActivation()");

    }

    /**
     * This is called when a message endpoint is deactivated.
     *
     * @param endpointFactory A message endpoint factory instance.
     * @param spec            An activation spec JavaBean instance.
     */
    public void endpointDeactivation(MessageEndpointFactory endpointFactory,
                                     ActivationSpec spec) {
        if (activation != null)
            activation.stop();
        worker.release();

        log.finest("endpointDeactivation()");

    }

    /**
     * This is called when a resource adapter instance is bootstrapped.
     *
     * @param ctx A bootstrap context containing references
     * @throws ResourceAdapterInternalException indicates bootstrap failure.
     */
    public void start(BootstrapContext ctx)
            throws ResourceAdapterInternalException {
        log.finest("start()");
        workManager = ctx.getWorkManager();

    }

    /**
     * This is called when a resource adapter instance is undeployed or
     * during application server shutdown.
     */
    public void stop() {
        log.finest("stop()");

    }

    /**
     * This method is called by the application server during crash recovery.
     *
     * @param specs An array of ActivationSpec JavaBeans
     * @return An array of XAResource objects
     * @throws ResourceException generic exception
     */
    public XAResource[] getXAResources(ActivationSpec[] specs)
            throws ResourceException {
        log.finest("getXAResources()");
        return null;
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return A hash code value for this object.
     */
    @Override
    public int hashCode() {
        int result = 17;
        return result;
    }

    /**
     * Indicates whether some other object is equal to this one.
     *
     * @param other The reference object with which to compare.
     * @return true if this object is the same as the obj argument, false otherwise.
     */
    @Override
    public boolean equals(Object other) {
        if (other == null)
            return false;
        if (other == this)
            return true;
        if (!(other instanceof RadiusResourceAdapter))
            return false;
        boolean result = true;
        return result;
    }

}
