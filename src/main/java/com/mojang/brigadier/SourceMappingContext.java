package com.mojang.brigadier;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Stores existing mappings of monads containing a source type {@link S} to their remapped type {@link R}.
 *
 * <p>
 * For internal use only.
 * </p>
 *
 * @param <S> The current (or previous) source type
 * @param <R> The remapped source type
 */
/*@ApiStatus.Internal*/
public class SourceMappingContext<S, R> {

    /*
     * Comparison by Identity, not by equals. In this case, the same instances are reused so it is not necessary to compare by equals.
     *
     * <p>
     * {@code Monad<S> -> Monad<R>}
     * </p>
     */
    private final Map<Object, Object> forwardMappings = new IdentityHashMap<>();
    private final Map<Object, Object> reverseMappings = new IdentityHashMap<>();

    private final Function<R, S> toOriginal;
    private final Function<S, R> toRemapped;

    public SourceMappingContext(final Function<R, S> toOriginal, final Function<S, R> toRemapped) {
        this.toOriginal = Objects.requireNonNull(toOriginal);
        this.toRemapped = Objects.requireNonNull(toRemapped);
    }

    @SuppressWarnings("unchecked")
    private <A, B> B computeIfAbsent(final A key, final Function<? super A, ? extends B> mappingFunction) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(mappingFunction, "mappingFunction");
        final B forward = (B) forwardMappings.computeIfAbsent(key, (Function<Object, Object>) mappingFunction);
        if (!reverseMappings.containsKey(forward)) {
            reverseMappings.put(forward, key);
        }
        return forward;
    }

    @SuppressWarnings("unchecked")
    private <T> T getOriginal(Object remapped, String name) {
        final T original = (T) reverseMappings.get(remapped);
        if (original == null) {
            throw new IllegalArgumentException("No original " + name + " found for " + remapped);
        }
        return original;
    }

    public S getOriginal(final R remapped) {
        return toOriginal.apply(remapped);
    }

    public R getRemapped(S original) {
        return toRemapped.apply(original);
    }

    public Command<R> getRemapped(final Command<S> original) {
        return computeIfAbsent(original, o -> (CommandContext<R> ctx) -> original.run(getOriginal(ctx)));
    }

    public Command<S> getOriginal(final Command<R> remapped) {
        return getOriginal(remapped, "command");
    }

    public LiteralCommandNode<R> getRemapped(final LiteralCommandNode<S> original) {
        return computeIfAbsent(original, o -> original.mapSource(this));
    }

    public LiteralCommandNode<S> getOriginal(final LiteralCommandNode<R> remapped) {
        return getOriginal(remapped, "literal command node");
    }

    public <T> ArgumentCommandNode<R, T> getRemapped(final ArgumentCommandNode<S, T> original) {
        return computeIfAbsent(original, o -> original.mapSource(this));
    }

    public <T> ArgumentCommandNode<S, T> getOriginal(final ArgumentCommandNode<R, T> remapped) {
        return getOriginal(remapped, "argument command node");
    }

    public RootCommandNode<R> getRemapped(final RootCommandNode<S> original) {
        return computeIfAbsent(original, o -> original.mapSource(this));
    }

    public RootCommandNode<S> getOriginal(final RootCommandNode<R> remapped) {
        return getOriginal(remapped, "root command node");
    }

    public CommandNode<R> getRemapped(final CommandNode<S> original) {
        if (original instanceof LiteralCommandNode) {
            return getRemapped((LiteralCommandNode<S>) original);
        } else if (original instanceof ArgumentCommandNode) {
            return getRemapped((ArgumentCommandNode<S, ?>) original);
        } else if (original instanceof RootCommandNode) {
            return getRemapped((RootCommandNode<S>) original);
        } else {
            throw new IllegalArgumentException("Unknown command node type " + original.getClass());
        }
    }

    public CommandNode<S> getOriginal(final CommandNode<R> remapped) {
        return getOriginal(remapped, "command node");
    }

    public RedirectModifier<R> getRemapped(final RedirectModifier<S> original) {
        return computeIfAbsent(original, o -> original.mapSource(this));
    }

    public RedirectModifier<S> getOriginal(final RedirectModifier<R> remapped) {
        return getOriginal(remapped, "redirect modifier");
    }


    /**
     * Converts a remapped {@link CommandContext} to an "original" one, which may be understood by original commands.
     *
     * <p>
     * The {@link CommandContext} must be mapped in reverse to the other types. This is due to the fact that the instance is given by the
     * command dispatcher and must be converted to a {@code CommandContext<S>} in order to be understood by lambdas expecting a command
     * source of the original type {@link S}.
     * <p>
     */
    public CommandContext<S> getOriginal(final CommandContext<R> remapped) {
        Objects.requireNonNull(remapped, "remapped");
        return remapped.mapSource(this);
    }
}
