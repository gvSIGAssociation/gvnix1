package org.gvnix.addon.gva.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.ReferenceStrategy;
import org.apache.felix.scr.annotations.References;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.addon.gva.security.providers.SecurityProvider;
import org.gvnix.addon.gva.security.providers.SecurityProviderId;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.project.FeatureNames;
import org.springframework.roo.project.ProjectOperations;

/**
 * Implementation of {@link SecurityProviderOperations} interface.
 * 
 * @since 1.1.1
 */
@Component
@Service
@Reference(name = "provider",
        strategy = ReferenceStrategy.EVENT,
        policy = ReferencePolicy.DYNAMIC,
        referenceInterface = SecurityProvider.class,
        cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE)
public class SecurityProviderOperationsImpl implements
        SecurityProviderOperations {

    @Reference
    private ProjectOperations projectOperations;

    private List<SecurityProvider> providers = new ArrayList<SecurityProvider>();
    private List<SecurityProviderId> providersId = null;

    /** {@inheritDoc} */
    public boolean checkSecuritySetup() {
        return projectOperations
                .isFeatureInstalledInFocusedModule(FeatureNames.SECURITY);
    }

    protected void bindProvider(final SecurityProvider provider) {
        providers.add(provider);
    }

    protected void unbindProvider(final SecurityProvider provider) {
        providers.remove(provider);
    }

    @Override
    public List<SecurityProviderId> getProvidersId() {
        if (providersId == null) {
            providersId = new ArrayList<SecurityProviderId>();
            for (SecurityProvider tmpProvider : providers) {
                providersId.add(new SecurityProviderId(tmpProvider));
            }
            providersId = Collections.unmodifiableList(providersId);
        }
        return providersId;
    }

    @Override
    public void installProvider(SecurityProviderId prov,
            JavaPackage targetPackage) {
        SecurityProvider provider = null;
        for (SecurityProvider tmpProvider : providers) {
            if (prov.is(tmpProvider)) {
                provider = tmpProvider;
                break;
            }
        }
        if (provider == null) {
            throw new RuntimeException("Provider '".concat(prov.getId())
                    .concat("' not found'"));
        }
        provider.install(targetPackage);
    }

    @Override
    public SecurityProviderId getProviderIdByName(String name) {
        SecurityProviderId provider = null;
        for (SecurityProvider tmpProvider : providers) {
            if (tmpProvider.getName().equals(name)) {
                provider = new SecurityProviderId(tmpProvider);
            }
        }
        return provider;
    }

}