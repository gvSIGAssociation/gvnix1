/*
 * Copyright 2015 DiSiD Technologies S.L.L. All rights reserved.
 *
 * Project  : DiSiD petclinic
 * SVN Id   : $Id$
 */
package org.gvnix.web.datatables.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import javax.persistence.EntityManager;

/**
 * Service which provides EntityManager instance for a JPA Entity class from a
 * ActiveRecord Spring-Roo entity
 * 
 * @author gvNIX Team
 * @deprecated use
 *             {@link org.gvnix.web.datatables.util.impl.EntityManagerProviderImpl}
 */
public class EntityManagerProviderImpl implements EntityManagerProvider {

    /* (non-Javadoc)
     * @see org.gvnix.web.datatables.util.EntityManagerProvider#getEntityManager(java.lang.Class)
     */
    public EntityManager getEntityManager(Class klass) {

        try {
            Method[] methods = klass.getMethods();

            for (Method method : methods) {
                if ((method.getModifiers() & Modifier.STATIC) != 0) {
                    if (method.getReturnType() == EntityManager.class) {
                        method.setAccessible(true);
                        return (EntityManager) method.invoke(null, null);
                    }
                }
            }
        }
        catch (Exception e) {
            throw new IllegalStateException(
                    "Error getting entity manager for domain class: ".concat(klass
                            .getName()), e);
        }
        throw new IllegalStateException(
                "Cannot get entity manager for domain class: ".concat(klass
                        .getName()));
    }

}
