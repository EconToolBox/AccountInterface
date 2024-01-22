package org.kaiaccount.account.inter.type.player;

import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.*;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import org.kaiaccount.AccountInterfaceManager;
import org.kaiaccount.account.inter.transfer.IsolatedTransaction;
import org.kaiaccount.account.inter.transfer.payment.Payment;
import org.kaiaccount.account.inter.transfer.payment.PaymentBuilder;
import org.kaiaccount.account.inter.transfer.result.SingleTransactionResult;
import org.kaiaccount.account.inter.transfer.result.TransactionResult;
import org.kaiaccount.account.inter.type.AccountType;
import org.kaiaccount.account.inter.type.IsolatedAccount;
import org.kaiaccount.account.inter.type.named.bank.player.PlayerBankAccount;
import org.kaiaccount.account.inter.type.named.bank.player.PlayerBankAccountBuilder;
import org.kaiaccount.AccountInterface;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedTransferQueue;
import java.util.function.Function;
import java.util.stream.Stream;

public abstract class AbstractPlayerAccount<Self extends AbstractPlayerAccount<Self>>
        implements PlayerAccount<Self>, AccountType {

    private final @NotNull OfflinePlayer player;

    private final @NotNull IsolatedAccount account;
    private final @NotNull Collection<PlayerBankAccount> banks = new LinkedTransferQueue<>();

    public AbstractPlayerAccount(@NotNull PlayerAccountBuilder builder) {
        this.player = builder.getPlayer();
        this.account = new IsolatedAccount(builder.getInitialBalance());
        if (this.player == null) {
            throw new IllegalArgumentException("Player is missing from builder");
        }
    }

    @CheckReturnValue
    private CompletableFuture<? extends TransactionResult> withdrawWithBanksMostlySynced(@NotNull Payment payment) {
        BigDecimal playerBalance = this.getBalance(payment.getCurrency());
        if (playerBalance.compareTo(payment.getAmount()) > 0) {
            return withdraw(payment);
        }
        List<AccountType> includedBanks = new LinkedList<>();
        BigDecimal calculated = BigDecimal.ZERO;
        for (PlayerBankAccount account : this.getBanks()) {
            if (!(account instanceof AccountType)) {
                continue;
            }
            calculated = calculated.add(account.getBalance(payment.getCurrency()));
            includedBanks.add((AccountType) account);
            if (calculated.compareTo(payment.getAmount()) >= 0) {
                break;
            }
        }

        List<AccountType> accounts = new LinkedList<>(includedBanks);
        accounts.add(this);

        CompletableFuture<TransactionResult> retur = new CompletableFuture<>();

        new IsolatedTransaction(map -> {
            BigDecimal processed = payment.getAmount();
            Collection<CompletableFuture<? extends TransactionResult>> ret = new LinkedList<>();
            if (playerBalance.compareTo(BigDecimal.ZERO) > 0) {
                processed = processed.subtract(playerBalance);
                IsolatedAccount account = map.get(this);
                CompletableFuture<SingleTransactionResult> withdraw = account
                        .withdraw(new PaymentBuilder().setAmount(playerBalance)
                                .setCurrency(payment.getCurrency())
                                .setReason(payment.getReason().orElse(null))
                                .setFrom(payment.getFrom().orElse(null))
                                .build(payment.getPlugin()));
                ret.add(withdraw.thenApply(single -> single));
            }

            for (AccountType bank : includedBanks) {
                BigDecimal balance = bank.getBalance(payment.getCurrency());
                if (balance.compareTo(processed) > 0) {
                    balance = processed;
                }
                processed = processed.subtract(balance);
                IsolatedAccount bankAccount = map.get(bank);
                CompletableFuture<SingleTransactionResult> withdraw = bankAccount
                        .withdraw(new PaymentBuilder().setAmount(balance)
                                .setFrom(payment.getFrom().orElse(null))
                                .setReason(payment.getReason().orElse(null))
                                .setCurrency(payment.getCurrency())
                                .build(payment.getPlugin()));
                ret.add(withdraw.thenApply(single -> single));
            }
            return ret;
        }, accounts).start().thenAccept(retur::complete);

        return retur;
    }

    @NotNull
    @Override
    @CheckReturnValue
    public CompletableFuture<TransactionResult> multipleTransaction(
            @NotNull Function<IsolatedAccount, CompletableFuture<? extends TransactionResult>>... transactions) {
        CompletableFuture<TransactionResult> ret = new CompletableFuture<>();
        new IsolatedTransaction(map -> {
            IsolatedAccount isolated = map.get(this);
            Stream<CompletableFuture<? extends TransactionResult>> stream = Arrays.stream(transactions).parallel()
                    .map(f -> f.apply(isolated));
            return stream.toList();
        }, this).start().thenAccept(ret::complete);
        return ret;
    }

    @Override
    @CheckReturnValue
    public @NotNull CompletableFuture<TransactionResult> withdrawWithBanks(@NotNull Payment payment) {
        CompletableFuture<TransactionResult> result = new CompletableFuture<>();
        new Thread(() -> this.withdrawWithBanksMostlySynced(payment).thenAccept(result::complete)).start();
        return result;
    }

    @Override
    @UnmodifiableView
    @CheckReturnValue
    public @NotNull Collection<PlayerBankAccount> getBanks() {
        // load others
        return Collections.unmodifiableCollection(this.banks);
    }

    @Override
    public @NotNull PlayerBankAccount createBankAccount(@NotNull String name) {
        PlayerBankAccount account = new PlayerBankAccountBuilder().setName(name).setAccount(this).build();
        this.banks.add(account);
        return account;
    }

    @Override
    public @NotNull boolean deleteBankAccount(@NotNull PlayerBankAccount account) {
        for (UUID accesser : account.getAccounts().keySet()) {
            account.removeAccount(accesser);
        }
        this.banks.remove(account);
        return true;
    }

    @Override
    public void registerBank(@NotNull PlayerBankAccount account) {
        if (!account.getAccountHolder().equals(this)) {
            throw new IllegalArgumentException(
                    "provided PlayerBankAccount cannot be registered. The AccountHolder must be this player");
        }
        this.banks.add(account);
    }

    @Override
    @CheckReturnValue
    public @NotNull OfflinePlayer getPlayer() {
        return this.player;
    }

    @Override
    @CheckReturnValue
    public @NotNull IsolatedAccount getIsolated() {
        return this.account;
    }
}
