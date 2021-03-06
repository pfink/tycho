/*******************************************************************************
 * Copyright (c) 2012 SAP SE and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SAP SE - initial API and implementation
 *******************************************************************************/
package org.eclipse.tycho.p2.target.ee;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.IRequirement;
import org.eclipse.equinox.p2.metadata.MetadataFactory;
import org.eclipse.equinox.p2.metadata.Version;
import org.eclipse.equinox.p2.metadata.VersionRange;
import org.eclipse.equinox.p2.publisher.actions.JREAction;
import org.eclipse.tycho.p2.util.resolution.ExecutionEnvironmentResolutionHints;

@SuppressWarnings("restriction")
public final class CustomEEResolutionHints implements ExecutionEnvironmentResolutionHints {

    // primary members
    private final String eeName;

    // derived members
    private transient String unitName;
    private transient Version unitVersion;

    public CustomEEResolutionHints(String eeName) throws InvalidEENameException {
        this.eeName = eeName;
        parse(eeName);
    }

    /** see {@link JREAction#generateJREIUData()} */
    void parse(String eeName) throws InvalidEENameException {
        int idx = eeName.indexOf('-');
        if (idx == -1) {
            throw new InvalidEENameException(eeName);
        }
        String name = eeName.substring(0, idx);
        name = name.replace('/', '.');
        name = name.replace('_', '.');
        this.unitName = "a.jre." + name.toLowerCase(Locale.ENGLISH);
        String version = eeName.substring(idx + 1);
        try {
            this.unitVersion = Version.create(version);
        } catch (IllegalArgumentException e) {
            throw new InvalidEENameException(eeName);
        }
    }

    @Override
    public boolean isEESpecificationUnit(IInstallableUnit unit) {
        return unitName.equals(unit.getId()) && unit.getVersion().equals(unitVersion);
    }

    @Override
    public boolean isNonApplicableEEUnit(IInstallableUnit iu) {
        return isJreUnit(iu.getId()) && !isEESpecificationUnit(iu);
    }

    private boolean isJreUnit(String id) {
        return id.startsWith("a.jre") || id.startsWith("config.a.jre");
    }

    @Override
    public Collection<IInstallableUnit> getMandatoryUnits() {
        return Collections.emptyList();
    }

    @Override
    public Collection<IInstallableUnit> getTemporaryAdditions() {
        return Collections.emptyList();
    }

    @Override
    public Collection<IRequirement> getMandatoryRequires() {
        VersionRange strictUnitRange = new VersionRange(unitVersion, true, unitVersion, true);
        return Collections.singleton(MetadataFactory.createRequirement(IInstallableUnit.NAMESPACE_IU_ID, unitName,
                strictUnitRange, null, false, false));
    }

    @Override
    public int hashCode() {
        final int prime = 37;
        int result = 1;
        result = prime * result + ((eeName == null) ? 0 : eeName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof CustomEEResolutionHints))
            return false;

        CustomEEResolutionHints other = (CustomEEResolutionHints) obj;
        return eq(eeName, other.eeName);
    }

    private static <T> boolean eq(T left, T right) {
        if (left == right) {
            return true;
        } else if (left == null) {
            return false;
        } else {
            return left.equals(right);
        }
    }
}
