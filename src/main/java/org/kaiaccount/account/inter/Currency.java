package org.kaiaccount.account.inter;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;

public interface Currency {

	String getDisplayNameSingle();

	String getDisplayNameMultiple();

	String getSymbol();

	String getKeyName();

	Plugin getPlugin();

	boolean isDefault();

	default String formatName(@NotNull BigDecimal amount) {
		return this.formatName(amount, null);
	}

	default String formatName(@NotNull BigDecimal amount, @Nullable Integer toPoint) {
		BigDecimal display = amount;
		if (toPoint != null) {
			display = amount.setScale(toPoint, RoundingMode.HALF_UP);
		}
		if (amount.compareTo(BigDecimal.ONE) >= 1) {
			return this.getDisplayNameMultiple() + display;
		}
		return this.getDisplayNameSingle() + display;
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

}
