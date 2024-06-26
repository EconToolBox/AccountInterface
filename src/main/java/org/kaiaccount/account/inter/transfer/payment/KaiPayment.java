package org.kaiaccount.account.inter.transfer.payment;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kaiaccount.account.inter.currency.Currency;
import org.kaiaccount.account.inter.type.named.NamedAccountLike;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

public class KaiPayment implements Payment {

    private final @NotNull Currency<?> currency;
    private final @NotNull BigDecimal bigDecimal;
    private final @NotNull Plugin plugin;
    private final @Nullable String reason;
    private final @Nullable NamedAccountLike from;
    private final @Nullable NamedAccountLike to;
    private final boolean priority;

    public KaiPayment(@NotNull PaymentBuilder builder, @NotNull Plugin plugin) {
        this.currency = Objects.requireNonNull(builder.getCurrency(), "Currency is missing");
        this.bigDecimal = Objects.requireNonNull(builder.getAmount(), "Amount is missing");
        this.plugin = Objects.requireNonNull(plugin, "Plugin is missing");
        this.reason = builder.getReason();
        this.from = builder.getFrom();
        this.to = builder.getTo();
        this.priority = builder.isPriority();
        if (this.bigDecimal.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Negative number can not be used in payments");
        }
        //noinspection ConstantValue
        if (this.plugin == null) {
            throw new IllegalArgumentException("No plugin specified");
        }
    }

    @Override
    @CheckReturnValue
    public boolean isPriority() {
        return this.priority;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public BigDecimal getAmount() {
        return this.bigDecimal;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public Currency<?> getCurrency() {
        return this.currency;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public Plugin getPlugin() {
        return this.plugin;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public Optional<String> getReason() {
        return Optional.ofNullable(this.reason);
    }

    @NotNull
    @Override
    @CheckReturnValue
    public Optional<NamedAccountLike> getFrom() {
        return Optional.ofNullable(this.from);
    }

    @Override
    public @NotNull Optional<NamedAccountLike> getTo() {
        return Optional.ofNullable(this.to);
    }
}
