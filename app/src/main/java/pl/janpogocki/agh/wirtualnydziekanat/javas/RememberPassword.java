package pl.janpogocki.agh.wirtualnydziekanat.javas;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.NetworkErrorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Jan on 04.08.2016.
 * Encrypt and return password saved in internal storage
 */

public class RememberPassword {
    private AccountManager accountManager;
    private Account myAccount = null;
    private Context context;

    public RememberPassword(Context c){
        context = c;
        accountManager = AccountManager.get(context);
        getAccount();
    }

    public void getAccount(){
        Account[] accounts = accountManager.getAccountsByType("pl.janpogocki.agh.wirtualnydziekanat");
        for (Account account : accounts) {
            if (account.name.length() >= 6) {
                myAccount = accounts[0];
                break;
            }
        }
    }

    public Boolean isRemembered(){
        if (myAccount != null)
            return true;
        else
            return false;
    }

    public void save(String _login, String _password) {
        Account account = new Account(_login, "pl.janpogocki.agh.wirtualnydziekanat");
        accountManager.addAccountExplicitly(account, _password, null);
    }

    public void remove(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            //accountManager.removeAccountExplicitly(myAccount);
            accountManager.removeAccount(myAccount, null, null, null);
        } else {
            accountManager.removeAccount(myAccount, null, null);
        }
    }

    public String getLogin() throws IOException {
        return myAccount.name;
    }

    public String getPassword() throws IOException{
        return accountManager.getPassword(myAccount);
    }
}
