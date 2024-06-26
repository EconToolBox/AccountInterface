package org.kaiaccount.account.inter.type.named.bank.player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import org.kaiaccount.account.inter.type.named.AbstractNamedAccountLike;
import org.kaiaccount.account.inter.type.named.bank.BankPermission;
import org.kaiaccount.account.inter.type.player.PlayerAccount;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractPlayerBankAccount
        extends AbstractNamedAccountLike
        implements PlayerBankAccount {

    private final @NotNull PlayerAccount account;
    private final @NotNull Map<UUID, Collection<BankPermission>> bankPermissions = new ConcurrentHashMap<>();

    public AbstractPlayerBankAccount(@NotNull PlayerBankAccountBuilder builder) {
        super(builder.getName(), builder.getInitialBalance());
        this.account = builder.getAccount();
    }

    @Override
    public @NotNull PlayerAccount getAccountHolder() {
        return this.account;
    }

    @Override
    public void addAccount(@NotNull UUID uuid, Collection<BankPermission> permissions) {
        if (permissions.isEmpty()) {
            throw new RuntimeException("Cannot add an account without permissions");
        }
        this.bankPermissions.put(uuid, permissions);
    }


    @Override
    public void removeAccount(@NotNull UUID uuid) {
        this.bankPermissions.remove(uuid);
    }

    @NotNull
    @Override
    @UnmodifiableView
    public Map<UUID, Collection<BankPermission>> getAccounts() {
        Map<UUID, Collection<BankPermission>> map = new HashMap<>(this.bankPermissions);
        map.put(this.getAccountHolder().getPlayer().getUniqueId(), Arrays.asList(BankPermission.values()));
        return Collections.unmodifiableMap(map);
    }
}
