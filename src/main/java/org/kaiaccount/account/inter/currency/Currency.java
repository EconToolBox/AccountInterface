package org.kaiaccount.account.inter.currency;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kaiaccount.account.inter.io.Serializable;
import org.kaiaccount.utils.builder.Buildable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

public interface Currency<Self extends Currency<Self>> extends Serializable<Self>, Buildable<Currency<?>, CurrencyBuilder> {

    @NotNull
    String getDisplayNameSingle();

    @NotNull
    String getDisplayNameMultiple();

    @NotNull
    String getDisplayNameShort();

    @NotNull
    String getSymbol();

    @NotNull
    String getKeyName();

    @NotNull
    Plugin getPlugin();

    boolean isDefault();

    void setDefault(boolean check);

    @NotNull
    Optional<BigDecimal> getWorth();

    void setWorth(@Nullable BigDecimal worth);

    default void removeWorth() {
        this.setWorth(null);
    }

    default String formatName(@NotNull BigDecimal amount) {
        return this.formatName(amount, null);
    }

    default String formatName(@NotNull BigDecimal amount, @Nullable Integer toPoint) {
        BigDecimal display = amount;
        if (toPoint != null) {
            display = amount.setScale(toPoint, RoundingMode.HALF_UP);
        }
        if (amount.compareTo(BigDecimal.ONE) >= 1) {
            return display + " " + this.getDisplayNameMultiple();
        }
        return display + " " + this.getDisplayNameSingle();
    }

    default String formatSymbol(@NotNull BigDecimal amount) {
        return this.formatSymbol(amount, null);
    }

    default String formatSymbol(@NotNull BigDecimal amount, @Nullable Integer toPoint) {
        BigDecimal display = amount;
        if (toPoint != null) {
            display = amount.setScale(toPoint, RoundingMode.HALF_UP);
        }
        return this.getSymbol() + display;
    }

    @Override
    default CurrencyBuilder toBuilder() {
        return new CurrencyBuilder()
                .setDefault(this.isDefault())
                .setName(this.getKeyName())
                .setDisplayNameMultiple(this.getDisplayNameMultiple())
                .setDisplayNameShort(this.getDisplayNameShort())
                .setDisplayNameSingle(this.getDisplayNameSingle())
                .setPlugin(this.getPlugin())
                .setSymbol(this.getSymbol())
                .setWorth(this.getWorth().orElse(null));
    }
}
