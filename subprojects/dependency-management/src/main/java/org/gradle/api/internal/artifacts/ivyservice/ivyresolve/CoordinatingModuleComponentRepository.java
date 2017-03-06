/*
 * Copyright 2017 the original author or authors.
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
package org.gradle.api.internal.artifacts.ivyservice.ivyresolve;

import org.gradle.api.artifacts.ModuleVersionSelector;
import org.gradle.api.artifacts.component.ComponentIdentifier;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.gradle.api.internal.component.ArtifactType;
import org.gradle.cache.internal.ProducerGuard;
import org.gradle.internal.Factory;
import org.gradle.internal.component.external.model.DefaultModuleComponentIdentifier;
import org.gradle.internal.component.model.ComponentArtifactMetadata;
import org.gradle.internal.component.model.ComponentOverrideMetadata;
import org.gradle.internal.component.model.ComponentResolveMetadata;
import org.gradle.internal.component.model.DependencyMetadata;
import org.gradle.internal.component.model.ModuleSource;
import org.gradle.internal.resolve.result.BuildableArtifactResolveResult;
import org.gradle.internal.resolve.result.BuildableArtifactSetResolveResult;
import org.gradle.internal.resolve.result.BuildableComponentArtifactsResolveResult;
import org.gradle.internal.resolve.result.BuildableModuleComponentMetaDataResolveResult;
import org.gradle.internal.resolve.result.BuildableModuleVersionListingResolveResult;

public class CoordinatingModuleComponentRepository extends BaseModuleComponentRepository {
    public CoordinatingModuleComponentRepository(ModuleComponentRepository delegate, ProducerGuard<ComponentIdentifier> producerGuard) {
        super(delegate, delegate.getLocalAccess(), wrap(delegate, producerGuard));
    }

    protected static ModuleComponentRepositoryAccess wrap(ModuleComponentRepository delegate, ProducerGuard<ComponentIdentifier> producerGuard) {
        return new CoordinatingAccess(delegate.getRemoteAccess(), producerGuard);
    }

    private static class CoordinatingAccess implements ModuleComponentRepositoryAccess {
        private final ModuleComponentRepositoryAccess delegate;
        private final ProducerGuard<ComponentIdentifier> producerGuard;

        private CoordinatingAccess(ModuleComponentRepositoryAccess delegate, ProducerGuard<ComponentIdentifier> producerGuard) {
            this.delegate = delegate;
            this.producerGuard = producerGuard;
        }

        @Override
        public void listModuleVersions(final DependencyMetadata dependency, final BuildableModuleVersionListingResolveResult result) {
            ModuleVersionSelector requested = dependency.getRequested();
            producerGuard.guardByKey(DefaultModuleComponentIdentifier.newId(requested.getGroup(), requested.getName(), requested.getVersion()), new Factory<Void>() {
                @Override
                public Void create() {
                    delegate.listModuleVersions(dependency, result);
                    return null;
                }
            });
        }

        @Override
        public void resolveComponentMetaData(final ModuleComponentIdentifier moduleComponentIdentifier, final ComponentOverrideMetadata requestMetaData, final BuildableModuleComponentMetaDataResolveResult result) {
            producerGuard.guardByKey(moduleComponentIdentifier, new Factory<Void>() {
                @Override
                public Void create() {
                    delegate.resolveComponentMetaData(moduleComponentIdentifier, requestMetaData, result);
                    return null;
                }
            });
        }

        @Override
        public void resolveArtifacts(final ComponentResolveMetadata component, final BuildableComponentArtifactsResolveResult result) {
            producerGuard.guardByKey(component.getComponentId(), new Factory<Void>() {
                @Override
                public Void create() {
                    delegate.resolveArtifacts(component, result);
                    return null;
                }
            });
        }

        @Override
        public void resolveArtifactsWithType(final ComponentResolveMetadata component, final ArtifactType artifactType, final BuildableArtifactSetResolveResult result) {
            producerGuard.guardByKey(component.getComponentId(), new Factory<Void>() {
                @Override
                public Void create() {
                    delegate.resolveArtifactsWithType(component, artifactType, result);
                    return null;
                }
            });
        }

        @Override
        public void resolveArtifact(final ComponentArtifactMetadata artifact, final ModuleSource moduleSource, final BuildableArtifactResolveResult result) {
            producerGuard.guardByKey(artifact.getComponentId(), new Factory<Void>() {
                @Override
                public Void create() {
                    delegate.resolveArtifact(artifact, moduleSource, result);
                    return null;
                }
            });
        }
    }
}
