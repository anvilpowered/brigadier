// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.

package com.mojang.brigadier.suggestion;

import com.mojang.brigadier.SourceMappingContext;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@FunctionalInterface
public interface SuggestionProvider<S> {
    CompletableFuture<Suggestions> getSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) throws CommandSyntaxException;

    default <R> SuggestionProvider<R> mapSource(final SourceMappingContext<S, R> mapper) {
        return (context, builder) -> getSuggestions(mapper.getOriginal(context), builder);
    }
}
