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
package org.fusesource.fabric.itests.paxexam.support;

import org.fusesource.fabric.api.DynamicReference;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class DelegatingInvocationHandler<T> implements InvocationHandler {

    private final BundleContext bundleContext;
    private final Class<T> type;
    private final DynamicReference<T> ref = new DynamicReference<T>();
    private final ServiceTracker<T, T> tracker;


    public DelegatingInvocationHandler(BundleContext bundleContext, Class<T> type) {
        this.bundleContext = bundleContext;
        this.type = type;
        this.tracker = new ServiceTracker<T, T>(bundleContext, type, null) {
            @Override
            public T addingService(ServiceReference<T> reference) {
                T service =  super.addingService(reference);
                ref.bind(service);
                return service;
            }

            @Override
            public void modifiedService(ServiceReference<T> reference, T service) {
                super.modifiedService(reference, service);
                ref.bind(service);
            }

            @Override
            public void removedService(ServiceReference<T> reference, T service) {
                super.removedService(reference, service);
                ref.unbind();
            }
        };
        tracker.open();
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            return method.invoke(ref.get(), args);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }
}
