package org.kaiaccount.account.inter.transfer.payment;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.kaiaccount.account.inter.currency.Currency;
import org.kaiaccount.account.inter.type.Account;

import java.math.BigDecimal;
import java.util.Optional;

public interface Payment {

	@NotNull
	BigDecimal getAmount();

	@NotNull
	Currency<?> getCurrency();

	@NotNull
	Plugin getPlugin();

	@NotNull
	Optional<String> getReason();

	@NotNull
	Optional<Account> getFrom();

	boolean isPriority();
}
