// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.

package com.mojang.brigadier;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import java.util.Collection;
import java.util.stream.Collectors;

@FunctionalInterface
public interface RedirectModifier<S> {
    Collection<S> apply(CommandContext<S> context) throws CommandSyntaxException;

    default <R> RedirectModifier<R> mapSource(final SourceMappingContext<S, R> mapper) {
        return context -> apply(mapper.getOriginal(context))
            .stream().map(mapper::getRemapped).collect(Collectors.toList());
    }
}
