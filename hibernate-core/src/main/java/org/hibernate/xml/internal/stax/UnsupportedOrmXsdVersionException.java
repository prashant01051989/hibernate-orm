/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2013, Red Hat Inc. or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.hibernate.xml.internal.stax;

import org.hibernate.HibernateException;
import org.hibernate.xml.spi.Origin;

/**
 * @author Steve Ebersole
 */
public class UnsupportedOrmXsdVersionException extends HibernateException {
	public UnsupportedOrmXsdVersionException(String requestedVersion, Origin origin) {
		super(
				String.format(
						"Encountered unsupported orm.xml xsd version [%s] in mapping document [type=%s, name=%s]",
						requestedVersion,
						origin.getType(),
						origin.getName()
				)
		);
	}
}