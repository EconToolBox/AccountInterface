package org.kaiaccount.account.inter.type.player;

import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.*;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import org.kaiaccount.account.inter.currency.Currency;
import org.kaiaccount.account.inter.transfer.payment.Payment;
import org.kaiaccount.account.inter.transfer.result.TransactionResult;
import org.kaiaccount.account.inter.type.Account;
import org.kaiaccount.account.inter.type.named.NamedAccountLike;
import org.kaiaccount.account.inter.type.named.bank.player.PlayerBankAccount;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public interface PlayerAccount<Self extends PlayerAccount<Self>> extends Account, NamedAccountLike {

    @NotNull
    @UnmodifiableView
    @CheckReturnValue
    Collection<PlayerBankAccount> getBanks();

    @NotNull
    PlayerBankAccount createBankAccount(@NotNull String name);

	@NotNull
	boolean deleteBankAccount(@NotNull PlayerBankAccount account);

    void registerBank(@NotNull PlayerBankAccount account);

    @NotNull
    @CheckReturnValue
    OfflinePlayer getPlayer();

    @Nls
    @Override
    default @NotNull String getAccountName() {
        return Objects.requireNonNullElse(getPlayer().getName(), "Unknown Player");
    }

    @NotNull
    @CheckReturnValue
    CompletableFuture<TransactionResult> withdrawWithBanks(@NotNull Payment payment);

    @NotNull
    @CheckReturnValue
    default Optional<PlayerBankAccount> getBank(@NotNull String keyName) {
        return this.getBanks()
                .parallelStream()
                .filter(account -> account.getAccountName().equals(keyName))
                .findAny();
    }

    @NotNull
    @UnmodifiableView
    @CheckReturnValue
    default Map<Currency<?>, BigDecimal> getBalancesWithBanks() {
        Map<Currency<?>, BigDecimal> playerBalance = new HashMap<>(this.getBalances());
        Map<Currency<?>, BigDecimal> bankBalance = this.getBanks()
                .parallelStream()
                .flatMap(bank -> bank.getBalances().entrySet().parallelStream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, BigDecimal::add));
        playerBalance.putAll(bankBalance);
        return Collections.unmodifiableMap(playerBalance);
    }
}
