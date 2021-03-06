/**
 * Copyright (C) FuseSource, Inc.
 * http://fusesource.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fusesource.fabric.extender.listener;

import org.apache.curator.framework.CuratorFramework;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.References;
import org.fusesource.fabric.api.ModuleStatus;
import org.fusesource.fabric.api.jcip.ThreadSafe;
import org.fusesource.fabric.api.scr.AbstractComponent;
import org.fusesource.fabric.api.scr.ValidatingReference;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.springframework.osgi.context.event.OsgiBundleApplicationContextEvent;
import org.springframework.osgi.context.event.OsgiBundleApplicationContextListener;
import org.springframework.osgi.context.event.OsgiBundleContextClosedEvent;
import org.springframework.osgi.context.event.OsgiBundleContextFailedEvent;
import org.springframework.osgi.context.event.OsgiBundleContextRefreshedEvent;
import org.springframework.osgi.extender.event.BootstrappingDependencyEvent;
import org.springframework.osgi.service.importer.event.OsgiServiceDependencyEvent;
import org.springframework.osgi.service.importer.event.OsgiServiceDependencyWaitStartingEvent;

@ThreadSafe
@Component(name = "org.fusesource.fabric.extender.listener.spring", description = "Fabric Spring Application Listener", immediate = true)
@References({
    @Reference(referenceInterface = CuratorFramework.class, bind = "bindCurator", unbind = "unbindCurator")
})
public final class FabricSpringApplicationListener extends AbstractExtenderListener {

    private static final String EXTENDER_TYPE = "spring";

    private ServiceRegistration<?> registration;

    @Activate
    void activate(BundleContext bundleContext) {
        Object listener = createListener(bundleContext);
        if (listener != null) {
            registration = bundleContext.registerService(OsgiBundleApplicationContextListener.class.getName(), listener, null);
        }
        super.activate(bundleContext);
    }

    @Deactivate
    void deactivate(BundleContext bundleContext) {
        super.deactivate(bundleContext);
        if (registration != null) {
            registration.unregister();
        }
    }

    @Override
    protected String getExtenderType() {
        return EXTENDER_TYPE;
    }

    private Object createListener(BundleContext bundleContext) {
        try {
            // Classloading issues are ignored
            return new SpringApplicationListener();
        } catch (Throwable t) {
            return null;
        }
    }

    class SpringApplicationListener implements OsgiBundleApplicationContextListener {

        @Override
        public void onOsgiApplicationEvent(OsgiBundleApplicationContextEvent event) {
            long bundleId = event.getBundle().getBundleId();
            ModuleStatus moduleStatus = toModuleStatus(event);
            updateBundle(bundleId, moduleStatus);
        }

        private ModuleStatus toModuleStatus(OsgiBundleApplicationContextEvent event) {
            if (event instanceof BootstrappingDependencyEvent) {
                OsgiServiceDependencyEvent de = ((BootstrappingDependencyEvent) event).getDependencyEvent();
                if (de instanceof OsgiServiceDependencyWaitStartingEvent) {
                    return ModuleStatus.WAITING;
                }
            } else if (event instanceof OsgiBundleContextFailedEvent) {
                return ModuleStatus.FAILED;
            } else if (event instanceof OsgiBundleContextRefreshedEvent) {
                return ModuleStatus.STARTED;
            } else if (event instanceof OsgiBundleContextClosedEvent) {
                return ModuleStatus.STOPPED;
            }
            return ModuleStatus.UNKNOWN;
        }

    }

}
