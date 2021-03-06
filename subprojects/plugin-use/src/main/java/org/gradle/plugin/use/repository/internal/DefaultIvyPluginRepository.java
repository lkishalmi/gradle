/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.plugin.use.repository.internal;

import org.gradle.api.Action;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.artifacts.repositories.ArtifactRepository;
import org.gradle.api.artifacts.repositories.AuthenticationContainer;
import org.gradle.api.artifacts.repositories.IvyArtifactRepository;
import org.gradle.api.credentials.Credentials;
import org.gradle.api.internal.artifacts.DependencyResolutionServices;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.strategy.VersionSelectorScheme;
import org.gradle.api.internal.file.FileResolver;
import org.gradle.api.internal.plugins.repositories.IvyPluginRepository;
import org.gradle.internal.artifacts.repositories.AuthenticationSupportedInternal;

class DefaultIvyPluginRepository extends AbstractPluginRepository implements IvyPluginRepository {
    private static final String IVY = "ivy";

    private final DependencyResolutionServices dependencyResolutionServices;

    public DefaultIvyPluginRepository(
        FileResolver fileResolver, DependencyResolutionServices dependencyResolutionServices,
        VersionSelectorScheme versionSelectorScheme, AuthenticationSupportedInternal delegate) {
        super(IVY, fileResolver, dependencyResolutionServices, versionSelectorScheme, delegate);
        this.dependencyResolutionServices = dependencyResolutionServices;
    }

    @Override
    protected ArtifactRepository createArtifactRepository(RepositoryHandler repositoryHandler) {
        return dependencyResolutionServices.getResolveRepositoryHandler().ivy(new Action<IvyArtifactRepository>() {
            @Override
            public void execute(IvyArtifactRepository ivyArtifactRepository) {
                ivyArtifactRepository.setName(getArtifactRepositoryName());
                ivyArtifactRepository.setUrl(getUrl());
                Credentials credentials = authenticationSupport().getConfiguredCredentials();
                if (credentials != null) {
                    ((AuthenticationSupportedInternal)ivyArtifactRepository).setConfiguredCredentials(credentials);
                    ivyArtifactRepository.authentication(new Action<AuthenticationContainer>() {
                        @Override
                        public void execute(AuthenticationContainer authenticationContainer) {
                            authenticationContainer.addAll(authenticationSupport().getConfiguredAuthentication());
                        }
                    });
                }
            }
        });
    }
}
